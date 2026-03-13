package dev.celestiacraft.libs.compat.curios;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.List;

public class CuriosUtils {
	/**
	 * 检查指定实体的 Curios 槽位中是否存在某个指定的物品,
	 *
	 * <p>该方法会通过 {@link CuriosApi#getCuriosInventory(LivingEntity)}
	 * 获取实体的 Curios 物品处理器, 并遍历其饰品栏内容,
	 * 判断是否存在与给定 {@link Item} 匹配的 {@link ItemStack}.</p>
	 *
	 * <p>如果实体没有 Curios 能力, 或未找到匹配物品, 则返回 {@code false}.</p>
	 *
	 * @param entity 要检查的生物实体(玩家或其他 LivingEntity)
	 * @param item   需要匹配的物品
	 * @return 如果实体的 Curios 栏位中包含该物品则返回 {@code true}, 否则返回 {@code false}
	 */
	public static boolean hasItem(LivingEntity entity, Item item) {
		return CuriosApi.getCuriosInventory(entity)
				.map((handler) -> {
					return handler.findFirstCurio((stack) -> {
						return stack.is(item);
					}).isPresent();
				}).orElse(false);
	}

	/**
	 * 获取指定实体的 Curios 槽位中的所有物品列表.
	 *
	 * <p>该方法会通过 {@link CuriosApi#getCuriosInventory(LivingEntity)}
	 * 获取实体的 Curios 物品处理器, 并遍历其所有饰品栏内容,
	 * 将非空物品栈添加到结果列表中.</p>
	 *
	 * <p>如果实体没有 Curios 能力, 则返回空列表.</p>
	 *
	 * @param entity 要检查的生物实体(玩家或其他 LivingEntity)
	 * @return 包含实体 Curios 槽位中所有非空物品栈的列表
	 */
	public static List<ItemStack> getAllItems(LivingEntity entity) {
		List<ItemStack> items = new ArrayList<>();

		CuriosApi.getCuriosInventory(entity)
				.ifPresent((handler) -> {
					handler.getCurios().forEach((id, stacksHandler) -> {
						int slots = stacksHandler.getSlots();

						for (int i = 0; i < slots; i++) {
							ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
							if (!stack.isEmpty()) {
								items.add(stack);
							}
						}
					});
				});
		return items;
	}
}