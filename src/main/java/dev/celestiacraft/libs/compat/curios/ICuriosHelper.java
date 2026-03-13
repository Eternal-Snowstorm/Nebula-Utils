package dev.celestiacraft.libs.compat.curios;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.List;

public interface ICuriosHelper {
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
	static boolean hasItem(LivingEntity entity, Item item) {
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
	static List<ItemStack> getAllItems(LivingEntity entity) {
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

	/**
	 * Curios Tick 接口
	 *
	 * <p>
	 * 实现该接口的物品在被玩家作为 Curios 饰品佩戴时,
	 * 会按照指定的 Tick 间隔周期性调用对应的方法
	 * </p>
	 *
	 * <p>
	 * 一般情况下只需要实现
	 * {@link #curiosTick(CuriosContext)} 即可
	 * </p>
	 *
	 * <p>
	 * 如果需要访问当前饰品的 {@link ItemStack},
	 * 可以实现 {@link #curiosTick(CuriosContext)}
	 * </p>
	 *
	 * <p>
	 * Tick 调用仅在服务端执行,
	 * 因此通常不需要额外判断
	 * {@code level.isClientSide()}
	 * </p>
	 *
	 * <pre>{@code
	 * public class CobaltItem extends Item implements ICuriosHelper {
	 *     @Override
	 *     public void curiosTick(CuriosContext context) {
	 *         context.player.addEffect(new MobEffectInstance(
	 *             MobEffects.MOVEMENT_SPEED,
	 *             40,
	 *             1,
	 *             false,
	 *             false
	 *         ));
	 *     }
	 * }
	 * }</pre>
	 */
	void curiosTick(CuriosContext context);

	/**
	 * 获取 Curios Tick 间隔
	 *
	 * <p>
	 * 返回值为 Tick 数,
	 * 表示两次 {@code curiosTick} 调用之间的间隔
	 * </p>
	 *
	 * <p>
	 * 默认值为 {@code 20},
	 * 即每秒执行一次
	 * </p>
	 *
	 * <pre>{@code
	 * @Override
	 * public int tickCheck() {
	 *     // 每两秒触发一次
	 *     return 20 * 2;
	 * }
	 * }</pre>
	 *
	 * @return Tick 间隔
	 */
	default int tickCheck() {
		return 20;
	}
}