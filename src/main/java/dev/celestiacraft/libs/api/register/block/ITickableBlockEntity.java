package dev.celestiacraft.libs.api.register.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 可自动分流 tick 逻辑的 BlockEntity 接口.
 *
 * <p>
 * 实现该接口后, {@link IEntityBlock#getTicker(Level, BlockState, net.minecraft.world.level.block.entity.BlockEntityType)}
 * 可以自动创建 ticker, 不再需要子类手写 {@code createTickerHelper}.
 * </p>
 *
 * <p>
 * 默认 {@link #tick(Level, BlockPos, BlockState)} 会根据当前世界侧分发到
 * {@link #clientTick(Level, BlockPos, BlockState)} 或 {@link #serverTick(Level, BlockPos, BlockState)}.
 * 子类只重写自己需要的方向即可.
 * </p>
 */
public interface ITickableBlockEntity {
	/**
	 * 执行一次 tick.
	 *
	 * <p>
	 * 默认按客户端和服务端分流到对应的专用方法.
	 * </p>
	 *
	 * @param level 当前世界
	 * @param pos   方块位置
	 * @param state 方块状态
	 */
	default void tick(Level level, BlockPos pos, BlockState state) {
		if (level.isClientSide()) {
			clientTick(level, pos, state);
		} else {
			serverTick(level, pos, state);
		}
	}

	/**
	 * 客户端 tick 入口.
	 *
	 * <p>
	 * 默认实现为空.
	 * </p>
	 *
	 * @param level 当前世界
	 * @param pos   方块位置
	 * @param state 方块状态
	 */
	default void clientTick(Level level, BlockPos pos, BlockState state) {
	}

	/**
	 * 服务端 tick 入口.
	 *
	 * <p>
	 * 默认实现为空.
	 * </p>
	 *
	 * @param level 当前世界
	 * @param pos   方块位置
	 * @param state 方块状态
	 */
	default void serverTick(Level level, BlockPos pos, BlockState state) {
	}
}