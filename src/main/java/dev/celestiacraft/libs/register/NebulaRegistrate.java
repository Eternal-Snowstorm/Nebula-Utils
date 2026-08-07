package dev.celestiacraft.libs.register;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.celestiacraft.libs.register.builder.item.NebulaItemBuilder;
import dev.celestiacraft.libs.register.builder.recipe.NebulaRecipeBuilder;
import dev.celestiacraft.libs.register.builder.recipe.NebulaRecipeTypeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import org.jetbrains.annotations.NotNull;
import java.util.function.Supplier;

public class NebulaRegistrate extends AbstractRegistrate<NebulaRegistrate> {
	protected NebulaRegistrate(String modid) {
		super(modid);
	}

	public static NebulaRegistrate create(String modid) {
		NebulaRegistrate registrate = new NebulaRegistrate(modid);
		NebulaRegistrateCallback.provideRegistrate(registrate);

		return registrate;
	}

	@Override
	public @NotNull NebulaRegistrate registerEventListeners(@NotNull IEventBus bus) {
		return super.registerEventListeners(bus);
	}

	@Override
	public <T extends Item> @NotNull NebulaItemBuilder<T, NebulaRegistrate> item(@NotNull NonNullFunction<Item.Properties, T> factory) {
		return item(self(), factory);
	}

	@Override
	public <T extends Item> @NotNull NebulaItemBuilder<T, NebulaRegistrate> item(@NotNull String name, @NotNull NonNullFunction<Item.Properties, T> factory) {
		return item(self(), name, factory);
	}

	@Override
	public <T extends Item, P> @NotNull NebulaItemBuilder<T, P> item(@NotNull P parent, @NotNull NonNullFunction<Item.Properties, T> factory) {
		return item(parent, currentName(), factory);
	}

	@Override
	public <T extends Item, P> @NotNull NebulaItemBuilder<T, P> item(@NotNull P parent, @NotNull String name, @NotNull NonNullFunction<Item.Properties, T> factory) {
		return NebulaItemBuilder.create(this, parent, name, this::accept, factory);
	}

	public <T extends Recipe<?>> @NotNull NebulaRecipeBuilder<T> recipe(@NotNull String name, @NotNull NonNullSupplier<? extends RecipeSerializer<T>> serializer) {
		return new NebulaRecipeBuilder<>(this, name, serializer);
	}

	public <T extends Recipe<?>> @NotNull NebulaRecipeTypeBuilder<T> recipeType(@NotNull String name, @NotNull NonNullSupplier<RecipeType<T>> factory, @NotNull Supplier<RecipeSerializer<T>> serializer) {
		return new NebulaRecipeTypeBuilder<>(this, self(), name, this::accept, factory, serializer);
	}
}