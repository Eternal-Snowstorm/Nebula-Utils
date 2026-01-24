package top.nebula.libs.compat;

import net.minecraftforge.fml.ModList;

public class ModDetector {
	public static final boolean KJS = ModList.get().isLoaded("kubejs");
}
