package top.nebula.libs.compat.kubejs.event;

import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.fluid.FluidStackJS;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FluidTooltipEventJS extends EventJS {
	private final FluidStackJS fluidId;
	private final List<Component> tooltip;
	private final List<Consumer<List<Component>>> queuedAdds = new ArrayList<>();

	public FluidTooltipEventJS(FluidStackJS fluid, List<Component> handler) {
		this.fluidId = fluid;
		this.tooltip = handler;
	}


	public FluidStackJS getFluid() {
		return fluidId;
	}

	/**
	 *
	 * <pre><code>
	 * event.add(Fluid.of("minecraft:lava"), (tooltip) => {
	 *     tooltip.add(Component.xxxx("aaa"))
	 * })
	 *
	 * event.add("minecraft:lava", (tooltip) => {
	 *     tooltip.add(Component.xxxx("aaa"))
	 * })
	 * </code></pre>
	 *
	 * @param fluid   传入流体
	 * @param handler 处理器
	 */
	@Info("Add tooltips to fluids in JEI or GUI\n\n为JEI和GUI中的流体添加Tooltip")
	public void add(FluidStackJS fluid, Consumer<List<Component>> handler) {
			if (fluid == null || fluidId == null) {
			return;
		}
		if (fluidId.equals(fluid)) {
			queuedAdds.add(handler);
		}
	}

	public void apply() {
				for (Consumer<List<Component>> consumer : queuedAdds) {
			consumer.accept(tooltip);
		}
	}
}