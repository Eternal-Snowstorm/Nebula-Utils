package dev.celestiacraft.libs.api.register.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

public class BurnItem extends BasicItem {
	private final int burmTimes;

	public BurnItem(Properties properties, int burmTimes) {
		super(properties);
		this.burmTimes = burmTimes;
	}

	@Override
	public int getBurnTime(ItemStack stack, @Nullable RecipeType<?> type) {
		return burmTimes;
	}
}