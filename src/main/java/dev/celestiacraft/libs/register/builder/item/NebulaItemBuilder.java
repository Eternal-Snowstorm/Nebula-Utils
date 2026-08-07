package dev.celestiacraft.libs.register.builder.item;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import com.tterrag.registrate.providers.RegistrateProvider;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.CreativeModeTabModifier;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import dev.celestiacraft.libs.api.client.context.TooltipContext;
import dev.celestiacraft.libs.client.tooltip.NebulaTooltipHandler;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("MethodOverridesStaticMethodOfSuperclass")
public class NebulaItemBuilder<T extends Item, P> extends ItemBuilder<T, P> {
	protected NebulaItemBuilder(
			AbstractRegistrate<?> registrate,
			P parent,
			String name,
			BuilderCallback callback,
			NonNullFunction<Item.Properties, T> factory
	) {
		super(registrate, parent, name, callback, factory);
	}

	public static <T extends Item, P> NebulaItemBuilder<T, P> create(
			AbstractRegistrate<?> registrate,
			P parent,
			String name,
			BuilderCallback callback,
			NonNullFunction<Item.Properties, T> factory
	) {
		return new NebulaItemBuilder<>(registrate, parent, name, callback, factory);
	}

	@Override
	public @NotNull NebulaItemBuilder<T, P> properties(@NotNull NonNullUnaryOperator<Item.Properties> properties) {
		super.properties(properties);
		return this;
	}

	@Override
	public @NotNull NebulaItemBuilder<T, P> initialProperties(@NotNull NonNullSupplier<Item.Properties> supplier) {
		super.initialProperties(supplier);
		return this;
	}

	@Override
	public @NotNull NebulaItemBuilder<T, P> tab(@NotNull ResourceKey<CreativeModeTab> tab, @NotNull Consumer<CreativeModeTabModifier> consumer) {
		super.tab(tab, consumer);
		return this;
	}

	@Override
	public @NotNull NebulaItemBuilder<T, P> tab(@NotNull ResourceKey<CreativeModeTab> tab) {
		super.tab(tab);
		return this;
	}

	@Override
	public @NotNull NebulaItemBuilder<T, P> removeTab(@NotNull ResourceKey<CreativeModeTab> tab) {
		super.removeTab(tab);
		return this;
	}

	@Override
	public @NotNull NebulaItemBuilder<T, P> color(@NotNull NonNullSupplier<Supplier<ItemColor>> supplier) {
		super.color(supplier);
		return this;
	}

	@Override
	public @NotNull NebulaItemBuilder<T, P> defaultModel() {
		super.defaultModel();
		return this;
	}

	@Override
	public @NotNull NebulaItemBuilder<T, P> model(@NotNull NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelProvider> cons) {
		super.model(cons);
		return this;
	}

	@Override
	public @NotNull NebulaItemBuilder<T, P> defaultLang() {
		super.defaultLang();
		return this;
	}

	@Override
	public @NotNull NebulaItemBuilder<T, P> lang(@NotNull String name) {
		super.lang(name);
		return this;
	}

	@Override
	public @NotNull NebulaItemBuilder<T, P> lang(net.minecraftforge.common.util.@NotNull NonNullFunction<T, String> func) {
		super.lang(func);
		return this;
	}

	@Override
	public @NotNull NebulaItemBuilder<T, P> lang(net.minecraftforge.common.util.@NotNull NonNullFunction<T, String> func, @NotNull String name) {
		super.lang(func, name);
		return this;
	}

	@Override
	public @NotNull NebulaItemBuilder<T, P> recipe(@NotNull NonNullBiConsumer<DataGenContext<Item, T>, RegistrateRecipeProvider> cons) {
		super.recipe(cons);
		return this;
	}

	@Override
	public @NotNull NebulaItemBuilder<T, P> onRegister(@NotNull NonNullConsumer<? super T> cons) {
		super.onRegister(cons);
		return this;
	}

	@Override
	public @NotNull <OR> NebulaItemBuilder<T, P> onRegisterAfter(@NotNull ResourceKey<? extends Registry<OR>> registry, @NotNull NonNullConsumer<? super T> cons) {
		super.onRegisterAfter(registry, cons);
		return this;
	}

	@Override
	public @NotNull <D extends RegistrateProvider> NebulaItemBuilder<T, P> setData(@NotNull ProviderType<? extends D> type, @NotNull NonNullBiConsumer<DataGenContext<Item, T>, D> cons) {
		super.setData(type, cons);
		return this;
	}

	@Override
	public @NotNull <D extends RegistrateProvider> NebulaItemBuilder<T, P> addMiscData(@NotNull ProviderType<? extends D> type, @NotNull NonNullConsumer<? extends D> cons) {
		super.addMiscData(type, cons);
		return this;
	}

	public NebulaItemBuilder<T, P> tooltip(Consumer<TooltipContext> tooltip) {
		return onRegister((item) -> {
			NebulaTooltipHandler.addTooltip(item, tooltip);
		});
	}

	public NebulaItemBuilder<T, P> tooltip(Component component) {
		return tooltip((context) -> {
			context.add(component);
		});
	}

	public NebulaItemBuilder<T, P> tooltip(String text) {
		return tooltip((context) -> {
			context.add(text);
		});
	}
}