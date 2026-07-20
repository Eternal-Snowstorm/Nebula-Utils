package dev.celestiacraft.libs.sounds;

import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

/**
 * 唱片规格数据定义.
 *
 * <p>
 * 该 record 用于封装唱片物品所需的核心参数:
 * <ul>
 *     <li>播放声音</li>
 *     <li>比较器红石信号强度</li>
 *     <li>播放时长</li>
 * </ul>
 * </p>
 *
 * <p>
 * 通过使用 Supplier<SoundEvent>,
 * 可以支持延迟注册的声音对象,
 * 适用于 Forge 注册流程.
 * </p>
 *
 * @param sound  声音事件供应器
 * @param analog 比较器输出强度
 * @param length 播放时长, 单位为秒
 */
public record RecordSpec(Supplier<SoundEvent> sound, int analog, int length) {
	/**
	 * 默认比较器输出强度.
	 */
	public static final int DEFAULT_ANALOG = 15;

	/**
	 * 创建一个使用默认红石强度的 RecordSpec.
	 *
	 * @param sound         声音事件供应器
	 * @param lengthSeconds 播放时长, 单位为秒
	 * @return RecordSpec 实例
	 */
	public static RecordSpec of(Supplier<SoundEvent> sound, int lengthSeconds) {
		return of(sound, DEFAULT_ANALOG, lengthSeconds);
	}

	public static RecordSpec of(Supplier<SoundEvent> sound, int analog, int lengthSeconds) {
		return new RecordSpec(sound, analog, lengthSeconds);
	}
}