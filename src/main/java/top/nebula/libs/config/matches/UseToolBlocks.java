package top.nebula.libs.config.matches;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import top.nebula.libs.config.CommonConfig;

public class UseToolBlocks {
	public static boolean matches(BlockState state) {
		for (String entry : CommonConfig.MUST_USE_TOOL_BLOCKS.get()) {
			if (entry.startsWith("#")) {
				ResourceLocation id = ResourceLocation.tryParse(entry.substring(1));
				TagKey<Block> tag = null;

				if (id != null) {
					tag = TagKey.create(Registries.BLOCK, id);
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