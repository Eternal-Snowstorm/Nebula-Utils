package dev.celestiacraft.libs.client.exporter;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.math.Axis;
import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.compat.ICheckModLoaded;
import dev.celestiacraft.libs.compat.ie.IEModelHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class StructureRenderer {
	private final StructureScene scene;
	private final VirtualBlockLevel virtualLevel;

	public StructureRenderer(StructureScene scene, Level level) {
		this.scene = scene;
		this.virtualLevel = new VirtualBlockLevel(level, scene.getBlocks(), scene.getBlockEntityNbt());
		this.virtualLevel.initAllBlockEntities();
		this.virtualLevel.initEntities(scene.getEntities());
		this.virtualLevel.refreshTransmitterConnections();
		this.virtualLevel.updateNeighborStates();
	}

	public void renderPreview(PoseStack guiPose, float rotX, float rotY, float zoom, float panX, float panY, int screenWidth, int screenHeight) {
		float dim = scene.getMaxDimension();

		if (dim < 1f) {
			dim = 1f;
		}

		float orthoRange = dim / zoom * 0.7f;
		Minecraft mc = Minecraft.getInstance();
		float aspect = (float) mc.getWindow().getGuiScaledWidth() / mc.getWindow().getGuiScaledHeight();
		Matrix4f savedProj = new Matrix4f(RenderSystem.getProjectionMatrix());

		Matrix4f ortho = new Matrix4f().ortho(
				-orthoRange * aspect,
				orthoRange * aspect,
				-orthoRange,
				orthoRange,
				0.01f,
				400f
		);

		RenderSystem.setProjectionMatrix(
				ortho,
				VertexSorting.ORTHOGRAPHIC_Z
		);

		PoseStack modelView = RenderSystem.getModelViewStack();

		modelView.pushPose();
		modelView.setIdentity();

		modelView.translate(panX, panY, -200);
		modelView.mulPose(Axis.XP.rotationDegrees(rotX));
		modelView.mulPose(Axis.YP.rotationDegrees(rotY));
		modelView.translate(
				-scene.getCenterX(),
				-scene.getCenterY(),
				-scene.getCenterZ()
		);

		RenderSystem.applyModelViewMatrix();

		Lighting.setupFor3DItems();
		RenderSystem.enableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

		PoseStack blockPose = new PoseStack();

		renderBlocks(blockPose, bufferSource);

		bufferSource.endBatch();

		RenderSystem.disableBlend();

		modelView.popPose();
		RenderSystem.applyModelViewMatrix();

		RenderSystem.setProjectionMatrix(
				savedProj,
				VertexSorting.ORTHOGRAPHIC_Z
		);
	}

	public void exportToPng(Path outputPath, int resolution, float rotX, float rotY, float zoom, float panX, float panY, Consumer<Path> onSuccess, Consumer<Exception> onError) {
		int maxTex = GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE);
		int maxSize = Math.min(resolution, maxTex);
		Minecraft mc = Minecraft.getInstance();
		float aspect = (float) mc.getWindow().getGuiScaledWidth() / mc.getWindow().getGuiScaledHeight();
		int exportWidth;
		int exportHeight;

		if (aspect >= 1.0f) {
			exportWidth = maxSize;
			exportHeight = Math.max(1, Math.round(maxSize / aspect));
		} else {
			exportWidth =
					Math.max(1, Math.round(maxSize * aspect));
			exportHeight = maxSize;
		}

		TextureTarget fbo = new TextureTarget(
				exportWidth,
				exportHeight,
				true,
				Minecraft.ON_OSX
		);

		fbo.setClearColor(0f, 0f, 0f, 0f);
		fbo.clear(Minecraft.ON_OSX);
		fbo.bindWrite(true);

		RenderSystem.viewport(
				0,
				0,
				exportWidth,
				exportHeight
		);

		RenderSystem.enableBlend();

		RenderSystem.blendFuncSeparate(
				GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
				GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
		);

		RenderSystem.enableDepthTest();

		float dim = scene.getMaxDimension();

		if (dim < 1f) {
			dim = 1f;
		}

		float orthoRange = dim / zoom * 0.7f;

		Matrix4f ortho = new Matrix4f().ortho(
				-orthoRange * aspect,
				orthoRange * aspect,
				-orthoRange,
				orthoRange,
				0.01f,
				400f
		);

		RenderSystem.setProjectionMatrix(
				ortho,
				VertexSorting.ORTHOGRAPHIC_Z
		);

		PoseStack modelView = RenderSystem.getModelViewStack();

		modelView.pushPose();
		modelView.setIdentity();

		modelView.translate(panX, panY, -200);
		modelView.mulPose(Axis.XP.rotationDegrees(rotX));
		modelView.mulPose(Axis.YP.rotationDegrees(rotY));
		modelView.translate(
				-scene.getCenterX(),
				-scene.getCenterY(),
				-scene.getCenterZ()
		);

		RenderSystem.applyModelViewMatrix();

		Lighting.setupFor3DItems();

		MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

		PoseStack blockPose = new PoseStack();

		renderBlocks(blockPose, bufferSource);

		bufferSource.endBatch();

		// 读取像素（必须在渲染线程）
		NativeImage image;

		try {
			image = new NativeImage(exportWidth, exportHeight, false);
			RenderSystem.bindTexture(fbo.getColorTextureId());
			image.downloadTexture(0, false);
			image.flipY();
		} catch (OutOfMemoryError error) {
			NebulaLibs.LOGGER.error(
					"Export failed: resolution {}x{} is too large, out of memory",
					exportWidth,
					exportHeight
			);

			modelView.popPose();
			RenderSystem.applyModelViewMatrix();
			fbo.destroyBuffers();

			mc.getMainRenderTarget().bindWrite(true);

			RenderSystem.viewport(
					0,
					0,
					mc.getWindow().getWidth(),
					mc.getWindow().getHeight()
			);
			onError.accept(new RuntimeException("Resolution too large, out of memory. Try a smaller value."));
			return;
		}

		modelView.popPose();
		RenderSystem.applyModelViewMatrix();

		fbo.destroyBuffers();

		mc.getMainRenderTarget().bindWrite(true);

		RenderSystem.viewport(
				0,
				0,
				mc.getWindow().getWidth(),
				mc.getWindow().getHeight()
		);

		// 文件写入异步执行，不阻塞渲染线程，卡游戏
		CompletableFuture.runAsync(() -> {
			try {
				if (outputPath.getParent() != null) {
					outputPath.getParent().toFile().mkdirs();
				}

				image.writeToFile(outputPath);
				image.close();

				mc.execute(() -> {
					onSuccess.accept(outputPath);
				});
			} catch (Exception exception) {
				NebulaLibs.LOGGER.error("Export failed", exception);
				image.close();
				mc.execute(() -> {
					onError.accept(exception);
				});
			}
		});
	}

	private void renderBlocks(PoseStack stack, MultiBufferSource.BufferSource source) {
		Minecraft mc = Minecraft.getInstance();
		BlockRenderDispatcher dispatcher = mc.getBlockRenderer();

		for (Map.Entry<BlockPos, BlockState> entry : scene.getBlocks().entrySet()) {
			BlockPos pos = entry.getKey();
			BlockState state = entry.getValue();

			if (state.getRenderShape() == RenderShape.INVISIBLE) {
				continue;
			}

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

			modelData = bakedModel.getModelData(virtualLevel, pos, state, modelData);

			stack.pushPose();
			stack.translate(pos.getX(), pos.getY(), pos.getZ());

			dispatcher.renderSingleBlock(
					state,
					stack,
					source,
					LightTexture.FULL_BRIGHT,
					OverlayTexture.NO_OVERLAY,
					modelData,
					null
			);

			stack.popPose();
		}

		source.endBatch();

		// 液体处理
		for (BlockEntity blockEntity : virtualLevel.getRenderedBlockEntities()) {
			BlockEntityRenderer<BlockEntity> renderer = mc.getBlockEntityRenderDispatcher().getRenderer(blockEntity);
			if (renderer != null) {
				BlockPos pos = blockEntity.getBlockPos();

				stack.pushPose();
				stack.translate(pos.getX(), pos.getY(), pos.getZ());

				try {
					renderer.render(
							blockEntity,
							0f,
							stack,
							source,
							LightTexture.FULL_BRIGHT,
							OverlayTexture.NO_OVERLAY
					);
				} catch (Exception ignored) {
				}

				stack.popPose();
			}
		}

		source.endBatch();

		for (Map.Entry<BlockPos, BlockState> entry : scene.getBlocks().entrySet()) {
			BlockPos pos = entry.getKey();
			BlockState state = entry.getValue();

			FluidState fluidState = state.getFluidState();

			if (fluidState.isEmpty()) {
				continue;
			}

			VertexConsumer fluidBuffer = source.getBuffer(RenderType.translucent());

			int chunkBaseX = pos.getX() & ~15;
			int chunkBaseY = pos.getY() & ~15;
			int chunkBaseZ = pos.getZ() & ~15;

			VertexConsumer offsetBuffer = (chunkBaseX | chunkBaseY | chunkBaseZ) == 0
					? fluidBuffer
					: new OffsetVertexConsumer(fluidBuffer, chunkBaseX, chunkBaseY, chunkBaseZ);

			dispatcher.renderLiquid(
					pos,
					virtualLevel,
					offsetBuffer,
					state,
					fluidState
			);
		}

		source.endBatch(RenderType.translucent());

		// 实体渲染
		for (Entity entity : virtualLevel.getRenderedEntities()) {
			try {
				stack.pushPose();

				mc.getEntityRenderDispatcher().render(
						entity,
						entity.getX(),
						entity.getY(),
						entity.getZ(),
						entity.getYRot(),
						0f,
						stack,
						source,
						LightTexture.FULL_BRIGHT
				);
				stack.popPose();
			} catch (Exception ignored) {
			}
		}
		source.endBatch();
	}
}