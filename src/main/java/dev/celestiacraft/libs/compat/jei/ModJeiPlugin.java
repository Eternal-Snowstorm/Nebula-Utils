package dev.celestiacraft.libs.compat.jei;

import dev.celestiacraft.libs.common.recipe.anvil_craft.AnvilCraftRecipe;
import dev.celestiacraft.libs.common.register.NebulaRecipe;
import dev.celestiacraft.libs.compat.jei.api.NebulaJeiRecipeType;
import dev.celestiacraft.libs.compat.jei.categoty.AnvilCraftCategory;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.NotNull;
import dev.celestiacraft.libs.NebulaLibs;

import java.util.List;

@JeiPlugin
public class ModJeiPlugin implements IModPlugin {
	@Override
	public @NotNull ResourceLocation getPluginUid() {
		return NebulaLibs.loadResource("jei_plugin");
	}

	@Override
	public void registerCategories(@NotNull IRecipeCategoryRegistration registration) {
		IJeiHelpers helpers = registration.getJeiHelpers();
		IGuiHelper helper = helpers.getGuiHelper();

		registration.addRecipeCategories(
				AnvilCraftCategory.builder(helper)
		);
	}

	@Override
	public void registerRecipes(@NotNull IRecipeRegistration registration) {
		RecipeManager manager = Minecraft.getInstance().level.getRecipeManager();

		List<AnvilCraftRecipe> anvilCraftRecipe = manager.getAllRecipesFor(NebulaRecipe.ANVIL_CRAFT.get());

		registration.addRecipes(NebulaJeiRecipeType.ANVIL_CRAFT, anvilCraftRecipe);
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		IJeiHelpers helpers = registration.getJeiHelpers();

		registration.addRecipeCatalyst(
				Items.ANVIL.getDefaultInstance(),
				NebulaJeiRecipeType.ANVIL_CRAFT
		);
	}
}