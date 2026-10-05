package dev.celestiacraft.libs.api.tab.mixin.client;

import dev.celestiacraft.libs.api.tab.FTSInternal;
import dev.celestiacraft.libs.api.tab.FancyTabSections;
import dev.celestiacraft.libs.api.tab.creativetab.BannerRenderer;
import dev.celestiacraft.libs.api.tab.creativetab.IndexPanel;
import dev.celestiacraft.libs.api.tab.section.Section;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.List;

/**
 * 在创造模式物品栏界面中渲染分节横幅与索引面板，并接管对应的鼠标点击与滚轮交互。
 */
@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin {
	@Shadow
	private static CreativeModeTab selectedTab;

	@Shadow
	private float scrollOffs;

	@Shadow
	protected abstract void refreshCurrentTabContents(Collection<ItemStack> items);

	@Inject(method = "renderBg", at = @At("TAIL"))
	private void nebula$renderBanners(GuiGraphics graphics, float partialTick, int mouseX, int mouseY, CallbackInfo info) {
		CreativeModeInventoryScreen self = (CreativeModeInventoryScreen) (Object) this;
		ResourceLocation tab = BuiltInRegistries.CREATIVE_MODE_TAB.getKey(selectedTab);

		if (BannerRenderer.CURRENT_TAB == null || !BannerRenderer.CURRENT_TAB.equals(tab)) {
			BannerRenderer.CURRENT_TAB = tab;
			FTSInternal.applyItems(selectedTab);
			refreshCurrentTabContents(selectedTab.getDisplayItems());
		}

		if (FancyTabSections.REGISTERED_TABS.containsKey(tab)) {
			List<Section<?>> sections = FancyTabSections.REGISTERED_TABS.get(tab);

			BannerRenderer.render(self, graphics, sections, mouseX, mouseY);

			float jumpTo = IndexPanel.tick();
			if (jumpTo >= 0.0f) {
				scrollOffs = jumpTo;
				self.getMenu().scrollTo(jumpTo);
			}
			IndexPanel.render(self, graphics, sections, mouseX, mouseY);
		}
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void nebula$mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> returnable) {
		if (button != 0) {
			return;
		}

		CreativeModeInventoryScreen self = (CreativeModeInventoryScreen) (Object) this;

		// 仅对在本模块中注册过的标签页生效
		ResourceLocation tab = BuiltInRegistries.CREATIVE_MODE_TAB.getKey(selectedTab);
		if (!FancyTabSections.REGISTERED_TABS.containsKey(tab)) {
			return;
		}

		List<Section<?>> sections = FancyTabSections.REGISTERED_TABS.get(tab);

		if (IndexPanel.mouseClicked(self, sections, scrollOffs, mouseX, mouseY, button)) {
			returnable.setReturnValue(true);
			return;
		}

		if (BannerRenderer.isInBanner(self, mouseX, mouseY)) {
			if (!self.getMenu().getCarried().isEmpty()) {
				self.getMenu().setCarried(ItemStack.EMPTY);
				returnable.cancel();
				return;
			}
		}

		if (!self.getMenu().getCarried().isEmpty()) return;

		for (Section<?> section : sections) {
			if (section.collapsible() && BannerRenderer.isInToggle(self, section, mouseX, mouseY)) {
				// 切换全部
				if (Screen.hasShiftDown()) {
					if (FTSInternal.isCollapsed(section)) {
						sections.stream()
								.filter(Section::collapsible)
								.forEach((section1) -> {
									FTSInternal.expand(section1, section1.equals(section));
								});
					} else {
						sections.stream()
								.filter(Section::collapsible)
								.forEach((section1) -> {
									FTSInternal.collapse(section1, section1.equals(section));
								});
					}
				} else {
					// 切换被点击的分节
					FTSInternal.toggle(section);
				}

				// 刷新标签页
				FTSInternal.applyItems(selectedTab);
				refreshCurrentTabContents(selectedTab.getDisplayItems());

				returnable.setReturnValue(true);
				return;
			}
		}
	}

	@ModifyArg(
			method = "renderLabels",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I"
			),
			index = 2)
	private int nebula$shiftTitleForIndexPanel(int x) {
		ResourceLocation tab = BuiltInRegistries.CREATIVE_MODE_TAB.getKey(selectedTab);
		List<Section<?>> sections = FancyTabSections.REGISTERED_TABS.get(tab);
		return IndexPanel.active(sections) ? IndexPanel.titleX() : x;
	}

	@Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
	private void nebula$indexPanelScroll(double mouseX, double mouseY, double delta, CallbackInfoReturnable<Boolean> cir) {
		ResourceLocation tab = BuiltInRegistries.CREATIVE_MODE_TAB.getKey(selectedTab);
		List<Section<?>> sections = FancyTabSections.REGISTERED_TABS.get(tab);
		if (sections == null) {
			return;
		}

		if (IndexPanel.mouseScrolled((CreativeModeInventoryScreen) (Object) this, sections, mouseX, mouseY, delta)) {
			cir.setReturnValue(true);
		}
	}
}