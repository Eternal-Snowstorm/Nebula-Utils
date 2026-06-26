package dev.celestiacraft.libs.api.register.block;

import dev.celestiacraft.libs.api.interaction.IFluidInteractable;
import dev.celestiacraft.libs.api.interaction.context.UseContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.function.ToIntFunction;

public class BasicBlock extends Block implements IFluidInteractable {
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	public static final DirectionProperty HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;

	public static final BooleanProperty LIT = BlockStateProperties.LIT;
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	public BasicBlock(Properties properties) {
		super(properties);

		BlockState state = defaultBlockState();

		Property<Direction> property = getFacingProperty();
		if (property != null) {
			state = state.setValue(property, Direction.NORTH);
		}

		if (useRedstone()) {
			state = state.setValue(POWERED, false);
		}

		if (useLitState()) {
			state = state.setValue(LIT, false);
		}

		registerDefaultState(state);
	}

	@Override
	public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult result) {
		return useOn(new UseContext(state, level, pos, player, hand, result));
	}

	public InteractionResult useOn(UseContext context) {
		InteractionResult result = tryFluidInteraction(
				context.getPlayer(),
				context.getHand(),
				context.getLevel(),
				context.getPos(),
				context.getResult()
		);
		if (result.consumesAction()) {
			return result;
		}

		return InteractionResult.PASS;
	}

	@Override
	public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
		return RenderShape.MODEL;
	}


	/**
	 * 定义该控制器使用的方向类型
	 *
	 * <p>
	 * 用于决定:
	 * </p>
	 * <ul>
	 *     <li>是否具有方向属性</li>
	 *     <li>使用哪种方向属性(六面 / 水平)</li>
	 *     <li>结构旋转与匹配的方式</li>
	 * </ul>
	 *
	 * <p>
	 * 可选类型:
	 * </p>
	 * <ul>
	 *     <li>{@code NONE}: 无方向(默认)</li>
	 *     <li>{@code HORIZONTAL}: 仅水平方向(N/E/S/W)</li>
	 *     <li>{@code FACING}: 六面方向(包含上下)</li>
	 * </ul>
	 *
	 * <p>
	 * 行为影响:
	 * </p>
	 * <ul>
	 *     <li>自动注册对应的 {@link Property}</li>
	 *     <li>控制 {@link #getStateForPlacement(BlockPlaceContext)} 的朝向逻辑</li>
	 *     <li>影响 {@link #rotate(BlockState, Rotation)} 与 {@link #mirror(BlockState, Mirror)}</li>
	 *     <li>用于多方块结构的方向对齐</li>
	 * </ul>
	 *
	 * <p>
	 * 子类通常应根据机器类型选择:
	 * </p>
	 * <ul>
	 *     <li>简单机器: {@code HORIZONTAL}</li>
	 *     <li>立体结构 / 可朝上放置: {@code FACING}</li>
	 *     <li>纯逻辑控制器: {@code NONE}</li>
	 * </ul>
	 *
	 * <pre>{@code
	 * @Override
	 * protected BasicBlockFacing useFacingType() {
	 *     return BasicBlockFacing.HORIZONTAL;
	 * }
	 * }</pre>
	 *
	 * @return 使用的方向类型, 默认 {@code NONE}
	 */
	protected BlockFacing useFacingType() {
		return BlockFacing.NONE;
	}

	/**
	 * 获取当前 Block 使用的方向属性
	 *
	 * <p>
	 * 根据 {@link #useFacingType()} 返回对应的 {@link Property}:
	 * </p>
	 *
	 * <ul>
	 *     <li>{@code FACING} => {@link BlockStateProperties#FACING}</li>
	 *     <li>{@code HORIZONTAL} => {@link BlockStateProperties#HORIZONTAL_FACING}</li>
	 *     <li>{@code NONE} => {@code null}</li>
	 * </ul>
	 *
	 * <p>
	 * 该方法主要用于:
	 * </p>
	 * <ul>
	 *     <li>注册 BlockState 属性</li>
	 *     <li>读取或写入朝向</li>
	 * </ul>
	 *
	 * @return 当前使用的方向属性, 若未启用方向则返回 {@code null}
	 */
	protected Property<Direction> getFacingProperty() {
		return switch (useFacingType()) {
			case FACING -> FACING;
			case HORIZONTAL -> HORIZONTAL_FACING;
			default -> null;
		};
	}

	/**
	 * 从 BlockState 中获取当前朝向
	 *
	 * <p>
	 * 若未启用方向属性或属性不存在, 则默认返回 {@link Direction#NORTH}
	 * </p>
	 *
	 * @param state 方块状态
	 * @return 当前朝向(保证非空)
	 */
	protected Direction getFacing(BlockState state) {
		Property<Direction> property = getFacingProperty();

		if (property == null || !state.hasProperty(property)) {
			return Direction.NORTH;
		}

		return state.getValue(property);
	}

	/**
	 * 注册 BlockState 属性
	 *
	 * <p>
	 * 自动根据 {@link #useFacingType()} 注册方向属性
	 * </p>
	 *
	 * <p>
	 * 子类一般无需重写, 除非需要额外属性
	 * </p>
	 *
	 * @param builder 状态构建器
	 */
	@Override
	protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);

		Property<Direction> property = getFacingProperty();
		if (property != null) {
			builder.add(property);
		}

		if (useRedstone()) {
			builder.add(POWERED);
		}

		if (useLitState()) {
			builder.add(LIT);
		}
	}

	/**
	 * 方块放置时的状态初始化
	 *
	 * <p>
	 * 默认行为:
	 * </p>
	 * <ul>
	 *     <li>水平模式: 朝向玩家反方向</li>
	 *     <li>六面模式:
	 *         <ul>
	 *             <li>正常: 水平反方向</li>
	 *             <li>潜行: 使用玩家视线方向</li>
	 *         </ul>
	 *     </li>
	 * </ul>
	 *
	 * @param context 放置上下文
	 * @return 初始化后的 BlockState
	 */
	@Override
	public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
		BlockState state = defaultBlockState();

		Property<Direction> property = getFacingProperty();
		if (property == null) {
			return state;
		}

		Direction facing = context.getHorizontalDirection().getOpposite();

		// 如果是六面方向支持上下
		if (useFacingType() == BlockFacing.FACING) {
			if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
				facing = context.getNearestLookingDirection().getOpposite();
			}
		}

		return state.setValue(property, facing);
	}

	/**
	 * 旋转方块状态
	 *
	 * <p>
	 * 自动对方向属性应用 {@link Rotation}
	 * </p>
	 *
	 * @param state    原状态
	 * @param rotation 旋转类型
	 * @return 旋转后的状态
	 */
	@Override
	public @NotNull BlockState rotate(@NotNull BlockState state, @NotNull Rotation rotation) {
		Property<Direction> property = getFacingProperty();

		if (property == null || !state.hasProperty(property)) {
			return state;
		}

		return state.setValue(property, rotation.rotate(state.getValue(property)));
	}

	/**
	 * 镜像方块状态
	 *
	 * <p>
	 * 实际通过 {@link #rotate(BlockState, Rotation)} 实现
	 * </p>
	 *
	 * @param state  原状态
	 * @param mirror 镜像类型
	 * @return 镜像后的状态
	 */
	@Override
	public @NotNull BlockState mirror(@NotNull BlockState state, Mirror mirror) {
		return rotate(state, mirror.getRotation(getFacing(state)));
	}

	/**
	 * 是否启用红石状态
	 *
	 * @return
	 */
	protected boolean useRedstone() {
		return false;
	}

	/**
	 * 是否启用点燃状态
	 *
	 * @return
	 */
	protected boolean useLitState() {
		return false;
	}

	/**
	 * 创建一个基于 {@link #LIT} 状态的亮度函数.
	 * <p>
	 * 当方块处于点亮状态时, 返回指定的亮度等级,
	 * 否则返回 {@code 0}.
	 *
	 * @param litLevel 点亮时的亮度等级, 会被限制在 {@code [0, 15]} 范围内
	 * @return 方块亮度函数
	 */
	public static ToIntFunction<BlockState> litBlockEmission(int litLevel) {
		return litBlockEmission(15, 0);
	}

	/**
	 * 创建一个基于 {@link #LIT} 状态的亮度函数.
	 * <p>
	 * 当方块处于点亮状态时返回 {@code litLevel},
	 * 熄灭状态时返回 {@code extinguishLevel}.
	 * <p>
	 * 两个亮度值都会自动限制在 {@code [0, 15]} 范围内.
	 *
	 * @param litLevel        点亮时的亮度等级
	 * @param extinguishLevel 熄灭时的亮度等级
	 * @return 方块亮度函数
	 */
	public static ToIntFunction<BlockState> litBlockEmission(int litLevel, int extinguishLevel) {
		litLevel = Math.min(15, Math.max(0, litLevel));
		extinguishLevel = Math.min(15, Math.max(0, extinguishLevel));

		int finalLitLevel = litLevel;
		int finalExtinguishLevel = extinguishLevel;
		return (state) -> {
			return state.getValue(LIT)
					? finalLitLevel
					: finalExtinguishLevel;
		};
	}

	/**
	 * 根据方块朝向获取对应的 Y 轴旋转角度.
	 * <p>
	 * 通常用于方块模型, BER 或 PartialModel 的水平旋转.
	 *
	 * @param direction 方块朝向
	 * @return 对应的 Y 轴旋转角度
	 */
	public static int getYRotFromFacing(Direction direction) {
		return switch (direction) {
			case EAST -> 90;
			case SOUTH -> 180;
			case WEST -> 270;
			default -> 0;
		};
	}

	/**
	 * 根据方块朝向获取对应的 X 轴旋转角度.
	 * <p>
	 * 仅对 {@link Direction#UP} 与 {@link Direction#DOWN} 生效,
	 * 其余方向返回 {@code 0}.
	 *
	 * @param direction 方块朝向
	 * @return 对应的 X 轴旋转角度
	 */
	public static int getXRotFromFacing(Direction direction) {
		return switch (direction) {
			case DOWN -> 90;
			case UP -> -90;
			default -> 0;
		};
	}
}