package dev.celestiacraft.libs.api.register.multiblock.machine;

import java.util.Objects;

public record WorkConditionResult(boolean pass, String reasonKey) {
	private static final WorkConditionResult PASS = new WorkConditionResult(true, "");

	public WorkConditionResult {
		reasonKey = Objects.requireNonNull(reasonKey, "reasonKey");
	}

	public static WorkConditionResult success() {
		return PASS;
	}

	public static WorkConditionResult fail(String reasonKey) {
		return new WorkConditionResult(false, reasonKey);
	}

	public boolean hasReason() {
		return !reasonKey.isEmpty();
	}
}