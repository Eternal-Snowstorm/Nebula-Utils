package dev.celestiacraft.libs.api.register.multiblock.machine;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

public final class FluidFilters {
	private static final FluidFilter ANY = (stack) -> {
		return true;
	};

	private FluidFilters() {
	}

	public static FluidFilter any() {
		return ANY;
	}

	public static FluidFilter only(Fluid fluid) {
		return (stack) -> {
			return !stack.isEmpty() && stack.getFluid() == fluid;
		};
	}

	public static FluidFilter tag(TagKey<Fluid> tag) {
		return (stack) -> {
			return !stack.isEmpty() && ForgeRegistries.FLUIDS.tags()
					.getTag(tag)
					.contains(stack.getFluid());
		};
	}
}