package dev.celestiacraft.libs.api.register.fluid;

import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import org.jetbrains.annotations.NotNull;

public class BasicFluid extends ForgeFlowingFluid {
	protected BasicFluid(Properties properties) {
		super(properties);
	}

	@Override
	public boolean isSource(@NotNull FluidState state) {
		return true;
	}

	@Override
	public int getAmount(@NotNull FluidState state) {
		return 1000;
	}
}