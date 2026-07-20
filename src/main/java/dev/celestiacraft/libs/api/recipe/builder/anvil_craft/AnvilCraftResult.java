package dev.celestiacraft.libs.api.recipe.builder.anvil_craft;

import com.google.gson.JsonObject;
import dev.celestiacraft.libs.common.register.NebulaSerializer;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class AnvilCraftResult implements FinishedRecipe {
	private final ResourceLocation id;
	private final Ingredient left;
	private final Ingredient right;
	private final ItemStack result;
	private final int cost;
	private final int materialCost;

	public AnvilCraftResult(
			ResourceLocation id,
			Ingredient left,
			Ingredient right,
			ItemStack result,
			int cost,
			int materialCost
	) {
		this.id = id;
		this.left = left;
		this.right = right;
		this.result = result;
		this.cost = cost;
		this.materialCost = materialCost;
	}

	@Override
	public void serializeRecipeData(JsonObject json) {
		json.add("left", left.toJson());
		json.add("right", right.toJson());

		JsonObject resultJson = new JsonObject();
		resultJson.addProperty("item", ForgeRegistries.ITEMS.getKey(result.getItem()).toString());

		if (result.getCount() != 1) {
			resultJson.addProperty("count", result.getCount());
		}

		if (result.hasTag()) {
			resultJson.addProperty("nbt", result.getTag().toString());
		}

		json.add("result", resultJson);

		if (cost != 0) {
			json.addProperty("cost", cost);
		}

		if (materialCost != 0) {
			json.addProperty("material_cost", materialCost);
		}
	}

	@Override
	public @NotNull ResourceLocation getId() {
		return id;
	}

	@Override
	public @NotNull RecipeSerializer<?> getType() {
		return NebulaSerializer.ANVIL_CRAFT.get();
	}

	@Override
	public JsonObject serializeAdvancement() {
		return null;
	}

	@Override
	public ResourceLocation getAdvancementId() {
		return null;
	}
}