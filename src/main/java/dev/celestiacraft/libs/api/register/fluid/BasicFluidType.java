package dev.celestiacraft.libs.api.register.fluid;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;

import java.util.function.Consumer;

public abstract class BasicFluidType extends FluidType {
	public BasicFluidType(Properties properties) {
		super(properties);
	}

	public abstract ResourceLocation getFlowingTexture();

	public abstract ResourceLocation getStillTexture();

	public int getTintColor() {
		return 0xFFFFFFFF;
	}

	@Override
	public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
		consumer.accept(new BasicFluidExtensions(this));
	}
}