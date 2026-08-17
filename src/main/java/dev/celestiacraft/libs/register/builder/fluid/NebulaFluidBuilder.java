package dev.celestiacraft.libs.register.builder.fluid;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateProvider;
import com.tterrag.registrate.util.entry.FluidEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.celestiacraft.libs.api.register.fluid.BasicFluidType;
import dev.celestiacraft.libs.register.NebulaRegistrate;
import dev.celestiacraft.libs.register.builder.block.NebulaBlockBuilder;
import dev.celestiacraft.libs.register.builder.item.NebulaItemBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * 通过 {@link NebulaRegistrate} 注册流体的 Registrate builder, 直接继承 {@link FluidBuilder}.
 * <p>
 * 与包装式 builder 不同, 继承后 {@code properties}/{@code fluidProperties}/{@code lang}/{@code source}/
 * {@code block}/{@code bucket} 等链式方法都会返回 {@link NebulaFluidBuilder} 本身.
 * <p>
 * 注意: {@link FluidBuilder} 的纹理字段是 final 的(构造时定死), 所以 <b>纹理必须在创建时传入</b>,
 * 不能再像包装版那样先 {@code of()} 再 {@code flowing()/still()} 后置配置.
 * 而 {@link #tint(int)} 与 {@link #tick(FluidTickHandler)} 因为是惰性求值(register 时才创建
 * FluidType/流体), 仍然可以在创建后链式调用.
 * <p>
 * 用法:
 * <pre>{@code
 * REGISTRATE.fluid("my_fluid",
 *         ResourceLocation.fromNamespaceAndPath("nebula_libs", "block/fluid/my_fluid_still"),
 *         ResourceLocation.fromNamespaceAndPath("nebula_libs", "block/fluid/my_fluid_flow"))
 *         .tint(0xFFAA0000)
 *         .tick((level, pos, state) -> { ... })
 *         .properties(props -> props.density(1000))
 *         .register();
 * }</pre>
 * 纹理路径支持 {@code block/fluid/xxx}、{@code textures/block/fluid/xxx} 或带 {@code .png} 后缀的写法,
 * 创建时会被自动归一化.
 */
@SuppressWarnings("MethodOverridesStaticMethodOfSuperclass")
public class NebulaFluidBuilder<T extends ForgeFlowingFluid, P> extends FluidBuilder<T, P> {
	@FunctionalInterface
	public interface FluidTickHandler {
		void tick(Level level, BlockPos pos, FluidState state);
	}

	/** 存放可在构建后修改的配置, 由构造时传入的工厂惰性读取. */
	public static class FluidConfig {
		private int tintColor = 0xFFFFFFFF;
		private FluidTickHandler tickHandler;
	}

	private final FluidConfig config;
	private boolean sourceSet;

	protected NebulaFluidBuilder(
			AbstractRegistrate<?> owner,
			P parent,
			String name,
			BuilderCallback callback,
			ResourceLocation stillTexture,
			ResourceLocation flowingTexture,
			FluidBuilder.FluidTypeFactory typeFactory,
			NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory,
			FluidConfig config
	) {
		super(owner, parent, name, callback, stillTexture, flowingTexture,
				typeFactory != null ? typeFactory : (props, still, flow) -> createDefaultType(props, still, flow, config),
				props -> createFlowing(props, fluidFactory, config));
		this.config = config;
	}

	/**
	 * 与 {@link FluidBuilder#create} 等价, 但产出 {@link NebulaFluidBuilder}.
	 * <p>
	 * {@code typeFactory} 为 {@code null} 时使用默认的 {@link BasicFluidType}(纹理 + 可染色).
	 */
	public static <T extends ForgeFlowingFluid, P> NebulaFluidBuilder<T, P> create(
			AbstractRegistrate<?> owner,
			P parent,
			String name,
			BuilderCallback callback,
			ResourceLocation stillTexture,
			ResourceLocation flowingTexture,
			FluidBuilder.FluidTypeFactory typeFactory,
			NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory
	) {
		FluidConfig config = new FluidConfig();
		return new NebulaFluidBuilder<>(owner, parent, name, callback,
				normalizeTexture(stillTexture), normalizeTexture(flowingTexture), typeFactory, fluidFactory, config)
				.defaultLang().defaultSource().defaultBlock().defaultBucket();
	}

	public static NebulaFluidBuilder<ForgeFlowingFluid.Flowing, NebulaRegistrate> of(
			NebulaRegistrate registrate,
			String name,
			ResourceLocation stillTexture,
			ResourceLocation flowingTexture
	) {
		return registrate.fluid(name, stillTexture, flowingTexture);
	}

	public static NebulaFluidBuilder<ForgeFlowingFluid.Flowing, NebulaRegistrate> of(
			NebulaRegistrate registrate,
			String name,
			ResourceLocation stillTexture,
			ResourceLocation flowingTexture,
			FluidBuilder.FluidTypeFactory typeFactory
	) {
		return registrate.fluid(name, stillTexture, flowingTexture, typeFactory);
	}

	public static <T extends ForgeFlowingFluid> NebulaFluidBuilder<T, NebulaRegistrate> of(
			NebulaRegistrate registrate,
			String name,
			ResourceLocation stillTexture,
			ResourceLocation flowingTexture,
			FluidBuilder.FluidTypeFactory typeFactory,
			NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory
	) {
		return registrate.fluid(name, stillTexture, flowingTexture, typeFactory, fluidFactory);
	}

	public NebulaFluidBuilder<T, P> tint(int tintColor) {
		config.tintColor = tintColor;
		return this;
	}

	/**
	 * 给流体注册 tick 回调, 源液体和流动液体都会生效.
	 * <p>
	 * 使用该方法时不要再传自定义流体工厂, 直接用 {@link #of(NebulaRegistrate, String, ResourceLocation, ResourceLocation)}.
	 */
	public NebulaFluidBuilder<T, P> tick(FluidTickHandler handler) {
		config.tickHandler = handler;
		return this;
	}

	@Override
	public @NotNull NebulaFluidBuilder<T, P> properties(@NotNull NonNullConsumer<FluidType.Properties> cons) {
		super.properties(cons);
		return this;
	}

	@Override
	public @NotNull NebulaFluidBuilder<T, P> fluidProperties(@NotNull NonNullConsumer<ForgeFlowingFluid.Properties> cons) {
		super.fluidProperties(cons);
		return this;
	}

	@Override
	public @NotNull NebulaFluidBuilder<T, P> defaultLang() {
		super.defaultLang();
		return this;
	}

	@Override
	public @NotNull NebulaFluidBuilder<T, P> lang(@NotNull String name) {
		super.lang(name);
		return this;
	}

	@Override
	public @NotNull NebulaFluidBuilder<T, P> lang(net.minecraftforge.common.util.@NotNull NonNullFunction<T, String> langKeyProvider) {
		super.lang(langKeyProvider);
		return this;
	}

	@Override
	public @NotNull NebulaFluidBuilder<T, P> lang(net.minecraftforge.common.util.@NotNull NonNullFunction<T, String> langKeyProvider, @NotNull String name) {
		super.lang(langKeyProvider, name);
		return this;
	}

	@Override
	public @NotNull NebulaFluidBuilder<T, P> renderType(@NotNull Supplier<RenderType> layer) {
		super.renderType(layer);
		return this;
	}

	@Override
	public @NotNull NebulaFluidBuilder<T, P> defaultSource() {
		super.defaultSource();
		return this;
	}

	@Override
	public @NotNull NebulaFluidBuilder<T, P> source(@NotNull NonNullFunction<ForgeFlowingFluid.Properties, ? extends ForgeFlowingFluid> factory) {
		super.source(factory);
		sourceSet = true;
		return this;
	}

	public NebulaFluidBuilder<T, P> source() {
		FluidTickHandler handler = config.tickHandler;
		if (handler == null) {
			return source(ForgeFlowingFluid.Source::new);
		}
		return source((properties) -> new ForgeFlowingFluid.Source(properties) {
			@Override
			public void tick(@NotNull Level level, @NotNull BlockPos pos, @NotNull FluidState state) {
				super.tick(level, pos, state);
				handler.tick(level, pos, state);
			}
		});
	}

	@Override
	public @NotNull NebulaFluidBuilder<T, P> defaultBlock() {
		super.defaultBlock();
		return this;
	}

	@Override
	public @NotNull NebulaFluidBuilder<T, P> defaultBucket() {
		super.defaultBucket();
		return this;
	}

	@Override
	public @NotNull NebulaFluidBuilder<T, P> noBlock() {
		super.noBlock();
		return this;
	}

	@Override
	public @NotNull NebulaBlockBuilder<LiquidBlock, FluidBuilder<T, P>> block() {
		return (NebulaBlockBuilder<LiquidBlock, FluidBuilder<T, P>>) super.block();
	}

	@Override
	public <B extends LiquidBlock> @NotNull NebulaBlockBuilder<B, FluidBuilder<T, P>> block(
			@NotNull NonNullBiFunction<NonNullSupplier<? extends T>, BlockBehaviour.Properties, ? extends B> factory
	) {
		return (NebulaBlockBuilder<B, FluidBuilder<T, P>>) super.block(factory);
	}

	/**
	 * 自定义液块(如需要独立的方块 tick 通道), 注册行为与默认液块一致.
	 */
	public NebulaFluidBuilder<T, P> liquidBlock(NonNullBiFunction<NonNullSupplier<? extends T>, BlockBehaviour.Properties, ? extends LiquidBlock> factory) {
		block(factory).register();
		return this;
	}

	@Override
	public @NotNull NebulaItemBuilder<BucketItem, FluidBuilder<T, P>> bucket() {
		return (NebulaItemBuilder<BucketItem, FluidBuilder<T, P>>) super.bucket();
	}

	@Override
	public <I extends BucketItem> @NotNull NebulaItemBuilder<I, FluidBuilder<T, P>> bucket(
			@NotNull NonNullBiFunction<Supplier<? extends ForgeFlowingFluid>, Item.Properties, ? extends I> factory
	) {
		return (NebulaItemBuilder<I, FluidBuilder<T, P>>) super.bucket(factory);
	}

	@Override
	public @NotNull NebulaFluidBuilder<T, P> noBucket() {
		super.noBucket();
		return this;
	}

	@Override
	public @NotNull NebulaFluidBuilder<T, P> onRegister(@NotNull NonNullConsumer<? super T> cons) {
		super.onRegister(cons);
		return this;
	}

	@Override
	public <OR> @NotNull NebulaFluidBuilder<T, P> onRegisterAfter(@NotNull ResourceKey<? extends Registry<OR>> registry, @NotNull NonNullConsumer<? super T> cons) {
		super.onRegisterAfter(registry, cons);
		return this;
	}

	@Override
	public <D extends RegistrateProvider> @NotNull NebulaFluidBuilder<T, P> setData(@NotNull ProviderType<? extends D> type, @NotNull NonNullBiConsumer<DataGenContext<Fluid, T>, D> cons) {
		super.setData(type, cons);
		return this;
	}

	@Override
	public <D extends RegistrateProvider> @NotNull NebulaFluidBuilder<T, P> addMiscData(@NotNull ProviderType<? extends D> type, @NotNull NonNullConsumer<? extends D> cons) {
		super.addMiscData(type, cons);
		return this;
	}

	@Override
	public @NotNull FluidEntry<T> register() {
		if (!sourceSet) {
			source();
		}
		return super.register();
	}

	private static <T extends ForgeFlowingFluid> T createFlowing(
			ForgeFlowingFluid.Properties properties,
			NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory,
			FluidConfig config
	) {
		FluidTickHandler handler = config.tickHandler;
		if (handler == null) {
			return fluidFactory.apply(properties);
		}
		return (T) new ForgeFlowingFluid.Flowing(properties) {
			@Override
			public void tick(@NotNull Level level, @NotNull BlockPos pos, @NotNull FluidState state) {
				super.tick(level, pos, state);
				handler.tick(level, pos, state);
			}
		};
	}

	private static BasicFluidType createDefaultType(
			FluidType.Properties properties,
			ResourceLocation stillTexture,
			ResourceLocation flowingTexture,
			FluidConfig config
	) {
		return new BasicFluidType(properties) {
			@Override
			public ResourceLocation getFlowingTexture() {
				return flowingTexture;
			}

			@Override
			public ResourceLocation getStillTexture() {
				return stillTexture;
			}

			@Override
			public int getTintColor() {
				return config.tintColor;
			}
		};
	}

	private static ResourceLocation normalizeTexture(ResourceLocation texture) {
		String path = texture.getPath();
		if (path.startsWith("textures/")) {
			path = path.substring("textures/".length());
		}
		if (path.endsWith(".png")) {
			path = path.substring(0, path.length() - ".png".length());
		}
		return ResourceLocation.fromNamespaceAndPath(texture.getNamespace(), path);
	}
}