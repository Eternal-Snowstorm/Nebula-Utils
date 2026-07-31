package dev.celestiacraft.libs.api.register.fluid;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

public class BasicFluidExtensions implements IClientFluidTypeExtensions {
	public BasicFluidType type;

	public BasicFluidExtensions(BasicFluidType type) {
		this.type = type;
	}

	@Override
	public ResourceLocation getFlowingTexture() {
		return type.getFlowingTexture();
	}

	@Override
	public ResourceLocation getStillTexture() {
		return type.getStillTexture();
	}

	@Override
	public int getTintColor() {
		return type.getTintColor();
	}
}