package dev.celestiacraft.libs.common.register;

import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.common.material.Material;
import net.minecraft.tags.BlockTags;

public class TestMaterial {
	public static final Material COPPER = NebulaLibs.MATERIAL
			.create("copper", BlockTags.NEEDS_IRON_TOOL)
			.color(0xA7A7A7, 0x121C37)
			.setDestroy(5, 5)
			.isMetal()
			.ingot(true)
			.molten();

	public static void init() {
	}
}