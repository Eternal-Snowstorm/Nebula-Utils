package dev.celestiacraft.libs.api.register.multiblock;

import dev.celestiacraft.libs.api.client.tooltip.TooltipContext;
import dev.celestiacraft.libs.api.register.block.BasicBlockItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class ControllerBlockItem extends BasicBlockItem {
	public ControllerBlockItem(ControllerBlock block, Properties properties) {
		super(block, properties);
	}

	@Override
	public void addTooltips(TooltipContext context) {
		if (!(this.getBlock() instanceof ControllerBlock controller)) {
			return;
		}
		context.getTooltip().add(Component.translatable("tooltip.preview_right_click", controller.getTriggerName())
				.withStyle(ChatFormatting.AQUA));
	}
}