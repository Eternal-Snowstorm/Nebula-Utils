package dev.celestiacraft.libs.common.material;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.client.assets.textures.fluid.FluidTextures;
import dev.celestiacraft.libs.common.fluid.type.MoltenType;
import dev.celestiacraft.libs.tags.TagsBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraftforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.ForgeRegistries;

public class MaterialRegistrar {
	private final CreateRegistrate registrate;

	public MaterialRegistrar(CreateRegistrate registrate) {
		this.registrate = registrate;
	}

	/**
	 * 注册物品类型
	 *
	 * @param type     类型
	 * @param material 材料
	 * @return
	 */
	protected ItemBuilder<Item, CreateRegistrate> createItem(String type, Material material) {
		String registerId = String.format("%s_%s", material.name, type);
		ItemBuilder<Item, CreateRegistrate> builder = registrate.item(registerId, Item::new);

		builder.tag(TagsBuilder.item(String.format("%ss/%s", type, material.name)).forge());
		builder.tag(TagsBuilder.item(String.format("%ss", type)).forge());

		return builder;
	}

	/**
	 * 注册金属方块类型
	 *
	 * @param material 材料
	 * @return
	 */
	protected BlockBuilder<Block, CreateRegistrate> createMetalBlock(Material material) {
		return createBlock("block", material);
	}

	/**
	 * 注册粗矿方块类型
	 *
	 * @param material 材料
	 * @return
	 */
	protected BlockBuilder<Block, CreateRegistrate> createRawBlock(Material material) {
		return createBlock("raw_block", material);
	}

	/**
	 * 注册熔融金属流体
	 *
	 * @param material 材料
	 * @return
	 */
	protected FluidBuilder<ForgeFlowingFluid.Flowing, CreateRegistrate> createMoltenFluid(Material material) {
		String registerId = String.format("molten_%s", material.name);
		int color = material.color1;

		FluidBuilder<ForgeFlowingFluid.Flowing, CreateRegistrate> builder = registrate.fluid(
				registerId,
				FluidTextures.MOLTEN_STILL,
				FluidTextures.MOLTEN_FLOW,
				((properties, still, flow) -> {
					return new MoltenType(properties, color);
				})
		);

		builder.renderType(RenderType::translucent);
		builder.tag(TagsBuilder.fluid("molten_materials").forge());
		builder.tag(TagsBuilder.fluid(String.format("molten_%s", material.name)).forge());
		builder.tag(TagsBuilder.fluid(String.format("molten_%s", material.name)).tconstruct());
		builder.source(ForgeFlowingFluid.Source::new);
		builder.bucket()
				.model((context, provider) -> {
					provider.withExistingParent(context.getName(), NebulaLibs.loadForgeResource("item/bucket_drip"))
							.customLoader(DynamicFluidContainerModelBuilder::begin)
							.fluid(ForgeRegistries.FLUIDS.getValue(ResourceLocation.fromNamespaceAndPath(registrate.getModid(), registerId)));
				})
				.register();

		return builder;
	}

	/**
	 * 注册方块类型
	 *
	 * @param type     类型
	 * @param material 材料
	 * @return
	 */
	private BlockBuilder<Block, CreateRegistrate> createBlock(String type, Material material) {
		// 生成 ID 和 Tag
		String registerId;
		String tagId;

		if ("raw_block".equals(type)) {
			registerId = String.format("raw_%s_block", material.name);
			tagId = String.format("storage_blocks/raw_%s", material.name);
		} else {
			registerId = String.format("%s_%s", material.name, type);
			tagId = String.format("storage_blocks/%s", material.name);
		}

		BlockBuilder<Block, CreateRegistrate> builder = registrate.block(registerId, Block::new);
		ItemBuilder<BlockItem, BlockBuilder<Block, CreateRegistrate>> itemBuilder = builder.item();

		// Tags
		itemBuilder.tag(TagsBuilder.item(tagId).forge());
		itemBuilder.tag(TagsBuilder.item("storage_blocks").forge());

		// Block 属性
		SoundType sound = material.sound != null ?
				material.sound : ("raw_block".equals(type) ? SoundType.STONE : SoundType.METAL);

		builder.properties((properties) -> {
			return properties.strength(material.hardness, material.resistance)
					.sound(sound);
		});

		return builder;
	}
}