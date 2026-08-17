package dev.celestiacraft.libs.register.builder.recipe;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.builders.NoConfigBuilder;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.celestiacraft.libs.api.register.recipe.RecipeEntry;
import dev.celestiacraft.libs.register.NebulaRegistrate;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * 注册 {@link RecipeType} 的 Registrate builder, {@link #register()} 返回真正的 {@link RecipeEntry}.
 * <p>
 * 与 {@code ItemBuilder -> ItemEntry} 同构: 覆写 {@code createEntryWrapper} 产出 {@link RecipeEntry}.
 */
public class NebulaRecipeTypeBuilder<T extends Recipe<?>> extends NoConfigBuilder<RecipeType<?>, RecipeType<T>, NebulaRegistrate> {
	private final Supplier<RecipeSerializer<T>> serializer;

	public NebulaRecipeTypeBuilder(
			AbstractRegistrate<?> registrate,
			NebulaRegistrate parent,
			String name,
			BuilderCallback callback,
			NonNullSupplier<RecipeType<T>> factory,
			Supplier<RecipeSerializer<T>> serializer
	) {
		super(registrate, parent, name, callback, Registries.RECIPE_TYPE, factory);
		this.serializer = serializer;
	}

	@Override
	protected @NotNull RecipeEntry<T> createEntryWrapper(@NotNull RegistryObject<RecipeType<T>> type) {
		return new RecipeEntry<>(
				getOwner(),
				ResourceLocation.fromNamespaceAndPath(getOwner().getModid(), getName()),
				type,
				serializer
		);
	}

	@Override
	public @NotNull RecipeEntry<T> register() {
		return (RecipeEntry<T>) super.register();
	}
}