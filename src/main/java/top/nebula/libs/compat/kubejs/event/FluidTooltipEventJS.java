package top.nebula.libs.compat.kubejs.event;

import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import top.nebula.libs.event.FluidTooltipHandler;

import java.util.List;
import java.util.function.Consumer;

public class FluidTooltipEventJS extends EventJS {
	/**
	 *
	 * <pre><code>
	 * // Only Component!
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
	public void add(String fluid, Consumer<List<Component>> handler) {
		if (fluid.startsWith("#")) {
			FluidTooltipHandler.registerTag(ResourceLocation.parse(fluid.substring(1)), handler);
		} else {
			FluidTooltipHandler.registerFluid(ResourceLocation.parse(fluid), handler);
		}
	}
}