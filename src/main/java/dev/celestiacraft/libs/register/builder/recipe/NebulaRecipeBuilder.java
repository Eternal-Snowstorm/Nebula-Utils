package dev.celestiacraft.libs.register.builder.recipe;

import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.celestiacraft.libs.api.register.recipe.RecipeEntry;
import dev.celestiacraft.libs.register.NebulaRegistrate;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * 通过 {@link NebulaRegistrate} 同时注册 RecipeType 和 RecipeSerializer 的 builder.
 * <p>
 * 用法: {@code REGISTRATE.recipe("crushing", CrushingRecipeSerializer::new).register()}
 * 返回同时持有 type 与 serializer 的 {@link RecipeEntry}.
 */
public class NebulaRecipeBuilder<T extends Recipe<?>> {
	private final NebulaRegistrate registrate;
	private final String name;
	private final NonNullSupplier<? extends RecipeSerializer<T>> serializerFactory;

	public NebulaRecipeBuilder(
			NebulaRegistrate registrate,
			String name,
			NonNullSupplier<? extends RecipeSerializer<T>> serializerFactory
	) {
		this.registrate = registrate;
		this.name = name;
		this.serializerFactory = serializerFactory;
	}

	public RecipeEntry<T> register() {
		ResourceLocation id = ResourceLocation.fromNamespaceAndPath(registrate.getModid(), name);

		RegistryEntry<RecipeSerializer<T>> serializer = registrate
				.<RecipeSerializer<?>, RecipeSerializer<T>>generic(name, Registries.RECIPE_SERIALIZER, () -> serializerFactory.get())
				.register();

		return registrate.recipeType(name, () -> new RecipeType<>() {
				@Override
				public String toString() {
					return id.toString();
				}
		}, serializer).register();
	}
}