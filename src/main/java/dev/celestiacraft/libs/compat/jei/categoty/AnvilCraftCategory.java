package dev.celestiacraft.libs.compat.jei.categoty;

import dev.celestiacraft.libs.client.NebulaLang;
import dev.celestiacraft.libs.common.recipe.anvil_craft.AnvilCraftRecipe;
import dev.celestiacraft.libs.compat.jei.api.NebulaJeiRecipeType;
import dev.celestiacraft.libs.compat.jei.api.SimpleJeiCategory;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public class AnvilCraftCategory {
	public static SimpleJeiCategory<AnvilCraftRecipe> builder(IGuiHelper helper) {
		return SimpleJeiCategory.builder(NebulaJeiRecipeType.ANVIL_CRAFT, helper)
				.setTitle(NebulaLang.JeiLang.setTranCategoryTitle("anvil_craft"))
				.setSize(125, 48)
				.setIcon(Items.ANVIL.getDefaultInstance())
				.setBackground(0, 0)
				.setRecipe((builder, recipe, group) -> {
					builder.addInputSlot(1, 1)
							.addIngredients(recipe.getLeft())
							.setStandardSlotBackground();

					builder.addInputSlot(50, 1)
							.addIngredients(recipe.getRight())
							.setStandardSlotBackground();

					builder.addOutputSlot(108, 1)
							.addItemStack(recipe.getResult())
							.setStandardSlotBackground();
				})
				.setExtra((builder, recipe, group) -> {
					builder.addRecipePlusSign().setPosition(27, 3);
					builder.addRecipeArrow().setPosition(76, 1);

					builder.addText(Component.literal(
									"Debug Info"
							), 120, 10
					).setPosition(2, 27);

					builder.addText(Component.translatable(
									"container.repair.cost",
									10
							), 120, 10
					).setPosition(2, 27);
					/*
					MutableComponent text = Component.empty();

					if (recipe.getCost() > 0) {
						text.append(Component.translatable(
								"container.repair.cost",
								recipe.getCost()
						));
					}

					if (recipe.getMaterialCost() > 0) {
						if (!text.getString().isEmpty()) {
							text.append("  ");
						}

						text.append(Component.translatable(
								"jei.category.anvil_craft.material_cost",
								recipe.getMaterialCost()
						));
					}

					if (!text.getString().isEmpty()) {
						builder.addText(text, 120, 10)
								.setPosition(2, 27);
					}
					 */
				})
				.build();
	}
}