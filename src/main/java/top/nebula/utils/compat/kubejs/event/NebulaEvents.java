package top.nebula.utils.compat.kubejs.event;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import top.nebula.utils.compat.kubejs.event.function.FluidTooltipEventJS;

public interface NebulaEvents {
	EventGroup GROUP = EventGroup.of("NebulaEvents");
	EventHandler FLUID_TOOLTIP = GROUP.client("fluidTooltip", () -> {
		return FluidTooltipEventJS.class;
	});
}