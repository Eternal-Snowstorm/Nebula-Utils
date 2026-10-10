package dev.celestiacraft.libs.tags.common;

import dev.celestiacraft.libs.tags.TagsBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class NebulaBlockTags {
	public static final TagKey<Block>
			SMOKE_SOURCE;

	static {
		SMOKE_SOURCE = TagsBuilder.block("smoke_source").nebulaLibs();
	}
}