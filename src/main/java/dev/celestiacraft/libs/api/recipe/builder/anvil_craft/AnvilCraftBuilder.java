package dev.celestiacraft.libs.api.recipe.builder.anvil_craft;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Objects;
import java.util.function.Consumer;

public class AnvilCraftBuilder {
	private Ingredient left;
	private Ingredient right;
	private ItemStack result = ItemStack.EMPTY;

	private int cost;
	private int materialCost;

	public static AnvilCraftBuilder builder() {
		return new AnvilCraftBuilder();
	}

	public AnvilCraftBuilder left(Ingredient left) {
		this.left = left;
		return this;
	}

	public AnvilCraftBuilder right(Ingredient right) {
		this.right = right;
		return this;
	}

	public AnvilCraftBuilder result(ItemStack result) {
		this.result = result;
		return this;
	}

	public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
		validate();

		consumer.accept(new AnvilCraftResult(
				id,
				left,
				right,
				result,
				cost,
				materialCost
		));
	}

	private void validate() {
		Objects.requireNonNull(left, "Missing left ingredient");
		Objects.requireNonNull(right, "Missing right ingredient");
		Objects.requireNonNull(result, "Missing result item");

		if (result.isEmpty()) {
			throw new IllegalStateException("Missing result");
		}
	}
}