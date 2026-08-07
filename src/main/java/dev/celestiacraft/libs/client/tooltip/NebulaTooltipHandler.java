package dev.celestiacraft.libs.client.tooltip;

import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.api.client.context.TooltipContext;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * {@code NebulaItemBuilder#tooltip} 的运行时入口.
 * <p>
 * 通过原版 {@link ItemTooltipEvent} 挂接, 因此对任意 {@link Item} (包括原版物品) 都生效,
 * 不要求物品继承 BasicItem/BasicBlockItem.
 */
@Mod.EventBusSubscriber(modid = NebulaLibs.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class NebulaTooltipHandler {
	private static final Map<Item, List<Consumer<TooltipContext>>> TOOLTIPS = new HashMap<>();

	private NebulaTooltipHandler() {
	}

	public static void addTooltip(Item item, Consumer<TooltipContext> tooltip) {
		TOOLTIPS.computeIfAbsent(item, key -> new ArrayList<>()).add(tooltip);
	}

	@SubscribeEvent
	public static void onItemTooltip(ItemTooltipEvent event) {
		List<Consumer<TooltipContext>> callbacks = TOOLTIPS.get(event.getItemStack().getItem());
		if (callbacks == null || callbacks.isEmpty()) {
			return;
		}

		TooltipContext context = new TooltipContext(
				event.getItemStack(),
				event.getEntity() != null ? event.getEntity().level() : null,
				event.getToolTip(),
				event.getFlags(),
				event.getEntity()
		);

		callbacks.forEach(callback -> callback.accept(context));
	}
}