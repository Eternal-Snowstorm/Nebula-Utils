package top.nebula.utils.tags.type;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import top.nebula.utils.tags.AbstractTagBuilder;

public class BlockTag extends AbstractTagBuilder<Block> {
	public BlockTag(String name) {
		super(name);
	}

	@Override
	public TagKey<Block> build() {
		return BlockTags.create(id());
	}
}