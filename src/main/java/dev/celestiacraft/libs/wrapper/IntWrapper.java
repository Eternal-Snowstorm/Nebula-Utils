package dev.celestiacraft.libs.wrapper;

import dev.latvian.mods.kubejs.typings.Info;

public class IntWrapper {
	private final int value;

	public IntWrapper(int value) {
		this.value = value;
	}

	public static final IntWrapper ZERO = new IntWrapper(0);
	public static final IntWrapper ONE = new IntWrapper(1);

	/**
	 * 加法
	 *
	 * @param other
	 * @return
	 */
	public IntWrapper plus(IntWrapper other) {
		return new IntWrapper(this.value + other.value);
	}

	/**
	 * 减法
	 *
	 * @param other
	 * @return
	 */
	public IntWrapper minus(IntWrapper other) {
		return new IntWrapper(this.value - other.value);
	}

	public boolean isZero() {
		return value == 0;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

	public int intValue() {
		return value;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null || getClass() != object.getClass()) {
			return false;
		}
		IntWrapper that = (IntWrapper) object;
		return value == that.value;
	}

	@Override
	public int hashCode() {
		return value;
	}

	/**
	 * 获取 int 的最大值
	 *
	 * @return
	 */
	@Info("获取 int 的最大值")
	public static int getMaxValue() {
		return Integer.MAX_VALUE;
	}

	/**
	 * 获取 int 的最小值
	 *
	 * @return
	 */
	@Info("获取 int 的最小值")
	public static int getMinValue() {
		return Integer.MIN_VALUE;
	}
}