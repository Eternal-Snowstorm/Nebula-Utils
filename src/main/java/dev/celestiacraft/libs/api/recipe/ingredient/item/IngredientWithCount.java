package dev.celestiacraft.libs.api.recipe.ingredient.item;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class IngredientWithCount {
	private final Ingredient ingredient;
	private final int count;

	/**
	 * 把自身转换成携带 {@code count} 数量的 ItemStack 列表(保留 Ingredient 的所有可选物品).
	 * JEI 槽位可直接 {@code addItemStacks(toItemStacks())} 显示右下角数量.
	 */
	public List<ItemStack> toItemStacks() {
		List<ItemStack> stacks = new ArrayList<>();
		for (ItemStack stack : ingredient.getItems()) {
			ItemStack copy = stack.copy();
			copy.setCount(count);
			stacks.add(copy);
		}
		return stacks;
	}
}