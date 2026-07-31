package dev.celestiacraft.libs.common.fluid.type;

import dev.celestiacraft.libs.api.register.fluid.BasicFluidType;
import dev.celestiacraft.libs.client.assets.FluidTextures;
import net.minecraft.resources.ResourceLocation;

public class MoltenType extends BasicFluidType {
	private int color;

	public MoltenType(Properties properties, int color) {
		super(properties.lightLevel(10)
				.temperature(1300)
				.viscosity(6000));
		this.color = color;
	}

	@Override
	public int getTintColor() {
		return color | 0xFF000000;
	}

	@Override
	public ResourceLocation getStillTexture() {
		return FluidTextures.MOLTEN_STILL;
	}

	@Override
	public ResourceLocation getFlowingTexture() {
		return FluidTextures.MOLTEN_FLOW;
	}
}