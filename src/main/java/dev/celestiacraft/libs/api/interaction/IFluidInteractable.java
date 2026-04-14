package dev.celestiacraft.libs.api.interaction;

import dev.celestiacraft.libs.api.register.block.BasicBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidUtil;
import org.jetbrains.annotations.NotNull;

/**
 * 流体交互接口
 *
 * <p>
 * 为方块提供基于 Forge {@link FluidUtil} 的通用流体交互能力,
 * 例如桶, 流体容器与方块内部 Tank 的交互
 * </p>
 *
 * <p>
 * 该接口<strong>不会自动生效</strong>, 实现类必须在 {@link #use(BlockState, Level, BlockPos, Player, InteractionHand, BlockHitResult)}
 * 中调用 {@link #tryFluidInteraction(Player, InteractionHand, Level, BlockPos, BlockHitResult)},
 * 否则流体交互逻辑不会被执行
 * </p>
 *
 * <p>
 * 推荐 Block 类直接继承已有实现(如 {@link BasicBlock}), 或按如下方式手动接入:
 * </p>
 *
 * <pre>{@code
 * @Override
 * public InteractionResult use(
 *     BlockState state,
 *     Level level,
 *     BlockPos pos,
 *     Player player,
 *     InteractionHand hand,
 *     BlockHitResult hit
 * ) {
 *     // 先尝试流体交互
 *     InteractionResult result = tryFluidInteraction(player, hand, level, pos, hit);
 *     if (result.consumesAction()) {
 *         return result;
 *     }
 *
 *     // 其他自定义逻辑
 *     return InteractionResult.PASS;
 * }
 * }</pre>
 *
 * <p>
 * 流体交互行为由以下方法控制:
 * </p>
 * <ul>
 *     <li>{@link #useFluidInteraction()}: 是否始终允许交互</li>
 *     <li>{@link #creativeUseFluidInteraction()}: 是否仅创造模式允许</li>
 *     <li>{@link #canUseFluidInteraction(Player)}: 最终权限判断</li>
 * </ul>
 */
public interface IFluidInteractable {
	/**
	 * 方块右键交互入口
	 *
	 * <p>
	 * <strong>必须由实现类重写</strong>, 否则该接口不会产生任何实际效果
	 * </p>
	 *
	 * <p>
	 * 通常需要在此方法中调用 {@link #tryFluidInteraction(Player, InteractionHand, Level, BlockPos, BlockHitResult)}
	 * 以启用流体交互逻辑
	 * </p>
	 *
	 * @param state  当前方块状态
	 * @param level  世界
	 * @param pos    方块位置
	 * @param player 玩家
	 * @param hand   使用的手
	 * @param result 命中结果
	 * @return 交互结果
	 */
	InteractionResult use(
			@NotNull BlockState state,
			@NotNull Level level,
			@NotNull BlockPos pos,
			@NotNull Player player,
			@NotNull InteractionHand hand,
			@NotNull BlockHitResult result
	);

	/**
	 * 无条件允许流体交互
	 *
	 * <p>
	 * 返回 {@code true} 时, 所有玩家均可进行流体交互
	 * </p>
	 *
	 * @return 是否启用
	 */
	default boolean useFluidInteraction() {
		return false;
	}

	/**
	 * 仅创造模式允许流体交互
	 *
	 * <p>
	 * 仅当玩家为创造模式时允许交互
	 * </p>
	 *
	 * @return 是否启用
	 */
	default boolean creativeUseFluidInteraction() {
		return false;
	}

	/**
	 * 判断玩家是否可以进行流体交互
	 *
	 * <p>
	 * 默认逻辑:
	 * </p>
	 * <ul>
	 *     <li>{@link #useFluidInteraction()} 为 true → 允许</li>
	 *     <li>玩家为创造模式 且 {@link #creativeUseFluidInteraction()} 为 true → 允许</li>
	 * </ul>
	 *
	 * @param player 玩家
	 * @return 是否允许交互
	 */
	default boolean canUseFluidInteraction(Player player) {
		if (player == null) {
			return false;
		}

		return useFluidInteraction() || (player.isCreative() && creativeUseFluidInteraction());
	}

	/**
	 * 尝试执行流体交互
	 *
	 * <p>
	 * 内部调用 {@link FluidUtil#interactWithFluidHandler(Player, InteractionHand, Level, BlockPos, Direction)}
	 * 与方块的流体能力进行交互
	 * </p>
	 *
	 * <p>
	 * 若交互成功返回 {@link InteractionResult#SUCCESS},
	 * 否则返回 {@link InteractionResult#PASS}
	 * </p>
	 *
	 * @param player 玩家
	 * @param hand   使用的手
	 * @param level  世界
	 * @param pos    方块位置
	 * @param result 命中结果
	 * @return 交互结果
	 */
	default InteractionResult tryFluidInteraction(
			Player player,
			InteractionHand hand,
			Level level,
			BlockPos pos,
			BlockHitResult result
	) {
		if (player == null) {
			return InteractionResult.PASS;
		}

		if (canUseFluidInteraction(player) && FluidUtil.interactWithFluidHandler(
				player,
				hand,
				level,
				pos,
				result.getDirection()
		)) {
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
	}
}