package top.nebula.libs.compat;

import net.minecraftforge.fml.ModList;

public interface ICheckModLoaded {
	private static boolean hasMod(String modid) {
		return ModList.get().isLoaded(modid);
	}

	static boolean hasCreate() {
		return hasMod("create");
	}

	static boolean hasKubeJS() {
		return hasMod("kubejs");
	}

	static boolean hasTCon() {
		return hasMod("tconstruct");
	}
}