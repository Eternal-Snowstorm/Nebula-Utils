package dev.celestiacraft.libs.common.recipe.anvil_craft;

import dev.celestiacraft.libs.common.register.NebulaRecipe;
import dev.celestiacraft.libs.common.register.NebulaSerializer;
import lombok.Getter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

@Getter
public class AnvilCraftRecipe implements Recipe<Container> {
	private final ResourceLocation id;
	private final Ingredient left;
	private final Ingredient right;
	private final ItemStack result;
	private final int cost;
	private final int materialCost;

	public AnvilCraftRecipe(
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
	public boolean matches(Container inv, @NotNull Level level) {
		return left.test(inv.getItem(0))
				&& right.test(inv.getItem(1));
	}

	@Override
	public @NotNull ItemStack assemble(@NotNull Container container, @NotNull RegistryAccess access) {
		return result.copy();
	}

	@Override
	public boolean canCraftInDimensions(int pWidth, int pHeight) {
		return false;
	}

	@Override
	public @NotNull ItemStack getResultItem(@NotNull RegistryAccess access) {
		return result;
	}

	@Override
	public @NotNull ResourceLocation getId() {
		return id;
	}

	@Override
	public @NotNull RecipeSerializer<?> getSerializer() {
		return NebulaSerializer.ANVIL_CRAFT.get();
	}

	@Override
	public @NotNull RecipeType<?> getType() {
		return NebulaRecipe.ANVIL_CRAFT.get();
	}

	@Override
	public boolean isSpecial() {
		return true;
	}
}