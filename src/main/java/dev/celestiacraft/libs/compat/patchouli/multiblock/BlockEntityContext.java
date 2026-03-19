package dev.celestiacraft.libs.compat.patchouli.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BlockEntityContext implements IMultiblockContext {
	private final BlockEntity entity;

	public BlockEntityContext(BlockEntity entity) {
		this.entity = entity;
	}

	@Override
	public Level getLevel() {
		return entity.getLevel();
	}

	@Override
	public BlockPos getBlockPos() {
		return entity.getBlockPos();
	}

	public BlockEntity getBlockEntity() {
		return entity;
	}
}