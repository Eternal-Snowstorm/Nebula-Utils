package dev.celestiacraft.libs.api.register.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public interface IBasicEntityBlock<T extends BlockEntity> extends EntityBlock {
	BlockEntityType<? extends T> getBlockEntityType();

	Class<T> getBlockEntityClass();

	@Override
	default BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
		return getBlockEntityType().create(pos, state);
	}

	default T getBlockEntity(BlockGetter getter, BlockPos pos) {
		BlockEntity blockEntity = getter.getBlockEntity(pos);
		Class<T> expectedClass = getBlockEntityClass();

		if (blockEntity == null) {
			return null;
		}
		if (!expectedClass.isInstance(blockEntity)) {
			return null;
		}

		return (T) blockEntity;
	}
}