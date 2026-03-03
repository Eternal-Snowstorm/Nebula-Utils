package dev.celestiacraft.libs.client.exporter;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.math.Axis;
import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.compat.ICheckModLoaded;
import dev.celestiacraft.libs.compat.ie.IEModelHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class StructureRenderer implements AutoCloseable {

	/**
	 * 禁用视锥体裁剪的 Frustum，解决 Create 传送带等 BlockEntityRenderer
	 * 使用玩家摄像机视锥体裁剪物品的问题（SafeBlockEntityRenderer.shouldCullItem）。
	 */
	private static final Frustum NO_CULL_FRUSTUM = new Frustum(
			new Matrix4f().identity(), new Matrix4f().identity()
	) {
		@Override
		public boolean isVisible(@NotNull AABB pBoundingBox) {
			return true;
		}
	};

	@Nullable
	private static final Field CAPTURED_FRUSTUM_FIELD;

	static {
		Field found = null;
		try {
			for (Field field : LevelRenderer.class.getDeclaredFields()) {
				if (field.getType() == Frustum.class) {
					field.setAccessible(true);
					found = field;
					break;
				}
			}
		} catch (Exception ignored) {
		}
		CAPTURED_FRUSTUM_FIELD = found;
	}

	private final StructureScene scene;
	private final VirtualBlockLevel virtualLevel;

	private final Map<RenderType, VertexBuffer> blockVBOs = new HashMap<>();
	@Nullable
	private VertexBuffer fluidVBO;
	private boolean vbosBuilt;

	public StructureRenderer(StructureScene scene, Level level) {
		this.scene = scene;
		this.virtualLevel = new VirtualBlockLevel(level, scene.getBlocks(), scene.getBlockEntityNbt());
		this.virtualLevel.initAllBlockEntities();
		this.virtualLevel.initEntities(scene.getEntities());
		this.virtualLevel.refreshTransmitterConnections();
		this.virtualLevel.updateNeighborStates();
	}

	// 公共渲染入口

	public void renderPreview(PoseStack guiPose, float rotX, float rotY, float zoom, float panX, float panY, int screenWidth, int screenHeight) {
		Minecraft mc = Minecraft.getInstance();
		float aspect = (float) mc.getWindow().getGuiScaledWidth() / mc.getWindow().getGuiScaledHeight();
		Matrix4f savedProj = new Matrix4f(RenderSystem.getProjectionMatrix());

		setupCamera(rotX, rotY, zoom, panX, panY, aspect);

		Lighting.setupFor3DItems();
		RenderSystem.enableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
		renderScene(new PoseStack(), bufferSource);
		bufferSource.endBatch();

		RenderSystem.disableBlend();
		teardownCamera();
		RenderSystem.setProjectionMatrix(savedProj, VertexSorting.ORTHOGRAPHIC_Z);
	}

	public void exportToPng(Path outputPath, int resolution, float rotX, float rotY, float zoom, float panX, float panY, Consumer<Path> onSuccess, Consumer<Exception> onError) {
		Minecraft mc = Minecraft.getInstance();
		float aspect = (float) mc.getWindow().getGuiScaledWidth() / mc.getWindow().getGuiScaledHeight();

		int maxSize = Math.min(resolution, GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE));
		int exportWidth, exportHeight;
		if (aspect >= 1.0f) {
			exportWidth = maxSize;
			exportHeight = Math.max(1, Math.round(maxSize / aspect));
		} else {
			exportWidth = Math.max(1, Math.round(maxSize * aspect));
			exportHeight = maxSize;
		}

		TextureTarget fbo = new TextureTarget(exportWidth, exportHeight, true, Minecraft.ON_OSX);
		fbo.setClearColor(0f, 0f, 0f, 0f);
		fbo.clear(Minecraft.ON_OSX);
		fbo.bindWrite(true);
		RenderSystem.viewport(0, 0, exportWidth, exportHeight);

		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(
				GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
				GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
		);
		RenderSystem.enableDepthTest();

		setupCamera(rotX, rotY, zoom, panX, panY, aspect);
		Lighting.setupFor3DItems();

		MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
		renderScene(new PoseStack(), bufferSource);
		bufferSource.endBatch();

		NativeImage image;
		try {
			image = new NativeImage(exportWidth, exportHeight, false);
			RenderSystem.bindTexture(fbo.getColorTextureId());
			image.downloadTexture(0, false);
			image.flipY();
		} catch (OutOfMemoryError error) {
			NebulaLibs.LOGGER.error("Export failed: resolution {}x{} is too large, out of memory", exportWidth, exportHeight);
			teardownCamera();
			fbo.destroyBuffers();
			restoreMainTarget(mc);
			onError.accept(new RuntimeException("Resolution too large, out of memory. Try a smaller value."));
			return;
		}

		teardownCamera();
		fbo.destroyBuffers();
		restoreMainTarget(mc);

		CompletableFuture.runAsync(() -> {
			try {
				if (outputPath.getParent() != null) {
					outputPath.getParent().toFile().mkdirs();
				}
				image.writeToFile(outputPath);
				image.close();
				mc.execute(() -> onSuccess.accept(outputPath));
			} catch (Exception exception) {
				NebulaLibs.LOGGER.error("Export failed", exception);
				image.close();
				mc.execute(() -> onError.accept(exception));
			}
		});
	}

	// 摄像机设置/恢复

	private void setupCamera(float rotX, float rotY, float zoom, float panX, float panY, float aspect) {
		float dim = Math.max(scene.getMaxDimension(), 1f);
		float orthoRange = dim / zoom * 0.7f;

		Matrix4f ortho = new Matrix4f().ortho(
				-orthoRange * aspect, orthoRange * aspect,
				-orthoRange, orthoRange,
				0.01f, 400f
		);
		RenderSystem.setProjectionMatrix(ortho, VertexSorting.ORTHOGRAPHIC_Z);

		PoseStack modelView = RenderSystem.getModelViewStack();
		modelView.pushPose();
		modelView.setIdentity();
		modelView.translate(panX, panY, -200);
		modelView.mulPose(Axis.XP.rotationDegrees(rotX));
		modelView.mulPose(Axis.YP.rotationDegrees(rotY));
		modelView.translate(-scene.getCenterX(), -scene.getCenterY(), -scene.getCenterZ());
		RenderSystem.applyModelViewMatrix();
	}

	private static void teardownCamera() {
		PoseStack modelView = RenderSystem.getModelViewStack();
		modelView.popPose();
		RenderSystem.applyModelViewMatrix();
	}

	private static void restoreMainTarget(Minecraft mc) {
		mc.getMainRenderTarget().bindWrite(true);
		RenderSystem.viewport(0, 0, mc.getWindow().getWidth(), mc.getWindow().getHeight());
	}

	// 场景渲染

	private void renderScene(PoseStack stack, MultiBufferSource.BufferSource source) {
		Minecraft mc = Minecraft.getInstance();
		BlockRenderDispatcher dispatcher = mc.getBlockRenderer();

		Frustum savedFrustum = disableFrustumCulling();

		if (!vbosBuilt) {
			buildVBOs(dispatcher);
		}

		drawBlockVBOs();

		renderBlockEntities(mc, source);
		source.endBatch();

		drawFluidVBO();

		renderEntities(stack, mc, source);
		source.endBatch();

		restoreFrustumCulling(savedFrustum);
	}

	// VBO 构建与绘制

	private void buildVBOs(BlockRenderDispatcher dispatcher) {
		PoseStack poseStack = new PoseStack();
		RandomSource random = RandomSource.create();
		ModelBlockRenderer modelRenderer = dispatcher.getModelRenderer();

		ModelBlockRenderer.enableCaching();

		Map<RenderType, BufferBuilder> builders = new HashMap<>();
		for (RenderType renderType : RenderType.chunkBufferLayers()) {
			BufferBuilder builder = new BufferBuilder(256 * 1024);
			builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
			builders.put(renderType, builder);
		}

		for (Map.Entry<BlockPos, BlockState> entry : scene.getBlocks().entrySet()) {
			BlockPos pos = entry.getKey();
			BlockState state = entry.getValue();

			if (state.getRenderShape() != RenderShape.MODEL) {
				continue;
			}

			ModelData modelData = getModelData(pos, state, dispatcher);
			BakedModel model = dispatcher.getBlockModel(state);

			for (RenderType renderType : model.getRenderTypes(state, random, modelData)) {
				BufferBuilder builder = builders.get(renderType);
				if (builder == null) {
					continue;
				}

				poseStack.pushPose();
				poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

				modelRenderer.tesselateBlock(
						virtualLevel, model, state, pos, poseStack, builder,
						false, random, state.getSeed(pos),
						OverlayTexture.NO_OVERLAY, modelData, renderType
				);

				poseStack.popPose();
			}
		}

		for (Map.Entry<RenderType, BufferBuilder> entry : builders.entrySet()) {
			BufferBuilder.RenderedBuffer rendered = entry.getValue().endOrDiscardIfEmpty();
			if (rendered != null) {
				VertexBuffer vbo = new VertexBuffer(VertexBuffer.Usage.STATIC);
				vbo.bind();
				vbo.upload(rendered);
				blockVBOs.put(entry.getKey(), vbo);
			}
		}
		VertexBuffer.unbind();

		ModelBlockRenderer.clearCache();

		buildFluidVBO(dispatcher);

		vbosBuilt = true;
	}

	private void buildFluidVBO(BlockRenderDispatcher dispatcher) {
		BufferBuilder builder = new BufferBuilder(256 * 1024);
		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

		for (Map.Entry<BlockPos, BlockState> entry : scene.getBlocks().entrySet()) {
			BlockPos pos = entry.getKey();
			BlockState state = entry.getValue();
			FluidState fluidState = state.getFluidState();

			if (fluidState.isEmpty()) {
				continue;
			}

			int chunkBaseX = pos.getX() & ~15;
			int chunkBaseY = pos.getY() & ~15;
			int chunkBaseZ = pos.getZ() & ~15;

			VertexConsumer buffer = (chunkBaseX | chunkBaseY | chunkBaseZ) == 0
					? builder
					: new OffsetVertexConsumer(builder, chunkBaseX, chunkBaseY, chunkBaseZ);

			dispatcher.renderLiquid(pos, virtualLevel, buffer, state, fluidState);
		}

		BufferBuilder.RenderedBuffer rendered = builder.endOrDiscardIfEmpty();
		if (rendered != null) {
			fluidVBO = new VertexBuffer(VertexBuffer.Usage.STATIC);
			fluidVBO.bind();
			fluidVBO.upload(rendered);
			VertexBuffer.unbind();
		}
	}

	private void drawBlockVBOs() {
		for (Map.Entry<RenderType, VertexBuffer> entry : blockVBOs.entrySet()) {
			RenderType renderType = entry.getKey();
			VertexBuffer vbo = entry.getValue();

			renderType.setupRenderState();
			vbo.bind();
			vbo.drawWithShader(
					RenderSystem.getModelViewMatrix(),
					RenderSystem.getProjectionMatrix(),
					RenderSystem.getShader()
			);
			VertexBuffer.unbind();
			renderType.clearRenderState();
		}
	}

	private void drawFluidVBO() {
		if (fluidVBO == null) {
			return;
		}

		RenderType renderType = RenderType.translucent();
		renderType.setupRenderState();
		fluidVBO.bind();
		fluidVBO.drawWithShader(
				RenderSystem.getModelViewMatrix(),
				RenderSystem.getProjectionMatrix(),
				RenderSystem.getShader()
		);
		VertexBuffer.unbind();
		renderType.clearRenderState();
	}

	// VBO 资源管理

	private void releaseVBOs() {
		for (VertexBuffer vbo : blockVBOs.values()) {
			vbo.close();
		}
		blockVBOs.clear();

		if (fluidVBO != null) {
			fluidVBO.close();
			fluidVBO = null;
		}

		vbosBuilt = false;
	}

	@Override
	public void close() {
		releaseVBOs();
	}

	// 方块数据辅助

	private ModelData getModelData(BlockPos pos, BlockState state, BlockRenderDispatcher dispatcher) {
		ModelData modelData = ModelData.EMPTY;
		BlockEntity blockEntity = virtualLevel.getBlockEntity(pos);

		if (blockEntity != null) {
			try {
				modelData = blockEntity.getModelData();
			} catch (Exception ignored) {
			}

			if (modelData == ModelData.EMPTY && ICheckModLoaded.hasIE()) {
				ModelData ieData = IEModelHelper.getModelOffset(blockEntity, state);
				if (ieData != null) {
					modelData = ieData;
				}
			}
		}

		BakedModel bakedModel = dispatcher.getBlockModel(state);
		return bakedModel.getModelData(virtualLevel, pos, state, modelData);
	}

	// 每帧渲染（BlockEntity / Entity 有动画，不缓存）

	private void renderBlockEntities(Minecraft mc, MultiBufferSource.BufferSource source) {
		for (BlockEntity blockEntity : virtualLevel.getRenderedBlockEntities()) {
			BlockEntityRenderer<BlockEntity> renderer = mc.getBlockEntityRenderDispatcher().getRenderer(blockEntity);
			if (renderer == null) {
				continue;
			}

			BlockPos pos = blockEntity.getBlockPos();
			PoseStack pose = new PoseStack();
			pose.translate(pos.getX(), pos.getY(), pos.getZ());

			try {
				renderer.render(blockEntity, 0f, pose, source, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
			} catch (Exception ignored) {
			}
		}
	}

	private void renderEntities(PoseStack stack, Minecraft mc, MultiBufferSource.BufferSource source) {
		for (Entity entity : virtualLevel.getRenderedEntities()) {
			try {
				stack.pushPose();
				mc.getEntityRenderDispatcher().render(
						entity, entity.getX(), entity.getY(), entity.getZ(),
						entity.getYRot(), 0f, stack, source, LightTexture.FULL_BRIGHT
				);
				stack.popPose();
			} catch (Exception ignored) {
			}
		}
	}

	// 视锥体裁剪控制

	@Nullable
	private static Frustum disableFrustumCulling() {
		if (CAPTURED_FRUSTUM_FIELD == null) return null;
		try {
			LevelRenderer lr = Minecraft.getInstance().levelRenderer;
			Frustum saved = (Frustum) CAPTURED_FRUSTUM_FIELD.get(lr);
			CAPTURED_FRUSTUM_FIELD.set(lr, NO_CULL_FRUSTUM);
			return saved;
		} catch (Exception e) {
			return null;
		}
	}

	private static void restoreFrustumCulling(@Nullable Frustum saved) {
		if (CAPTURED_FRUSTUM_FIELD == null) return;
		try {
			LevelRenderer lr = Minecraft.getInstance().levelRenderer;
			CAPTURED_FRUSTUM_FIELD.set(lr, saved);
		} catch (Exception ignored) {
		}
	}
}
