package dev.celestiacraft.libs.compat.patchouli.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public record WorldContext(Level level, BlockPos pos) implements MultiblockContext {
	@Override
	public Level getLevel() {
		return level;
	}

	@Override
	public BlockPos getBlockPos() {
		return pos;
	}
}