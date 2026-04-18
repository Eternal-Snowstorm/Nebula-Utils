package dev.celestiacraft.libs.api.register.multiblock;

import dev.celestiacraft.libs.api.register.block.BasicBlockItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ControllerBlockItem extends BasicBlockItem {
	public ControllerBlockItem(ControllerBlock block, Properties properties) {
		super(block, properties);
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
		if (!(this.getBlock() instanceof ControllerBlock controller)) {
			return;
		}
		tooltip.add(Component.translatable("tooltip.preview_right_click", controller.getTriggerName())
				.withStyle(ChatFormatting.AQUA));
	}
}