package top.nebula.utils.compat.kubejs.event.handler;

import com.mojang.datafixers.util.Either;
import dev.latvian.mods.kubejs.fluid.FluidStackJS;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.nebula.utils.compat.kubejs.event.NebulaEvents;
import top.nebula.utils.compat.kubejs.event.function.FluidTooltipEventJS;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class FluidTooltipHandler {
	@SubscribeEvent
	public static void onGatherTooltip(RenderTooltipEvent.GatherComponents event) {
		ItemStack item = event.getItemStack();
		List<Either<FormattedText, TooltipComponent>> elements = event.getTooltipElements();

		if (!item.isEmpty()) {
			return;
		}
		if (elements.size() < 2) {
			return;
		}

		FormattedText text = elements
				.get(1)
				.left()
				.orElse(null);

		if (text == null) {
			return;
		}

		ResourceLocation id = ResourceLocation.tryParse(text.getString());
		if (id == null) {
			return;
		}
		if (!BuiltInRegistries.FLUID.containsKey(id)) {
			return;
		}

		FluidStackJS fluid = FluidStackJS.of(id);

		List<Component> additions = new ArrayList<>();

		FluidTooltipEventJS jsEvent = new FluidTooltipEventJS(fluid, additions);
		NebulaEvents.FLUID_TOOLTIP.post(jsEvent);
		jsEvent.apply();

		int insertIndex = Math.min(1, elements.size());
		for (Component component : additions) {
			elements.add(insertIndex, Either.left(component));
			insertIndex++;
		}
	}
}
