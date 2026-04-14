package dev.celestiacraft.libs.common.recipe.machine;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class MachineRecipeSerializer implements RecipeSerializer<MachineRecipe> {
	private final ResourceLocation recipeTypeId;

	public MachineRecipeSerializer(ResourceLocation recipeTypeId) {
		this.recipeTypeId = recipeTypeId;
	}

	@Override
	public @NotNull MachineRecipe fromJson(@NotNull ResourceLocation id, @NotNull JsonObject json) {
		return MachineRecipe.fromJson(id, recipeTypeId, json);
	}

	@Override
	public MachineRecipe fromNetwork(@NotNull ResourceLocation id, @NotNull FriendlyByteBuf buf) {
		return MachineRecipe.fromNetwork(id, recipeTypeId, buf);
	}

	@Override
	public void toNetwork(@NotNull FriendlyByteBuf buf, MachineRecipe recipe) {
		recipe.toNetwork(buf);
	}
}
