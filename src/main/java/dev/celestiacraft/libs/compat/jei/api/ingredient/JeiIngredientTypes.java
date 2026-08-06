package dev.celestiacraft.libs.compat.jei.api.ingredient;

import dev.celestiacraft.libs.api.recipe.ingredient.item.IngredientWithCount;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.registration.IModIngredientRegistration;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 把 {@link IngredientWithCount} 注册为 JEI 自定义配料类型.
 * 槽位里直接 {@code addIngredient(TYPE, input)} 即可, JEI 会用带数量渲染器绘制右下角数量,
 * 不再需要每次手动转 ItemStack.
 */
public class JeiIngredientTypes {
	public static final IIngredientType<IngredientWithCount> INGREDIENT_WITH_COUNT = () -> IngredientWithCount.class;

	private static final IIngredientHelper<IngredientWithCount> HELPER = new IngredientWithCountHelper();
	private static final IIngredientRenderer<IngredientWithCount> RENDERER = new IngredientWithCountRenderer();

	public static void register(IModIngredientRegistration registration) {
		registration.register(INGREDIENT_WITH_COUNT, List.of(), HELPER, RENDERER);
	}

	public static ItemStack firstStack(IngredientWithCount input) {
		List<ItemStack> stacks = input.toItemStacks();
		return stacks.isEmpty() ? ItemStack.EMPTY : stacks.get(0);
	}
}