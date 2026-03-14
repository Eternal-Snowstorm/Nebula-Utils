package dev.celestiacraft.libs.common.register;

import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.common.material.Material;
import net.minecraft.tags.BlockTags;

public class TestMaterial {
	public static final Material MATERIAL = new Material(NebulaLibs.MODID);

	public static final Material COPPER = MATERIAL.material("copper", BlockTags.NEEDS_IRON_TOOL)
			.color(0xB87333, 0xD4956A)
			.destroy(3, 6)
			.isMetal()
			.ingot()
			.nugget()
			.plate()
			.dust()
			.rod()
			.gear()
			.wire()
			.metalBlock()
			.rawBlock()
			.molten();

	public static void init() {

	}
}