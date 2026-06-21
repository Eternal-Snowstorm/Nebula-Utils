package dev.celestiacraft.libs.common.recipe.anvil_craft;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AnvilCraftSerializer implements RecipeSerializer<AnvilCraftRecipe> {
	@Override
	public @NotNull AnvilCraftRecipe fromJson(@NotNull ResourceLocation location, @NotNull JsonObject object) {
		Ingredient left = Ingredient.fromJson(object.get("left"));
		Ingredient right = Ingredient.fromJson(object.get("right"));

		ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(
				object,
				"result"
		));

		int cost = GsonHelper.getAsInt(object, "cost", 0);
		int materialCost = GsonHelper.getAsInt(object, "material_cost", 0);

		return new AnvilCraftRecipe(location, left, right, result, cost, materialCost);
	}

	@Override
	public @Nullable AnvilCraftRecipe fromNetwork(@NotNull ResourceLocation id, @NotNull FriendlyByteBuf buf) {
		Ingredient left = Ingredient.fromNetwork(buf);
		Ingredient right = Ingredient.fromNetwork(buf);

		ItemStack result = buf.readItem();

		int cost = buf.readVarInt();
		int materialCost = buf.readVarInt();

		return new AnvilCraftRecipe(id, left, right, result, cost, materialCost);
	}

	@Override
	public void toNetwork(@NotNull FriendlyByteBuf buf, @NotNull AnvilCraftRecipe recipe) {
		recipe.getLeft().toNetwork(buf);
		recipe.getRight().toNetwork(buf);

		buf.writeItem(recipe.getResult());

		buf.writeVarInt(recipe.getCost());
		buf.writeVarInt(recipe.getMaterialCost());
	}
}