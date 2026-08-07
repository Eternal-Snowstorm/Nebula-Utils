package dev.celestiacraft.libs.api.register.recipe;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class RecipeEntry<T extends Recipe<?>> extends RegistryEntry<RecipeType<T>> {
	private final ResourceLocation name;
	private final Supplier<RecipeSerializer<T>> serializer;

	public RecipeEntry(
			AbstractRegistrate<?> owner,
			ResourceLocation name,
			RegistryObject<RecipeType<T>> type,
			Supplier<RecipeSerializer<T>> serializer
	) {
		super(owner, type);
		this.name = name;
		this.serializer = serializer;
	}

	public RecipeType<T> getRecipeType() {
		return get();
	}

	public RecipeSerializer<T> getSerializer() {
		return serializer.get();
	}

	public Supplier<RecipeType<T>> typeHolder() {
		return this;
	}

	public Supplier<RecipeSerializer<T>> serializerHolder() {
		return serializer;
	}

	public String getName() {
		return name.toString();
	}
}