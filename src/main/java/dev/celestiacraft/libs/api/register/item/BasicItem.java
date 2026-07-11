package dev.celestiacraft.libs.api.register.item;

import dev.celestiacraft.libs.api.client.context.TooltipContext;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BasicItem extends Item {
	public BasicItem(Properties properties) {
		super(properties);
	}

	protected InteractionResult useOtherItem(@NotNull Item item, @NotNull UseOnContext context) {
		ItemStack stack = item.getDefaultInstance();

		BlockHitResult result = new BlockHitResult(
				context.getClickLocation(),
				context.getClickedFace(),
				context.getClickedPos(),
				false
		);

		UseOnContext newContext = new UseOnContext(
				context.getLevel(),
				context.getPlayer(),
				context.getHand(),
				stack,
				result
		);

		return item.useOn(newContext);
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
		Player player = Minecraft.getInstance().player;

		addTooltips(new TooltipContext(stack, level, tooltip, flag, player));
	}

	public void addTooltips(TooltipContext context) {
	}
}