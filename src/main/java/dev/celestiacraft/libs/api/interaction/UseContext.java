package dev.celestiacraft.libs.api.interaction;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class UseContext {
	@Getter
	private final Level level;
	@Getter
	private final BlockPos pos;
	@Getter
	private final Player player;
	@Getter
	private final InteractionHand hand;
	@Getter
	private final BlockState state;
	@Getter
	private final BlockHitResult result;

	public UseContext(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
		this.state = state;
		this.level = level;
		this.pos = pos;
		this.player = player;
		this.hand = hand;
		this.result = result;
	}

	public boolean isClient() {
		return level.isClientSide();
	}

	public ItemStack getItem() {
		return player.getItemInHand(hand);
	}
}