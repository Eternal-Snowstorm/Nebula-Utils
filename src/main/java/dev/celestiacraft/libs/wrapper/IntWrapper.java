package dev.celestiacraft.libs.wrapper;

public class IntWrapper {
	private final int value;

	public IntWrapper(int value) {
		this.value = value;
	}

	public static final IntWrapper ZERO = new IntWrapper(0);
	public static final IntWrapper ONE = new IntWrapper(1);

	// 加法
	public IntWrapper plus(IntWrapper other) {
		return new IntWrapper(this.value + other.value);
	}

	// 减法
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
}