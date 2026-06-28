package dev.celestiacraft.libs.api.register.item.energy;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

public class EnergyItemCapabilityProvider implements ICapabilityProvider {
	private final ItemStack stack;
	private final IEnergyItem item;

	public EnergyItemCapabilityProvider(ItemStack stack, IEnergyItem item) {
		this.stack = stack;
		this.item = item;
	}

	private final LazyOptional<IEnergyStorage> energy = LazyOptional.of(() -> {
		return new IEnergyStorage() {
			@Override
			public int receiveEnergy(int maxReceive, boolean simulate) {
				return item.receiveEnergy(stack, maxReceive, simulate);
			}

			@Override
			public int extractEnergy(int maxExtract, boolean simulate) {
				return item.extractEnergy(stack, maxExtract, simulate);
			}

			@Override
			public int getEnergyStored() {
				return item.getEnergyStored(stack);
			}

			@Override
			public int getMaxEnergyStored() {
				return item.getMaxEnergyStored(stack);
			}

			@Override
			public boolean canExtract() {
				return item.getMaxExtract(stack) > 0;
			}

			@Override
			public boolean canReceive() {
				return item.getMaxReceive(stack) > 0;
			}
		};
	});

	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, Direction direction) {
		if (capability == ForgeCapabilities.ENERGY) {
			return energy.cast();
		}
		return LazyOptional.empty();
	}
}