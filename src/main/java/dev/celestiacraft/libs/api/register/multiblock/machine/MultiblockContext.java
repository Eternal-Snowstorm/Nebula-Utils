package dev.celestiacraft.libs.api.register.multiblock.machine;

import dev.celestiacraft.libs.api.register.multiblock.ControllerBlockEntity;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MultiblockContext<T extends ControllerBlockEntity> {
	@Getter
	private T entity;
	@Getter
	private Level level;
	@Getter
	private ItemStack stack;
	@Getter
	private BlockPos pos;
	@Getter
	private BlockState state;

	public MultiblockContext(T entity, Level level, ItemStack stack, BlockPos pos, BlockState state) {
		this.entity = entity;
		this.level = level;
		this.stack = stack;
		this.pos = pos;
		this.state = state;
	}

	public boolean isClient() {
		return level.isClientSide();
	}

	public static <T extends ControllerBlockEntity> MultiblockContext<T> of(T entity) {
		if (entity.getLevel() == null) {
			throw new IllegalStateException("Level is null when creating MultiblockContext");
		}

		return new MultiblockContext<>(
				entity,
				entity.getLevel(),
				ItemStack.EMPTY,
				entity.getBlockPos(),
				entity.getBlockState()
		);
	}
}