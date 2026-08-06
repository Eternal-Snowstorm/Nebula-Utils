package dev.celestiacraft.libs.compat.jei.api.ingredient;

import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.api.recipe.ingredient.item.IngredientWithCount;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class IngredientWithCountHelper implements IIngredientHelper<IngredientWithCount> {
	@Override
	public @NotNull IIngredientType<IngredientWithCount> getIngredientType() {
		return JeiIngredientTypes.INGREDIENT_WITH_COUNT;
	}

	@Override
	public @NotNull String getDisplayName(@NotNull IngredientWithCount ingredient) {
		ItemStack stack = JeiIngredientTypes.firstStack(ingredient);
		return stack.isEmpty() ? "Empty" : stack.getHoverName().getString();
	}

	@Override
	public @NotNull String getUniqueId(@NotNull IngredientWithCount ingredient, @NotNull UidContext context) {
		return getResourceLocation(ingredient) + "@" + ingredient.getCount();
	}

	@Override
	public @NotNull ResourceLocation getResourceLocation(@NotNull IngredientWithCount ingredient) {
		ItemStack stack = JeiIngredientTypes.firstStack(ingredient);
		ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
		return key != null ? key : NebulaLibs.loadResource("unknown");
	}

	@Override
	public @NotNull IngredientWithCount copyIngredient(IngredientWithCount ingredient) {
		return new IngredientWithCount(ingredient.getIngredient(), ingredient.getCount());
	}

	@Override
	public @NotNull String getErrorInfo(IngredientWithCount ingredient) {
		return ingredient == null ? "null" : ingredient.toString();
	}
}