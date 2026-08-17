package dev.celestiacraft.libs.register.builder.block;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateProvider;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import dev.celestiacraft.libs.register.NebulaRegistrate;
import dev.celestiacraft.libs.register.builder.item.NebulaItemBuilder;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * 通过 {@link NebulaRegistrate} 注册 {@link Block} 的 Registrate builder.
 * <p>
 * 覆写 {@code item()} 使其返回 {@link NebulaItemBuilder}, 并让其余链式方法保持返回
 * {@link NebulaBlockBuilder}, 这样 {@code block(...).properties(...).item(...)} 也能拿到
 * {@link NebulaItemBuilder}.
 */
@SuppressWarnings("MethodOverridesStaticMethodOfSuperclass")
public class NebulaBlockBuilder<T extends Block, P> extends BlockBuilder<T, P> {
	protected NebulaBlockBuilder(
			AbstractRegistrate<?> owner,
			P parent,
			String name,
			BuilderCallback callback,
			NonNullFunction<BlockBehaviour.Properties, T> factory,
			NonNullSupplier<BlockBehaviour.Properties> initialProperties
	) {
		super(owner, parent, name, callback, factory, initialProperties);
	}

	public static <T extends Block, P> NebulaBlockBuilder<T, P> create(
			AbstractRegistrate<?> owner,
			P parent,
			String name,
			BuilderCallback callback,
			NonNullFunction<BlockBehaviour.Properties, T> factory
	) {
		NebulaBlockBuilder<T, P> builder = new NebulaBlockBuilder<>(owner, parent, name, callback, factory, BlockBehaviour.Properties::of);
		builder.defaultBlockstate();
		builder.defaultLoot();
		builder.defaultLang();
		return builder;
	}

	@Override
	public @NotNull NebulaBlockBuilder<T, P> properties(@NotNull NonNullUnaryOperator<BlockBehaviour.Properties> function) {
		super.properties(function);
		return this;
	}

	@Override
	public @NotNull NebulaBlockBuilder<T, P> initialProperties(@NotNull NonNullSupplier<? extends Block> supplier) {
		super.initialProperties(supplier);
		return this;
	}

	@Override
	@SuppressWarnings({"deprecation", "removal"})
	public @NotNull NebulaBlockBuilder<T, P> addLayer(@NotNull Supplier<Supplier<RenderType>> layer) {
		super.addLayer(layer);
		return this;
	}

	@Override
	public @NotNull NebulaBlockBuilder<T, P> simpleItem() {
		return (NebulaBlockBuilder<T, P>) item().build();
	}

	@Override
	public @NotNull NebulaBlockBuilder<T, P> color(@NotNull NonNullSupplier<Supplier<BlockColor>> supplier) {
		super.color(supplier);
		return this;
	}

	@Override
	public @NotNull NebulaBlockBuilder<T, P> defaultBlockstate() {
		super.defaultBlockstate();
		return this;
	}

	@Override
	public @NotNull NebulaBlockBuilder<T, P> blockstate(@NotNull NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> consumer) {
		super.blockstate(consumer);
		return this;
	}

	@Override
	public @NotNull NebulaBlockBuilder<T, P> defaultLang() {
		super.defaultLang();
		return this;
	}

	@Override
	public @NotNull NebulaBlockBuilder<T, P> lang(@NotNull String name) {
		super.lang(name);
		return this;
	}

	@Override
	public @NotNull NebulaBlockBuilder<T, P> defaultLoot() {
		super.defaultLoot();
		return this;
	}

	@Override
	public @NotNull NebulaBlockBuilder<T, P> loot(@NotNull NonNullBiConsumer<RegistrateBlockLootTables, T> consumer) {
		super.loot(consumer);
		return this;
	}

	@Override
	public @NotNull NebulaBlockBuilder<T, P> recipe(@NotNull NonNullBiConsumer<DataGenContext<Block, T>, RegistrateRecipeProvider> consumer) {
		super.recipe(consumer);
		return this;
	}

	@Override
	public <I extends Item> @NotNull NebulaItemBuilder<I, BlockBuilder<T, P>> item(@NotNull NonNullBiFunction<? super T, Item.Properties, ? extends I> factory) {
		return (NebulaItemBuilder<I, BlockBuilder<T, P>>) super.item(factory);
	}

	@Override
	public @NotNull NebulaItemBuilder<BlockItem, BlockBuilder<T, P>> item() {
		return (NebulaItemBuilder<BlockItem, BlockBuilder<T, P>>) super.item();
	}

	@Override
	public @NotNull NebulaBlockBuilder<T, P> lang(net.minecraftforge.common.util.@NotNull NonNullFunction<T, String> function) {
		super.lang(function);
		return this;
	}

	@Override
	public @NotNull NebulaBlockBuilder<T, P> lang(net.minecraftforge.common.util.@NotNull NonNullFunction<T, String> function, @NotNull String name) {
		super.lang(function, name);
		return this;
	}

	@Override
	public @NotNull NebulaBlockBuilder<T, P> onRegister(@NotNull NonNullConsumer<? super T> cons) {
		super.onRegister(cons);
		return this;
	}

	@Override
	public <OR> @NotNull NebulaBlockBuilder<T, P> onRegisterAfter(@NotNull ResourceKey<? extends Registry<OR>> registry, @NotNull NonNullConsumer<? super T> consumer) {
		super.onRegisterAfter(registry, consumer);
		return this;
	}

	@Override
	public <D extends RegistrateProvider> @NotNull NebulaBlockBuilder<T, P> setData(@NotNull ProviderType<? extends D> type, @NotNull NonNullBiConsumer<DataGenContext<Block, T>, D> consumer) {
		super.setData(type, consumer);
		return this;
	}

	@Override
	public <D extends RegistrateProvider> @NotNull NebulaBlockBuilder<T, P> addMiscData(@NotNull ProviderType<? extends D> type, @NotNull NonNullConsumer<? extends D> consumer) {
		super.addMiscData(type, consumer);
		return this;
	}
}