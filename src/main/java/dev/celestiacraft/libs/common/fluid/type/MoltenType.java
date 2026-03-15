package dev.celestiacraft.libs.common.fluid.type;

import dev.celestiacraft.libs.client.assets.FluidTextures;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;

import java.util.function.Consumer;

public class MoltenType extends FluidType {
	private final int color;

	public MoltenType(Properties properties, int color) {
		super(properties.lightLevel(10)
				.temperature(1300)
				.viscosity(6000));
		this.color = color;
	}

	@Override
	public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
		consumer.accept(new IClientFluidTypeExtensions() {
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
		});
	}
}