package dev.celestiacraft.libs.common.register;

import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.common.material.Material;
import net.minecraft.tags.BlockTags;

public class TestMaterial {
	public static final Material COPPER = NebulaLibs.MATERIAL.create("copper", "Copper", BlockTags.NEEDS_IRON_TOOL)
			.color(0xB87333, 0xD4956A)
			.setDestroy(3, 6)
			.isMetal()
			.ingot();

	public static void init() {
	}
}