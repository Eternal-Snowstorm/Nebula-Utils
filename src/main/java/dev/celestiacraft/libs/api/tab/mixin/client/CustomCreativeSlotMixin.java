package dev.celestiacraft.libs.api.tab.mixin.client;

import dev.celestiacraft.libs.api.tab.FTSInternal;
import dev.celestiacraft.libs.api.tab.creativetab.BannerRenderer;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;

/**
 * 让创造模式物品栏的自定义槽位跳过横幅行：横幅行不可交互，也不显示高亮。
 */
@Mixin(targets = "net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen$CustomCreativeSlot")
public class CustomCreativeSlotMixin extends Slot {
	public CustomCreativeSlotMixin() {
		super(null, 0, 0, 0);
	}

	@Override
	public boolean isActive() {
		int absoluteRow = BannerRenderer.CURRENT_ROW + index / 9;
		return !FTSInternal.isBannerRow(BannerRenderer.CURRENT_TAB, absoluteRow);
	}

	@Override
	public boolean isHighlightable() {
		int absoluteRow = BannerRenderer.CURRENT_ROW + index / 9;
		return BannerRenderer.CURRENT_TAB == null
				|| !FTSInternal.isBannerRow(BannerRenderer.CURRENT_TAB, absoluteRow);
	}
}