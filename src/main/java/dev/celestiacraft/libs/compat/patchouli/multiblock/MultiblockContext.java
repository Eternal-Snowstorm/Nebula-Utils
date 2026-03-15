package dev.celestiacraft.libs.compat.patchouli.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface MultiblockContext {
	Level getLevel();

	BlockPos getBlockPos();

	default boolean isClient() {
		Level level = getLevel();
		return level != null && level.isClientSide();
	}
}