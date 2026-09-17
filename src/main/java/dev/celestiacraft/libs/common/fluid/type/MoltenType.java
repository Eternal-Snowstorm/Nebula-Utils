package dev.celestiacraft.libs.common.fluid.type;

import dev.celestiacraft.libs.api.register.fluid.BasicFluidType;
import dev.celestiacraft.libs.client.assets.FluidTextures;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * <h2>MoltenType</h2>
 *
 * <p>
 * 熔融金属使用的流体类型.
 * </p>
 *
 * <p>
 * 默认使用 Nebula Libs 自带的熔融贴图, 也可以通过
 * {@link #MoltenType(Properties, int, ResourceLocation, ResourceLocation)}
 * 指定贴图(例如整合包自己的 {@code cmi:fluid/metal/still}).
 * </p>
 */
public class MoltenType extends BasicFluidType {
	private final int color;

	@Nullable
	private final ResourceLocation still;
	@Nullable
	private final ResourceLocation flowing;

	public MoltenType(Properties properties, int color) {
		this(properties, color, null, null);
	}

	public MoltenType(Properties properties, int color, @Nullable ResourceLocation still, @Nullable ResourceLocation flowing) {
		super(properties.lightLevel(10)
				.temperature(1300)
				.viscosity(6000));
		this.color = color;
		this.still = still;
		this.flowing = flowing;
	}

	@Override
	public int getTintColor() {
		return color | 0xFF000000;
	}

	@Override
	public ResourceLocation getStillTexture() {
		return still == null ? FluidTextures.MOLTEN_STILL : still;
	}

	@Override
	public ResourceLocation getFlowingTexture() {
		return flowing == null ? FluidTextures.MOLTEN_FLOW : flowing;
	}
}