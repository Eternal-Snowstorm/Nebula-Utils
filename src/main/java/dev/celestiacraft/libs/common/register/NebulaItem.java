package dev.celestiacraft.libs.common.register;

import com.tterrag.registrate.util.entry.ItemEntry;
import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.api.register.item.BasicItem;
import net.minecraft.world.item.Rarity;

public class NebulaItem {
	public static final ItemEntry<BasicItem> GEOLOGICAL_HAMMER;
	public static final ItemEntry<BasicItem> BLOCK_ENTITY_TOOL;

	static {
		GEOLOGICAL_HAMMER = NebulaLibs.REGISTRATE.item("geological_hammer", BasicItem::new)
				.properties((properties) -> {
					return properties.rarity(Rarity.EPIC);
				})
				.register();

		BLOCK_ENTITY_TOOL = NebulaLibs.REGISTRATE.item("block_entity_tool", BasicItem::new)
				.properties((properties) -> {
					return properties.rarity(Rarity.EPIC);
				})
				.register();
	}

	public static void register() {
		NebulaLibs.LOGGER.info("Nebula Libs Items Registered!");
	}
}