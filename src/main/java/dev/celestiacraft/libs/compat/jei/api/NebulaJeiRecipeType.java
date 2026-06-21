package dev.celestiacraft.libs.compat.jei.api;

import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.common.recipe.anvil_craft.AnvilCraftRecipe;
import mezz.jei.api.recipe.RecipeType;

public class NebulaJeiRecipeType {
	public static final RecipeType<AnvilCraftRecipe> ANVIL_CRAFT;

	static {
		ANVIL_CRAFT = addJeiRecipeType("anvil_craft", AnvilCraftRecipe.class);
	}

	private static <T> RecipeType<T> addJeiRecipeType(String path, Class<? extends T> recipeClass) {
		return RecipeType.create(NebulaLibs.MODID, path, recipeClass);
	}
}