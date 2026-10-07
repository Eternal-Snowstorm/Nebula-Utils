package dev.celestiacraft.libs.api.tab.section;

import dev.celestiacraft.libs.api.tab.creativetab.ConglomerateOfItems;
import lombok.Getter;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 带标题渲染能力的抽象分区基类, 标题支持滚动、居中、描边等效果.
 *
 * @since 4.0
 */
public class AbstractSectionWithTitle<T extends AbstractSectionWithTitle<T>> implements ISection<T>, IStickySection {
	final ResourceLocation id;
	// Component.translatable("section." + id.getNamespace() + "." + id.getPath())
	@Getter
	public Component title;
	public boolean renderTitle = true;
	public int titleOffsetX = 5;
	public int titleOffsetY = 5;
	public int textColor = 0xFFFFFFFF;
	public int textOutline = 0x00000000;
	public boolean textShadow = true;
	boolean centered = false;
	boolean collapsible = true;
	boolean sticky = true;
	Function<RegistryAccess, ItemStack> displayItemFunction = null;
	ItemStack displayItem = null;
	Runnable onRender = () -> {
	};
	Consumer<T> onRenderConsumer = (section) -> {
	};
	ConglomerateOfItems items = ConglomerateOfItems.create();

	public AbstractSectionWithTitle(ResourceLocation id) {
		this.id = id;
		title = Component.translatable("section." + id.getNamespace() + "." + id.getPath());
	}

	@Override
	public void render(GuiGraphics guiGraphics, Font font, int topLeftX, int topLeftY) {
		onRender.run();
		onRenderConsumer.accept((T) this);
		renderTitle(guiGraphics, font, topLeftX, topLeftY);
	}

	public void renderTitle(GuiGraphics guiGraphics, Font font, int topLeftX, int topLeftY) {
		if (!renderTitle) return;
		topLeftX += titleOffsetX;
		topLeftY += titleOffsetY;

		if (centered) {
			if (textOutline != 0x00000000) {
				centeredScrollingText(guiGraphics, font, title, topLeftX + 78, topLeftX, topLeftX + 137, topLeftY - 1, textOutline, false);
				centeredScrollingText(guiGraphics, font, title, topLeftX + 78, topLeftX, topLeftX + 137, topLeftY + 1, textOutline, textShadow);
				centeredScrollingText(guiGraphics, font, title, topLeftX + 78 + 1, topLeftX + 1, topLeftX + 137 + 1, topLeftY, textOutline, textShadow);
				centeredScrollingText(guiGraphics, font, title, topLeftX + 78 - 1, topLeftX - 1, topLeftX + 137 - 1, topLeftY, textOutline, false);
			}

			centeredScrollingText(guiGraphics, font, title, topLeftX + 78, topLeftX, topLeftX + 137, topLeftY, textColor, textShadow);
		} else {
			if (textOutline != 0x00000000) {
				scrollingText(guiGraphics, font, title, topLeftX, topLeftX + 137, topLeftY - 1, textOutline, false, 100);
				scrollingText(guiGraphics, font, title, topLeftX, topLeftX + 137, topLeftY + 1, textOutline, textShadow, 100);
				scrollingText(guiGraphics, font, title, topLeftX + 1, topLeftX + 137 + 1, topLeftY, textOutline, textShadow, 100);
				scrollingText(guiGraphics, font, title, topLeftX - 1, topLeftX + 137 - 1, topLeftY, textOutline, false, 100);
			}

			scrollingText(guiGraphics, font, title, topLeftX, topLeftX + 137, topLeftY, textColor, textShadow, 100);
		}
	}


	public T setTitle(Component component) {
		title = component;
		return (T) this;
	}

	public T setTitle(String title) {
		this.title = Component.literal(title);
		return (T) this;
	}

	/**
	 * 设置标题是否渲染.
	 *
	 * @since 6.0
	 */
	public T setRenderTitle(boolean renderTitle) {
		this.renderTitle = renderTitle;
		return (T) this;
	}

	/**
	 * 设置用于显示的 ItemStack.
	 *
	 * @since 6.0
	 */
	public T setDisplayItem(Function<RegistryAccess, ItemStack> item) {
		displayItemFunction = item;
		return (T) this;
	}

	/**
	 * 触发 displayItemFunction, 以便根据 RegistryAccess 设置 ItemStack
	 *
	 * @since 6.0
	 */
	@Override
	public void onReload(RegistryAccess registryAccess) {
		if (displayItemFunction != null) displayItem = displayItemFunction.apply(registryAccess);
	}

	/**
	 * 若已设置显示物品则使用它, 否则传递给父类实现
	 *
	 * @since 6.0
	 */
	@Override
	public ItemStack icon() {
		return displayItem == null ? ISection.super.icon() : displayItem;
	}

	/**
	 * 调整标题渲染的偏移量.
	 * 默认值为 (5, 5)
	 *
	 * @since 4.0
	 */
	public T setTitleOffset(int x, int y) {
		titleOffsetX = x;
		titleOffsetY = y;
		return (T) this;
	}

	/**
	 * 让分区标题居中显示(中间位置 + 偏移量)
	 *
	 * @since 5.0
	 */
	public T setCentered(boolean centered) {
		this.centered = centered;
		return (T) this;
	}

	public T setTextColor(int textColor) {
		this.textColor = textColor;
		return (T) this;
	}

	public T setTextOutline(int textOutline) {
		this.textOutline = textOutline;
		return (T) this;
	}

	public T setTextShadow(boolean textShadow) {
		this.textShadow = textShadow;
		return (T) this;
	}

	public T setCollapsible(boolean collapsible) {
		this.collapsible = collapsible;
		return (T) this;
	}

	public T setItems(ConglomerateOfItems items) {
		this.items = items;
		return (T) this;
	}

	/**
	 * 调整标题渲染的偏移量.
	 * 默认值为 (5, 5)
	 *
	 * @since 5.0
	 */
	public T setSticky(boolean sticky) {
		this.sticky = sticky;
		return (T) this;
	}

	/**
	 * 每当该 Section 的 {@link ISection#render(GuiGraphics, Font, int, int)} 被调用时执行代码.
	 *
	 * @since 5.0
	 */
	public T setOnRender(Runnable onRender) {
		this.onRender = onRender;
		return (T) this;
	}

	/**
	 * 设置一个消费者, 在该 Section 的 {@link ISection#render(GuiGraphics, Font, int, int)} 开始时运行.
	 *
	 * @since 6.0
	 */
	public T setOnRenderConsumer(Consumer<T> onRenderSection) {
		onRenderConsumer = onRenderSection;
		return (T) this;
	}

	@Override
	public ResourceLocation id() {
		return id;
	}

	@Override
	public ConglomerateOfItems items() {
		return items;
	}

	@Override
	public boolean collapsible() {
		return collapsible;
	}

	@Override
	public boolean isSticky() {
		return sticky;
	}

	/**
	 * 来自 wdUtils
	 *
	 * @since 6.2
	 */
	public static void centeredScrollingText(GuiGraphics guiGraphics, Font font, Component text, int centerX, int minX, int maxX, int y, int color, boolean shadow) {
		int i = font.width(text);
		int k = maxX - minX;
		if (i > k) {
			int l = i - k;
			double d0 = Util.getMillis() / (double) 300.0F;
			double d1 = Math.max(l * (double) 0.5F, 3.0F);
			double d2 = StrictMath.sin((Math.PI / 2.0D) * StrictMath.cos((Math.PI * 2.0D) * d0 / d1)) / 2.0F + 0.5F;
			double d3 = Mth.lerp(d2, 0.0F, l);
			guiGraphics.enableScissor(minX, y - 10, maxX, y + 10);
			int x = minX - (int) d3;
			guiGraphics.drawString(font, text, x, y, color, shadow);
			guiGraphics.disableScissor();
		} else {
			int i1 = Mth.clamp(centerX, minX + i / 2, maxX - i / 2);
			guiGraphics.drawString(font, text.getVisualOrderText(), i1 - font.width(text.getVisualOrderText()) / 2, y, color, shadow);
		}
	}

	/**
	 * 来自 wdUtils
	 *
	 * @since 6.2
	 */
	public static void scrollingText(GuiGraphics guiGraphics, Font font, Component text, int minX, int maxX, int y, int color, boolean shadow, int scrollingSpeed) {
		int i = font.width(text);
		int k = maxX - minX;
		if (i > k) {
			int l = i - k;
			double d0 = Util.getMillis() / (double) scrollingSpeed;
			double d1 = Math.max(l * (double) 0.5F, 3.0F);
			double d2 = StrictMath.sin((Math.PI / 2.0D) * StrictMath.cos((Math.PI * 2.0D) * d0 / d1)) / 2.0F + 0.5F;
			double d3 = Mth.lerp(d2, 0.0F, l);
			guiGraphics.enableScissor(minX, y - 20, maxX, y + 20);
			int x = minX - (int) d3;
			guiGraphics.drawString(font, text, x, y, color, shadow);
			guiGraphics.disableScissor();
		} else {
			int i1 = Mth.clamp(minX, minX + i / 2, maxX - i / 2);
			guiGraphics.drawString(font, text.getVisualOrderText(), i1 - font.width(text.getVisualOrderText()) / 2, y, color, shadow);
		}
	}
}