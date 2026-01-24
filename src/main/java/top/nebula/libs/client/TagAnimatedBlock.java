package top.nebula.libs.client;

import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.stream.Collectors;

public class TagAnimatedBlock {
	private static List<Block> getBlocks(TagKey<Block> tag) {
		if (Minecraft.getInstance().level == null) {
			return List.of();
		}

		return Minecraft.getInstance().level.registryAccess()
				.registryOrThrow(Registries.BLOCK)
				.getTag(tag)
				.map((block) -> {
					return block.stream()
							.map(Holder::value)
							.collect(Collectors.toList());
				})
				.orElse(List.of());
	}

	public static Block get(TagKey<Block> tag, float intervalTick) {
		List<Block> blocks = getBlocks(tag);

		if (blocks.isEmpty()) {
			return Blocks.FIRE;
		}

		float time = AnimationTickHolder.getRenderTime();
		int index = (int) ((time / intervalTick) % blocks.size());
		return blocks.get(index);
	}
}