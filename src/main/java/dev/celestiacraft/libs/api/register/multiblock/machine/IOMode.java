package dev.celestiacraft.libs.api.register.multiblock.machine;

public enum IOMode {
	INPUT,
	OUTPUT,
	BOTH;

	public boolean canInsert() {
		return this == INPUT || this == BOTH;
	}

	public boolean canExtract() {
		return this == OUTPUT || this == BOTH;
	}
}