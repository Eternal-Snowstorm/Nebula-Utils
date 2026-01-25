package top.nebula.libs.event;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class FluidTooltipHandler {
	private static final Map<ResourceLocation, List<Consumer<List<Component>>>> FLUID_HANDLERS = new HashMap<>();
	private static final Map<ResourceLocation, List<Consumer<List<Component>>>> TAG_HANDLERS = new HashMap<>();

	private FluidTooltipHandler() {
	}

	public static void registerFluid(ResourceLocation id, Consumer<List<Component>> handler) {
		FLUID_HANDLERS.computeIfAbsent(id, (location) -> {
			return new ArrayList<>();
		}).add(handler);
	}

	public static void registerTag(ResourceLocation tag, Consumer<List<Component>> handler) {
		TAG_HANDLERS.computeIfAbsent(tag, (location) -> {
			return new ArrayList<>();
		}).add(handler);
	}

	public static void fire(FluidStack fluid, List<Component> tooltip) {
		if (fluid.isEmpty()) {
			return;
		}

		ResourceLocation fluidId = ForgeRegistries.FLUIDS.getKey(fluid.getFluid());
		if (fluidId == null) {
			return;
		}

		List<Consumer<List<Component>>> list = FLUID_HANDLERS.get(fluidId);
		if (list != null) {
			list.forEach((handler) -> {
				handler.accept(tooltip);
			});
		}

		fluid.getFluid()
				.builtInRegistryHolder()
				.tags()
				.forEach((tag) -> {
					List<Consumer<List<Component>>> handlers = TAG_HANDLERS.get(tag.location());
					if (handlers != null) {
						handlers.forEach((handler) -> {
							handler.accept(tooltip);
						});
					}
				});
	}
}