package dev.celestiacraft.libs.common.register;

import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.api.register.item.BasicItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class NebulaItem {
	public static final DeferredRegister<Item> ITEMS;
	public static final Supplier<BasicItem> GEOLOGICAL_HAMMER;
	public static final Supplier<BasicItem> BLOCK_ENTITY_TOOL;

	static {
		ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, NebulaLibs.MODID);

		GEOLOGICAL_HAMMER = ITEMS.register("geological_hammer", () -> {
			return new BasicItem(new Item.Properties()
					.rarity(Rarity.EPIC)
					.stacksTo(1)
			);
		});
		BLOCK_ENTITY_TOOL = ITEMS.register("block_entity_tool", () -> {
			return new BasicItem(new Item.Properties()
					.rarity(Rarity.EPIC)
					.stacksTo(1)
			);
		});
	}

	public static void register(IEventBus bus) {
		NebulaLibs.LOGGER.info("Nebula Libs Items Registered!");
		ITEMS.register(bus);
	}
}