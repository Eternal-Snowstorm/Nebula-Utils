package dev.celestiacraft.libs.event.common;

import dev.celestiacraft.libs.common.register.NebulaItem;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class AddCreativeModeTabs {
	@SubscribeEvent
	public static void buildContents(BuildCreativeModeTabContentsEvent event) {
		ResourceKey<CreativeModeTab> key = event.getTabKey();

		if (key.equals(CreativeModeTabs.OP_BLOCKS)) {
			event.accept(NebulaItem.GEOLOGICAL_HAMMER);
		}
	}
}