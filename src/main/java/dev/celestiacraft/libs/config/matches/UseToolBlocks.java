package dev.celestiacraft.libs.config.matches;

import dev.celestiacraft.libs.config.common.CommonConfigs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public class UseToolBlocks {
	public static boolean matches(BlockState state) {
		for (String entry : CommonConfigs.MUST_USE_TOOL_BLOCKS.get()) {
			if (!entry.isEmpty() && entry.charAt(0) == '#') {
				ResourceLocation id = ResourceLocation.tryParse(entry.substring(1));
				TagKey<Block> tag = null;

				if (id != null) {
					tag = BlockTags.create(id);
				}

				if (tag != null && state.is(tag)) {
					return true;
				}
			} else {
				ResourceLocation id = ResourceLocation.tryParse(entry);
				Block block = ForgeRegistries.BLOCKS.getValue(id);

				if (block != null && state.is(block)) {
					return true;
				}
			}
		}

		return false;
	}
}