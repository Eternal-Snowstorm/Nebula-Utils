package dev.celestiacraft.libs.client.tooltip;

import dev.celestiacraft.libs.client.tooltip.InlineItemTooltipComponent.ItemSegment;
import dev.celestiacraft.libs.client.tooltip.InlineItemTooltipComponent.Segment;
import dev.celestiacraft.libs.client.tooltip.InlineItemTooltipComponent.TextSegment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

import java.util.List;

/**
 * 自定义 ClientTooltipComponent, 在同一行内渲染文本和物品图标.
 * <p>
 * 使用 Font 的 String 方法 (而非 Component) 以正确解释 § 格式代码.
 * 支持每个物品图标独立的缩放比例.
 */
public class InlineItemClientTooltipComponent implements ClientTooltipComponent {

	private static final int FONT_HEIGHT = 9;
	private static final int TEXT_LINE_HEIGHT = 10;
	private static final int ITEM_SPACER = 1;

	private final List<Segment> segments;
	private final int lineHeight;

	public InlineItemClientTooltipComponent(InlineItemTooltipComponent data) {
		this.segments = data.segments();
		int maxH = TEXT_LINE_HEIGHT;
		for (Segment seg : segments) {
			if (seg instanceof ItemSegment is) {
				maxH = Math.max(maxH, (int) (16 * is.scale()) + 2);
			}
		}
		this.lineHeight = maxH;
	}

	@Override
	public int getHeight() {
		return lineHeight;
	}

	@Override
	public int getWidth(Font font) {
		int width = 0;
		for (Segment seg : segments) {
			if (seg instanceof TextSegment ts) {
				// String 版本的 width() 会正确跳过 § 格式代码
				width += font.width(ts.text());
			} else if (seg instanceof ItemSegment is) {
				width += itemTotalWidth(is.scale());
			}
		}
		return width;
	}

	@Override
	public void renderText(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource bufferSource) {
		float ox = x;
		float textY = y + (lineHeight - FONT_HEIGHT) / 2.0f;
		for (Segment seg : segments) {
			if (seg instanceof TextSegment ts) {
				// String 版本的 drawInBatch() 会正确解释 § 格式代码 (§c = 红色 等)
				font.drawInBatch(
						ts.text(), ox, textY, -1, true,
						matrix, bufferSource, Font.DisplayMode.NORMAL, 0, 15728880
				);
				ox += font.width(ts.text());
			} else if (seg instanceof ItemSegment is) {
				ox += itemTotalWidth(is.scale());
			}
		}
	}

	@Override
	public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
		float ox = x;
		for (Segment seg : segments) {
			if (seg instanceof TextSegment ts) {
				ox += font.width(ts.text());
			} else if (seg instanceof ItemSegment is) {
				float scale = is.scale();
				float itemPixels = 16 * scale;
				float iconY = y + (lineHeight - itemPixels) / 2.0f;
				guiGraphics.pose().pushPose();
				guiGraphics.pose().translate(ox + 1, iconY, 0);
				guiGraphics.pose().scale(scale, scale, scale);
				guiGraphics.renderFakeItem(is.stack(), 0, 0);
				guiGraphics.pose().popPose();
				ox += itemTotalWidth(scale);
			}
		}
	}

	private static int itemTotalWidth(float scale) {
		return (int) (16 * scale) + 2 + ITEM_SPACER;
	}
}
