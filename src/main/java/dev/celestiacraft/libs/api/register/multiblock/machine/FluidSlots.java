package dev.celestiacraft.libs.api.register.multiblock.machine;

import net.minecraftforge.fluids.FluidStack;

import java.util.Objects;

public record FluidSlots(int capacity, IOMode mode, FluidFilter filter) {
	public static final FluidSlots[] EMPTY = new FluidSlots[0];

	public FluidSlots(int capacity, IOMode mode) {
		this(capacity, mode, FluidFilters.any());
	}

	public FluidSlots(int capacity, IOMode mode, FluidFilter filter) {
		if (capacity <= 0) {
			throw new IllegalArgumentException("Fluid slot capacity must be greater than 0");
		}

		this.capacity = capacity;
		this.mode = Objects.requireNonNull(mode, "mode");
		this.filter = Objects.requireNonNull(filter, "filter");
	}


	public boolean accepts(FluidStack stack) {
		return stack.isEmpty() || filter.test(stack);
	}
}