package dev.celestiacraft.libs.api.register.item;

import dev.celestiacraft.libs.sounds.RecordSpec;
import net.minecraft.world.item.RecordItem;

/**
 * 自定义唱片物品实现.
 *
 * <p>
 * 该类对 {@link RecordItem} 进行了轻量封装,
 * 用于配合 {@link RecordSpec} 简化唱片物品的注册流程.
 * </p>
 *
 * <p>
 * 与原版 RecordItem 不同之处在于:
 * <ul>
 *     <li>自动限制最大堆叠数量为 1</li>
 *     <li>通过 RecordSpec 统一管理声音, 红石信号强度, 播放时长</li>
 * </ul>
 * </p>
 *
 * <p>
 * 适用于需要批量注册唱片物品的场景,
 * 避免在构造器中重复传入多个参数.
 * </p>
 */
public class BasicRecordItem extends RecordItem {
	/**
	 * 应用默认属性.
	 *
	 * <p>
	 * 当前会强制设置 stacksTo(1),
	 * 确保唱片物品不可堆叠.
	 * </p>
	 *
	 * @param properties 原始属性
	 * @return 修改后的属性
	 */
	private static Properties applyProperties(Properties properties) {
		return properties.stacksTo(1);
	}

	/**
	 * 构造一个自定义唱片物品.
	 *
	 * @param spec 唱片规格定义
	 * @param properties 物品属性
	 */
	public BasicRecordItem(RecordSpec spec, Properties properties) {
		super(spec.analog(), spec.sound(), applyProperties(properties), spec.length());
	}
}