package top.nebula.libs.event;

import com.mojang.datafixers.util.Either;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FluidTooltipHandler {
	private static final Map<ResourceLocation, List<Consumer<List<Component>>>> FLUID_HANDLERS = new HashMap<>();
	private static final Map<ResourceLocation, List<Consumer<List<Component>>>> TAG_HANDLERS = new HashMap<>();

	public static void registerFluid(ResourceLocation fluidId, Consumer<List<Component>> handler) {
		FLUID_HANDLERS.computeIfAbsent(fluidId, location -> new ArrayList<>())
				.add(handler);
	}

	public static void registerTag(ResourceLocation fluidTag, Consumer<List<Component>> handler) {
		TAG_HANDLERS.computeIfAbsent(fluidTag, location -> new ArrayList<>())
				.add(handler);
	}


	@SubscribeEvent
	public static void onGatherTooltip(RenderTooltipEvent.GatherComponents event) {

		ItemStack stack = event.getItemStack();
		if (stack.isEmpty()) return;

		FluidUtil.getFluidContained(stack).ifPresent(fluid -> {

			List<Component> tooltip = new ArrayList<>();

			for (var e : event.getTooltipElements()) {
				if (e.left().isPresent()) {
					tooltip.add(Component.literal(e.left().get().getString()));
				}
			}

			fire(fluid, tooltip);

			for (Component c : tooltip) {
				event.getTooltipElements().add(Either.left(c));
			}
		});
	}

	private static void fire(FluidStack fluid, List<Component> tooltip) {
		if (fluid.isEmpty()) return;

		ResourceLocation id = ForgeRegistries.FLUIDS.getKey(fluid.getFluid());
		if (id == null) return;

		var list = FLUID_HANDLERS.get(id);
		if (list != null) {
			list.forEach(h -> h.accept(tooltip));
		}

		fluid.getFluid().builtInRegistryHolder().tags().forEach(tag -> {
			var handlers = TAG_HANDLERS.get(tag.location());
			if (handlers != null) {
				handlers.forEach(h -> h.accept(tooltip));
			}
		});
	}
}