package dev.celestiacraft.libs.common.fluid;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.builders.FluidBuilder;
import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.common.material.Material;
import dev.celestiacraft.libs.tags.TagsBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Consumer;

/**
 * <h2>FluidRegistrar</h2>
 * <p>
 * 流体注册工具类, 负责创建和配置各种类型的流体.
 * </p>
 *
 * <hr>
 *
 * <h3>使用方式</h3>
 *
 * <p><b>1. 定义材料时声明熔融类型:</b></p>
 * <pre>{@code
 * new Material("tin", "iron")
 *     .color(0xD3D4D5, 0xA1A2A3)
 *     .ingot().plate().nugget().dust()
 *     .molten(); // 声明此材料需要注册熔融流体
 * }</pre>
 *
 * <p><b>2. 自动注册:</b></p>
 * <p>
 * 调用 {@code MaterialRegister.register()} 时, 会自动遍历所有 {@link Material}
 * 并对包含 {@code MOLTEN} 类型的材料调用本类的 {@link #createMoltenFluid(Material)} 方法.
 * </p>
 *
 * <p><b>3. 注册产物:</b></p>
 * <p>
 * 以材料名 {@code tin} 为例, 将自动注册以下内容:
 * </p>
 * <ul>
 *     <li>{@code nebula_libs:molten_tin} — 源流体 (Source Fluid)</li>
 *     <li>{@code nebula_libs:flowing_molten_tin} — 流动流体 (Flowing Fluid)</li>
 *     <li>{@code nebula_libs:molten_tin} — 液体方块 (LiquidBlock)</li>
 *     <li>{@code nebula_libs:molten_tin_bucket} — 桶物品 (BucketItem)</li>
 * </ul>
 *
 * <p><b>4. 自动添加的 Tags:</b></p>
 * <ul>
 *     <li>{@code forge:molten_materials}</li>
 *     <li>{@code forge:molten_tin}</li>
 *     <li>{@code tconstruct:molten_tin}</li>
 * </ul>
 *
 * <hr>
 *
 * <h3>纹理要求</h3>
 * <p>
 * 需要在资源包中提供以下灰度纹理文件 (颜色通过 {@link Material#color1} 进行染色):
 * </p>
 * <ul>
 *     <li>{@code assets/nebula_libs/textures/fluid/metal/still.png}</li>
 *     <li>{@code assets/nebula_libs/textures/fluid/metal/flow.png}</li>
 * </ul>
 *
 * @see Material#molten()
 * @see dev.celestiacraft.libs.common.material.MaterialRegister
 */
public class FluidRegistrar {
	private static final ResourceLocation STILL_TEXTURE = ResourceLocation.fromNamespaceAndPath(NebulaLibs.MODID, "fluid/metal/still");
	private static final ResourceLocation FLOW_TEXTURE = ResourceLocation.fromNamespaceAndPath(NebulaLibs.MODID, "fluid/metal/flow");

	/**
	 * 注册熔融流体
	 * <p>
	 * 创建一个带有颜色染色的熔融流体, 包含:
	 * <ul>
	 *     <li>源流体 (Source) + 流动流体 (Flowing)</li>
	 *     <li>液体方块 (LiquidBlock)</li>
	 *     <li>桶物品 (BucketItem), 使用 forge:fluid_container 模型</li>
	 *     <li>Tags: forge:molten_materials, tconstruct:molten_{name}, forge:molten_{name}</li>
	 *     <li>渲染类型: translucent</li>
	 * </ul>
	 *
	 * @param material 材料
	 * @return FluidBuilder
	 */
    public FluidBuilder<ForgeFlowingFluid.Flowing, CreateRegistrate> createMoltenFluid(Material material) {
		String name = "molten_" + material.name;
		int color = material.color1;

		return NebulaLibs.REGISTRATE
				.fluid(name, STILL_TEXTURE, FLOW_TEXTURE,
						(properties, still, flow) -> createColoredFluidType(properties, still, flow, color))
				.renderType(RenderType::translucent)
				.tag(TagsBuilder.fluid("molten_materials").forge())
				.tag(TagsBuilder.fluid("molten_" + material.name).tconstruct())
				.tag(TagsBuilder.fluid("molten_" + material.name).forge())
				.source(ForgeFlowingFluid.Source::new)
				.bucket()
					.model((ctx, prov) ->
							prov.withExistingParent(ctx.getName(), ResourceLocation.fromNamespaceAndPath("forge", "item/bucket_drip"))
									.customLoader(DynamicFluidContainerModelBuilder::begin)
									.fluid(ForgeRegistries.FLUIDS.getValue(
											ResourceLocation.fromNamespaceAndPath(NebulaLibs.MODID, name)))
									)
					.build();
	}

	/**
	 * 创建带颜色染色的 FluidType
	 * <p>
	 * 通过 {@link IClientFluidTypeExtensions} 提供:
	 * <ul>
	 *     <li>静止纹理 (Still Texture)</li>
	 *     <li>流动纹理 (Flowing Texture)</li>
	 *     <li>染色颜色 (Tint Color)</li>
	 * </ul>
	 *
	 * @param properties     FluidType 属性
	 * @param stillTexture   静止纹理
	 * @param flowingTexture 流动纹理
	 * @param color          RGB 颜色值 (不含 Alpha)
	 * @return 配置好的 FluidType
	 */
	private static FluidType createColoredFluidType(
			FluidType.Properties properties,
			ResourceLocation stillTexture,
			ResourceLocation flowingTexture,
			int color) {
		return new FluidType(properties) {
			@Override
			public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
				consumer.accept(new IClientFluidTypeExtensions() {
					@Override
					public ResourceLocation getStillTexture() {
						return stillTexture;
					}

					@Override
					public ResourceLocation getFlowingTexture() {
						return flowingTexture;
					}

					@Override
					public int getTintColor() {
						return 0xFF000000 | color;
					}
				});
			}
		};
	}
}
