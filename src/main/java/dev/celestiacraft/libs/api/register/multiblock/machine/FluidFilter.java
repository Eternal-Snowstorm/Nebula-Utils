package dev.celestiacraft.libs.api.register.multiblock.machine;

import net.minecraftforge.fluids.FluidStack;

@FunctionalInterface
public interface FluidFilter {
	boolean test(FluidStack stack);
}