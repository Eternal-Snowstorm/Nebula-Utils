package dev.celestiacraft.libs.utils.stack;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ItemStack 相关工具类.
 *
 * <p>
 * 提供对 Ingredient 与 ItemStack 进行常用转换和扩展的辅助方法.
 * </p>
 *
 * <h2>设计目的</h2>
 * <ul>
 *     <li>避免在业务代码中重复编写 ItemStack 复制逻辑</li>
 *     <li>统一数量扩展行为</li>
 *     <li>保持原始 Ingredient 内部数据不被修改</li>
 * </ul>
 */
public class StackUtils {
	/**
	 * 将一个 Ingredient 中的所有匹配 ItemStack
	 * 扩展为指定数量的新列表.
	 *
	 * <p>
	 * 该方法会:
	 * <ul>
	 *     <li>遍历 ingredient.getItems()</li>
	 *     <li>对每个 ItemStack 进行 copy()</li>
	 *     <li>设置新的数量 count</li>
	 *     <li>返回新的 List 集合</li>
	 * </ul>
	 * </p>
	 *
	 * <p>
	 * 注意: 不会修改原 Ingredient 内部的 ItemStack.
	 * </p>
	 *
	 * @param ingredient 原始配方材料
	 * @param count 目标数量
	 * @return 新的 ItemStack 列表, 每个元素数量均为 count
	 */
	public static List<ItemStack> expandWithCount(Ingredient ingredient, int count) {
		return Arrays.stream(ingredient.getItems())
				.map((stack) -> {
					ItemStack copy = stack.copy();
					copy.setCount(count);
					return copy;
				})
				.collect(Collectors.toList());
	}
}