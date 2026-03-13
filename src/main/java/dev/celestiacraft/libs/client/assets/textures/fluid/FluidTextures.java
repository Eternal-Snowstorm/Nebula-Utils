package dev.celestiacraft.libs.client.assets.textures.fluid;

import dev.celestiacraft.libs.NebulaLibs;
import net.minecraft.resources.ResourceLocation;

public class FluidTextures {
	public static final ResourceLocation MOLTEN_FLOW;
	public static final ResourceLocation MOLTEN_STILL;

	static {
		MOLTEN_FLOW = NebulaLibs.loadResource("fluid/molten/flow");
		MOLTEN_STILL = NebulaLibs.loadResource("fluid/molten/still");
	}
}