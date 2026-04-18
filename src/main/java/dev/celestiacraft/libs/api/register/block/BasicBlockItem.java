package dev.celestiacraft.libs.api.register.block;

import dev.celestiacraft.libs.api.client.tooltip.TooltipContext;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BasicBlockItem extends BlockItem {
	public BasicBlockItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
		Player player = Minecraft.getInstance().player;

		addTooltips(new TooltipContext(stack, level, tooltip, flag, player));
	}

	public void addTooltips(TooltipContext context) {
	}
}