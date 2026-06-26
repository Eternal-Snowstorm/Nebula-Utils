package dev.celestiacraft.libs.api.register.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public interface IEntityBlock<T extends BlockEntity> extends EntityBlock {
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

	/**
	 * 在方块实体类型匹配时创建并返回对应的 {@link BlockEntityTicker}。
	 * <p>
	 * 通常用于 {@code EntityBlock#getTicker} 中,
	 * 用于安全地判断当前的方块实体类型是否正确,
	 * 只有类型一致时才会返回对应的 ticker。
	 * <p>
	 * 如果 {@code serverType} 与 {@code clientType} 不一致,
	 * 则返回 {@code null}。
	 *
	 * @param serverType 期望的方块实体类型
	 * @param clientType Minecraft 传入的实际方块实体类型
	 * @param ticker     当类型匹配时返回的 ticker 实例
	 * @param <E>        期望的方块实体类型
	 * @param <A>        实际的方块实体类型
	 * @return 如果两个方块实体类型一致则返回对应 ticker, 否则返回 {@code null}
	 */
	static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> serverType, BlockEntityType<E> clientType, BlockEntityTicker<? super E> ticker) {
		return clientType == serverType ? (BlockEntityTicker<A>) ticker : null;
	}
}