package dev.celestiacraft.libs.utils;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

public class TabUtils {
	public static ResourceKey<CreativeModeTab> getTabKey(ResourceLocation name) {
		return ResourceKey.create(Registries.CREATIVE_MODE_TAB, name);
	}
}