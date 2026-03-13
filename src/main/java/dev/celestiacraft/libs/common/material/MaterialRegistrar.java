package dev.celestiacraft.libs.common.material;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.tags.TagsBuilder;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

public class MaterialRegistrar {
	/**
	 * 注册物品类型
	 *
	 * @param type     物品类型
	 * @param material 材料
	 * @return
	 */
	protected ItemBuilder<Item, CreateRegistrate> createItem(String type, Material material) {
		String registerId = String.format("%s_%s", type, material.name);

		ItemBuilder<Item, CreateRegistrate> builder = NebulaLibs.REGISTRATE.item(registerId, Item::new);

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
	 * 注册方块类型
	 *
	 * @param type     方块类型
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
			registerId = String.format("%s_%s", type, material.name);
			tagId = String.format("storage_blocks/%s", material.name);
		}

		BlockBuilder<Block, CreateRegistrate> builder = NebulaLibs.REGISTRATE.block(registerId, Block::new);
		ItemBuilder<BlockItem, BlockBuilder<Block, CreateRegistrate>> itemBuilder = builder.item();

		// Tags
		itemBuilder.tag(TagsBuilder.item(tagId).forge());
		itemBuilder.tag(TagsBuilder.item("storage_blocks").forge());

		// Block 属性
		SoundType sound = material.sound != null ?
				material.sound : ("raw_block".equals(type) ?
				SoundType.STONE : SoundType.METAL);

		builder.properties((props) -> props.strength(material.hardness, material.resistance).sound(sound));

		return builder;
	}
}