package dev.celestiacraft.libs.compat.kubejs.event;

import dev.celestiacraft.libs.compat.kubejs.event.client.jei.RegisterIngredientAliasesEventJS;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public class NebulaEventJS {
	private static final EventGroup GROUP;
	public static final EventHandler JEI_ALIASES_EVENT;

	static {
		GROUP = EventGroup.of("NebulaEvents");

		JEI_ALIASES_EVENT = GROUP.client("registerJeiAliases", () -> {
			return RegisterIngredientAliasesEventJS.class;
		});
	}

	public static void init() {
		GROUP.register();
	}
}
