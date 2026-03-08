package dev.celestiacraft.libs.debug;

import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.common.register.NebulaItem;
import dev.celestiacraft.libs.utils.CommandUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = NebulaLibs.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DebugTools {
	/**
	 * 是否拿着调试工具
	 *
	 * @param player
	 * @return
	 */
	public static boolean isDebugToolOffhand(Player player) {
		return player.getOffhandItem().getItem() == NebulaItem.GEOLOGICAL_HAMMER.get();
	}

	/**
	 * 是否允许使用 Debug 工具
	 *
	 * @param player
	 * @return
	 */
	private static boolean canUse(Player player) {
		return DebugUserManager.isDebugger(player) && isDebugToolOffhand(player);
	}

	/**
	 * Chat 调试指令
	 *
	 * @param event
	 */
	@SubscribeEvent
	public static void onServerChat(ServerChatEvent event) {
		ServerPlayer player = event.getPlayer();
		String message = event.getRawText().trim().toLowerCase();

		if (!DebugUserManager.isDebugger(player)) {
			return;
		}

		switch (message) {
			case "-kf":
				player.addEffect(new MobEffectInstance(
						MobEffects.NIGHT_VISION,
						-1,
						255,
						false,
						false
				));
				player.addEffect(new MobEffectInstance(
						MobEffects.DAMAGE_BOOST,
						-1,
						255,
						false,
						false
				));
				player.addEffect(new MobEffectInstance(
						MobEffects.DAMAGE_RESISTANCE,
						-1,
						255,
						false,
						false
				));
				event.setCanceled(true);
				break;

			case "-efc":
				player.removeAllEffects();
				event.setCanceled(true);
				break;

			case "-rej":
				CommandUtils.runCommand(player, "kjs reload client_scripts");
				CommandUtils.runCommand(player, "reload");
				player.sendSystemMessage(Component.translatable("message.jei.reloaded")
						.withStyle(ChatFormatting.GREEN));
				event.setCanceled(true);
				break;

			case "-re":
				List<String> commandList = List.of(
						"client_scripts",
						"config",
						"lang",
						"server_scripts",
						"startup_scripts",
						"textures"
				);
				commandList.forEach((cmd) -> {
					CommandUtils.runCommand(player, "kjs reload " + cmd);
				});

				player.sendSystemMessage(Component.translatable("message.reloaded")
						.withStyle(ChatFormatting.GREEN));

				event.setCanceled(true);
				break;
		}
	}

	/**
	 * 潜行 + 右键空气
	 * kjs hand
	 *
	 * @param event
	 */
	@SubscribeEvent
	public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
		Player player = event.getEntity();

		if (player.level().isClientSide()) {
			return;
		}
		if (event.getHand() != InteractionHand.OFF_HAND) {
			return;
		}
		if (!canUse(player)) {
			return;
		}
		if (!player.isCrouching()) {
			return;
		}
		// 主手不能是空气
		if (player.getMainHandItem().isEmpty()) {
			return;
		}
		if (event.getItemStack().isEmpty()) {
			return;
		}

		CommandUtils.runCommand(player, "kjs hand");

		player.swing(InteractionHand.OFF_HAND);

		event.setCanceled(true);
	}

	/**
	 * 潜行 + 右键方块
	 * 查看硬度
	 *
	 * @param event
	 */
	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		Player player = event.getEntity();

		if (player.level().isClientSide()) {
			return;
		}
		if (event.getHand() != InteractionHand.OFF_HAND) {
			return;
		}
		if (!canUse(player)) {
			return;
		}
		if (!player.isCrouching()) {
			return;
		}

		BlockPos pos = event.getPos();
		BlockState state = player.level().getBlockState(pos);

		float hardness = state.getDestroySpeed(player.level(), pos);

		player.sendSystemMessage(Component.translatable("message.block_hardness", hardness)
				.withStyle(ChatFormatting.YELLOW));

		player.swing(InteractionHand.OFF_HAND);

		event.setCanceled(true);
	}
}