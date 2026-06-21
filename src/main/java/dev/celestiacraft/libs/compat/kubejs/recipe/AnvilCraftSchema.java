package dev.celestiacraft.libs.compat.kubejs.recipe;

import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;

public interface AnvilCraftSchema {
	RecipeKey<OutputItem> OUTPUT = ItemComponents.OUTPUT.key("result");
	RecipeKey<InputItem> LEFT = ItemComponents.INPUT.key("left");
	RecipeKey<InputItem> RIGHT = ItemComponents.INPUT.key("right");
	RecipeKey<Integer> COST = NumberComponent.INT.key("cost").optional(0);
	RecipeKey<Integer> MATERIAL_COST = NumberComponent.INT.key("material_cost").optional(0);

	RecipeSchema SCHEMA = new RecipeSchema(OUTPUT, LEFT, RIGHT, COST, MATERIAL_COST);
}