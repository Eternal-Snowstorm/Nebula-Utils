package dev.celestiacraft.libs.api.register.item.energy;

import dev.celestiacraft.libs.api.client.context.TooltipContext;
import dev.celestiacraft.libs.api.register.item.BasicItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 一个简单基础的电池物品类
 * <p>
 * 类自身不添加多余功能
 * <p>
 * 只重写了部分方法
 */
public abstract class BasicEnergyItem extends BasicItem implements IEnergyItem {
	public BasicEnergyItem(Properties properties) {
		super(properties);
	}

	@Override
	public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
		return new EnergyItemCapabilityProvider(stack, this);
	}

	@Override
	public int getBarColor(@NotNull ItemStack stack) {
		return getEnergyBarColor(stack);
	}

	@Override
	public boolean isBarVisible(@NotNull ItemStack stack) {
		return isEnergyBarVisible(stack);
	}

	/**
	 * 添加能量Tooltip
	 *
	 * @param context
	 */
	@Override
	public void addTooltips(TooltipContext context) {
		List<Component> tooltip = context.getTooltip();
		ItemStack stack = context.getStack();

		CompoundTag tag = stack.getTag();

		int stored = tag != null ? tag.getInt("Energy") : 0;
		int max = getMaxEnergyStored(stack);

		tooltip.add(Component.literal("§e" + stored + " / " + max + " FE"));
	}
}