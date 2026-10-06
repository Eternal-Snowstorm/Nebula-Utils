package dev.celestiacraft.libs.api.register.block;

import dev.celestiacraft.libs.api.client.context.TooltipContext;
import dev.celestiacraft.libs.api.client.tooltip.ClientTooltips;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BasicBlockItem extends BlockItem {
	public BasicBlockItem(Block block, Properties properties) {
		super(block, properties);
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
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientTooltips.render(this, stack, level, tooltip, flag));
	}

	public void addTooltips(TooltipContext context) {
	}
}