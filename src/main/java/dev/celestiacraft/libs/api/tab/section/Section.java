package dev.celestiacraft.libs.api.tab.section;

import dev.celestiacraft.libs.api.tab.FTSInternal;
import dev.celestiacraft.libs.api.tab.creativetab.BannerRenderer;
import dev.celestiacraft.libs.api.tab.creativetab.ConglomerateOfItems;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.function.Supplier;

/**
 * 创造模式标签页中的一个分区, 负责承载一组物品并定义其渲染与折叠行为.
 *
 * @since 3.0
 */
public interface Section<T extends Section<T>> {
	ResourceLocation id();

	default boolean collapsible() {
		return true;
	}

	/**
	 * 让需要人类可读标签的功能(例如跳转列表)能够适用于任意 Section 实现, 
	 * 而无需知道其具体类型.
	 *
	 * @return 该分区的显示标题, 若没有则返回 null.
	 * @since 6.0
	 */
	default Component getTitle() {
		return Component.translatable("section." + id().getNamespace() + "." + id().getPath());
	}

	/**
	 * @return 该分区的显示图标, 用于分区索引(Section Index)
	 * @since 6.0
	 */
	default ItemStack icon() {
		return items().getStacks()
				.stream()
				.filter((stack) -> {
					return !stack.isEmpty();
				}).findFirst()
				.orElse(Items.DIAMOND.getDefaultInstance());
	}

	/**
	 * 每当分区物品被重新加载时调用
	 *
	 * @since 6.0
	 */
	default void onReload(RegistryAccess registryAccess) {
	}


	ConglomerateOfItems items();

	void render(GuiGraphics guiGraphics, Font font, int topLeftX, int topLeftY);

	/**
	 * ConglomerateOfItems 的重定向辅助方法
	 *
	 * @since 4.0
	 */
	default T add(Item item) {
		items().add(item);
		return (T) this;
	}

	default T add(ItemStack stack) {
		items().add(stack);
		return (T) this;
	}

	default T add(RegistryObject<Item> registryObjectOfItem) {
		items().add(registryObjectOfItem);
		return (T) this;
	}

	default T add(ItemLike itemLike) {
		items().add(itemLike);
		return (T) this;
	}

	default T add(Supplier<ItemStack> itemStackSupplier) {
		items().add(itemStackSupplier);
		return (T) this;
	}

	default T add(List<ItemStack> listOfStacks) {
		items().add(listOfStacks);
		return (T) this;
	}

	default T add(ConglomerateOfItems.RegistryDependentEntry entry) {
		items().add(entry);
		return (T) this;
	}

	default T addItemTag(TagKey<Item> tag) {
		add((registry) -> {
			return registry.lookup(Registries.ITEM)
					.map((lookup) -> lookup.get(tag)
							.map((named) -> {
								return named.stream().map((holder) -> {
									return holder.value().getDefaultInstance();
								}).toList();
							}).orElse(List.of()))
					.orElseGet(List::of);
		});
		return (T) this;
	}

	/**
	 * 在给定分区横幅的右侧渲染折叠切换按钮.
	 * 若 isHoveringAny 传入 true, 则在按住 Shift 时高亮悬停纹理.
	 *
	 * @since 4.0
	 */
	default void renderToggle(CreativeModeInventoryScreen screen, GuiGraphics graphics, Section<?> section, int x, int y, int w, int bannerWidth, int mouseX, int mouseY, boolean isHoveringAny) {
		if (!section.collapsible()) {
			return;
		}
		// 横幅行中最右侧槽位的左上角.
		int tx1 = x + w + 3;
		int tx0 = tx1 - BannerRenderer.ROW_HEIGHT;
		int ty0 = y - 1;
		int ty1 = y + bannerWidth;

		// 将按钮纹理居中于槽位内(每个轴 +1 以对齐网格单元).
		int bx = tx0 + (BannerRenderer.ROW_HEIGHT - 16) / 2 + 1;
		int by = ty0 + (bannerWidth - 16) / 2 + 1;

		// 鼠标悬停时渲染带高亮的按钮纹理
		if (((isHoveringAny && Screen.hasShiftDown()) || mouseX >= tx0 && mouseX < tx1 && mouseY >= ty0 && mouseY < ty1) && screen.getMenu().getCarried().isEmpty()) {
			graphics.blit(FTSInternal.isCollapsed(section)
					? BannerRenderer.COLLAPSED_BUTTON_HIGHLIGHT
					: BannerRenderer.EXPANDED_BUTTON_HIGHLIGHT, bx, by, 0, 0, 16, 16, 16, 16);
		} else {
			graphics.blit(FTSInternal.isCollapsed(section)
					? BannerRenderer.COLLAPSED_BUTTON
					: BannerRenderer.EXPANDED_BUTTON, bx, by, 0, 0, 16, 16, 16, 16);
		}
	}
}