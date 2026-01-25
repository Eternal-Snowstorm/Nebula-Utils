package top.nebula.libs.compat.kubejs.event;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface NebulaEvents {

	EventGroup GROUP = EventGroup.of("NebulaEvents");

	EventHandler FLUID_TOOLTIP = GROUP.client("fluidTooltip", () -> {
		return FluidTooltipEventJS.class;
	});
}