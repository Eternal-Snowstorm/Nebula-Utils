package dev.celestiacraft.libs.common.item.energy;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 能量物品接口 (Forge Energy - Item 实现)
 *
 * <p>
 * 用于为 {@link Item} 提供基于 NBT 的能量存储能力
 * </p>
 *
 * <p>
 * 该接口提供了一套完整的默认实现:
 * </p>
 * <ul>
 *     <li>能量的 NBT 存储与读取</li>
 *     <li>充能 / 放能逻辑</li>
 *     <li>容量与 IO 限制控制</li>
 * </ul>
 *
 * <p>
 * 核心设计:
 * </p>
 * <ul>
 *     <li>所有数据均存储于 {@link ItemStack} 的 NBT 中</li>
 *     <li>不在 Item 实例中缓存任何运行时数据</li>
 *     <li>通过 default 方法实现行为复用</li>
 * </ul>
 *
 * <p>
 * 使用方式:
 * </p>
 * <pre>{@code
 * public class BatteryItem extends Item implements IEnergyItem {
 *     public BatteryItem(Properties properties) {
 *         super(properties.stacksTo(1));
 *     }
 *
 *     // 能量参数
 *     @Override
 *     public int getCapacity(ItemStack stack) {
 *         return 100000;
 *     }
 *
 *     @Override
 *     public int getMaxReceive(ItemStack stack) {
 *         return 1000;
 *     }
 *
 *     @Override
 *     public int getMaxExtract(ItemStack stack) {
 *         return 1000;
 *     }
 *
 *     // 能量条显示(桥接)
 *     @Override
 *     public boolean isBarVisible(ItemStack stack) {
 *         return isEnergyBarVisible(stack);
 *     }
 *
 *     @Override
 *     public int getBarWidth(ItemStack stack) {
 *         return getEnergyBarWidth(stack);
 *     }
 *
 *     @Override
 *     public int getBarColor(ItemStack stack) {
 *         return getEnergyBarColor(stack);
 *     }
 *
 *     // Capability 挂载(关键)
 *     @Override
 *     public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
 *         return new EnergyItemCapabilityProvider(stack, this);
 *     }
 * }
 * }</pre>
 *
 * <p>
 * 注意:
 * </p>
 * <ul>
 *     <li>ItemStack 不应堆叠(使用{@code stacksTo(1)}), 否则可能共享 NBT</li>
 *     <li>所有能量操作必须通过 stack 进行, 避免状态错乱</li>
 * </ul>
 */
public interface IEnergyItem {
	/**
	 * 获取最大能量容量
	 * <p>
	 * 决定该物品最多可存储的 FE 容量
	 * </p>
	 *
	 * @param stack 物品
	 * @return 最大能量值
	 */
	int getCapacity(ItemStack stack);

	/**
	 * 获取最大输入速率
	 * <p>
	 * 限制每次 {@link #receiveEnergy} 可接收的最大能量
	 * </p>
	 *
	 * @param stack 物品
	 * @return 最大输入速率
	 */
	int getMaxReceive(ItemStack stack);

	/**
	 * 获取最大输出速率
	 * <p>
	 * 限制每次 {@link #extractEnergy} 可提取的最大能量
	 * </p>
	 *
	 * @param stack 物品
	 * @return 最大输出速率
	 */
	int getMaxExtract(ItemStack stack);

	/**
	 * 从 NBT 获取当前能量
	 * <p>
	 * 使用键 {@code "Energy"}
	 * </p>
	 *
	 * @param stack 物品
	 * @return
	 */
	default int getEnergy(ItemStack stack) {
		return stack.getOrCreateTag().getInt("Energy");
	}

	/**
	 * 设置当前能量值
	 * <p>
	 * 自动限制不超过 {@link #getCapacity}
	 * </p>
	 *
	 * @param stack  物品
	 * @param energy 能量
	 */
	default void setEnergy(ItemStack stack, int energy) {
		stack.getOrCreateTag().putInt(
				"Energy",
				Math.min(energy, getCapacity(stack))
		);
	}

	/**
	 * 向物品中注入能量
	 *
	 * @param stack      目标物品
	 * @param maxReceive 请求输入
	 * @param simulate   是否模拟
	 * @return 实际接收的能量
	 */
	default int receiveEnergy(ItemStack stack, int maxReceive, boolean simulate) {
		int energy = getEnergy(stack);
		int received = Math.min(
				getCapacity(stack) - energy,
				Math.min(getMaxReceive(stack), maxReceive)
		);

		if (!simulate) {
			setEnergy(stack, energy + received);
		}

		return received;
	}

	/**
	 * 从物品中提取能量
	 *
	 * @param stack      目标物品
	 * @param maxExtract 请求输出
	 * @param simulate   是否模拟
	 * @return 实际提取的能量
	 */
	default int extractEnergy(ItemStack stack, int maxExtract, boolean simulate) {
		int energy = getEnergy(stack);
		int extracted = Math.min(
				energy,
				Math.min(getMaxExtract(stack), maxExtract)
		);

		if (!simulate) {
			setEnergy(stack, energy - extracted);
		}

		return extracted;
	}

	/**
	 * 获取当前存储能量
	 *
	 * @param stack 物品
	 * @return
	 */
	default int getEnergyStored(ItemStack stack) {
		return getEnergy(stack);
	}

	/**
	 * 获取最大能量存储
	 *
	 * @param stack 物品
	 * @return
	 */
	default int getMaxEnergyStored(ItemStack stack) {
		return getCapacity(stack);
	}

	/**
	 * 定义电力条宽度
	 *
	 * @param stack
	 * @return
	 */
	default int getEnergyBarWidth(ItemStack stack) {
		return Math.round(13.0F * getEnergyStored(stack) / getMaxEnergyStored(stack));
	}

	/**
	 * 是否显示电力条
	 *
	 * @param stack
	 * @return
	 */
	default boolean isEnergyBarVisible(ItemStack stack) {
		return true;
	}

	/**
	 * 定义电力条颜色
	 *
	 * @param stack
	 * @return
	 */
	default int getEnergyBarColor(ItemStack stack) {
		return 0x00FFFF;
	}

	/**
	 * 获取比例条
	 *
	 * @param stack
	 * @return
	 */
	default float getRatioBar(ItemStack stack) {
		return (float) getEnergyStored(stack) / getCapacity(stack);
	}
}