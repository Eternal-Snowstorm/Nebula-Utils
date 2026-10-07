package dev.celestiacraft.libs.api.tab.mixin.client;

import dev.celestiacraft.libs.api.tab.creativetab.BannerRenderer;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 在滚动创造模式物品栏时同步记录当前行号, 供横幅渲染定位使用.
 */
@Mixin(CreativeModeInventoryScreen.ItemPickerMenu.class)
public abstract class ItemPickerMenuMixin {
	@Shadow
	protected abstract int getRowIndexForScroll(float f);

	@Inject(method = "scrollTo", at = @At("HEAD"))
	private void nebula$scrollTo(float f, CallbackInfo ci) {
		BannerRenderer.CURRENT_ROW = getRowIndexForScroll(f);
	}
}