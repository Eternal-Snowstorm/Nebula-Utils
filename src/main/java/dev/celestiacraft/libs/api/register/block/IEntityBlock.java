package dev.celestiacraft.libs.api.register.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IEntityBlock<T extends BlockEntity> extends EntityBlock {
	BlockEntityType<T> getBlockEntityType();

	Class<T> getBlockEntityClass();

	@Override
	default BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
		return getBlockEntityType().create(pos, state);
	}

	@Nullable
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
	 * 在方块实体类型匹配时创建并返回对应的 {@link BlockEntityTicker}
	 * <p>
	 * 通常用于 {@code EntityBlock#getTicker} 中,
	 * 用于安全地判断当前的方块实体类型是否正确,
	 * 只有类型一致时才会返回对应的 ticker
	 * <p>
	 * 如果 {@code actualType} 与 {@code expectedType} 不一致,
	 * 则返回 {@code null}
	 *
	 * @param actualType   Minecraft 传入的实际方块实体类型
	 * @param expectedType 期望的方块实体类型
	 * @param ticker       当类型匹配时返回的 ticker 实例
	 * @param <E>          实际的方块实体类型
	 * @param <A>          期望的方块实体类型
	 * @return 如果两个方块实体类型一致则返回对应 ticker, 否则返回 {@code null}
	 */
	@Nullable
	static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<E> createTickerHelper(BlockEntityType<E> actualType, BlockEntityType<A> expectedType, BlockEntityTicker<? super A> ticker) {
		return actualType == expectedType ? (BlockEntityTicker<E>) ticker : null;
	}

	/**
	 * 省流: 自动 {@code createTickerHelper}, 不需要在 {@code Block} 内添加
	 * <p>
	 * 返回当前方块实体对应的 {@link BlockEntityTicker}
	 * <p>
	 * 默认实现会自动为实现了 {@link ITickableBlockEntity} 的方块实体创建
	 * {@link BlockEntityTicker}, 并在每个游戏刻调用
	 * {@link ITickableBlockEntity#tick(Level, BlockPos, BlockState)}
	 * <p>
	 * 如果当前方块实体类型没有实现 {@link ITickableBlockEntity},
	 * 或 Minecraft 传入的 {@link BlockEntityType} 与
	 * {@link #getBlockEntityType()} 不一致, 则返回 {@code null},
	 * 不会执行任何 Tick 逻辑
	 * <p>
	 * 一般情况下, 实现 {@link IEntityBlock} 后无需重写此方法,
	 * 只需让对应的 {@link BlockEntity} 实现 {@link ITickableBlockEntity}
	 * 即可自动获得 Tick 支持
	 *
	 * @param level 当前世界
	 * @param state 当前方块状态
	 * @param type  Minecraft 传入的方块实体类型
	 * @param <E>   方块实体类型
	 * @return 匹配时返回对应的 {@link BlockEntityTicker}, 否则返回 {@code null}
	 */
	@Override
	@Nullable
	default <E extends BlockEntity> BlockEntityTicker<E> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<E> type) {
		if (!ITickableBlockEntity.class.isAssignableFrom(getBlockEntityClass())) {
			return null;
		}

		return createTickerHelper(type, getBlockEntityType(), (tickLevel, pos, tickState, entity) -> {
			((ITickableBlockEntity) entity).tick(tickLevel, pos, tickState, entity);
		});
	}
}