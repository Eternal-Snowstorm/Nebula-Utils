package dev.celestiacraft.libs.compat.curios;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * 该对象用于在构件行为触发时传递运行环境信息.
 * </p>
 *
 * <p>
 * Context 会根据不同的触发事件填充不同字段, 
 * 因此部分字段可能为 {@code null}.
 * </p>
 */
public class CuriosContext {
	/**
	 * 玩家
	 */
	@Getter
	private final Player player;

	/**
	 * 当前世界
	 */
	@Getter
	private final Level level;

	/**
	 * 当前 ItemStack
	 */
	@Getter
	private final ItemStack stack;

	/**
	 * 实体
	 */
	@Nullable
	@Getter
	private Entity entity;

	/**
	 * 存活实体
	 */
	@Nullable
	@Getter
	private LivingEntity livingEntity;

	/**
	 * 玩家使用的手
	 */
	@Nullable
	@Getter
	private InteractionHand hand;

	/**
	 * 伤害来源
	 */
	@Nullable
	@Getter
	private DamageSource damageSource;

	/**
	 * 伤害数值
	 */
	@Getter
	private float damage;

	/**
	 * 方块位置
	 */
	@Nullable
	@Getter
	private BlockPos pos;

	/**
	 * 方块状态
	 */
	@Nullable
	@Getter
	private BlockState state;

	public CuriosContext(Player player, Level level, ItemStack stack) {
		this.player = player;
		this.level = level;
		this.stack = stack;
	}
}