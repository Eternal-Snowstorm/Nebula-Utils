package dev.celestiacraft.libs.compat.ftbquests.client;

import dev.celestiacraft.libs.client.tooltip.InlineItemPatternParser;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * FTB Library Widget 子类, 在 FTB Quests 任务描述面板中渲染文本 + 内联物品图标的混合内容.
 * <p>
 * 物品列表在构造时一次性解析并缓存为 {@link ItemStack},
 * 每帧 {@link #draw} 仅做轮播索引计算, 无注册表查询和对象分配.
 */
public class InlineItemQuestWidget extends Widget {

	private static final int LINE_HEIGHT = 12;
	private static final int ITEM_SPACER = 1;

	private sealed interface RenderSegment permits TextRender, ItemRender {
	}

	private record TextRender(String text) implements RenderSegment {
	}

	/** 物品段: 构造时预解析为 ItemStack 列表, draw 时仅做索引选取. */
	private record ItemRender(List<ItemStack> stacks, float scale, float speed) implements RenderSegment {
	}

	private final List<List<RenderSegment>> wrappedLines;

	public InlineItemQuestWidget(Panel parent, String rawText, int maxWidth) {
		super(parent);

		Font font = Minecraft.getInstance().font;
		List<RenderSegment> segments = parseRenderSegments(rawText);
		this.wrappedLines = wrapSegments(segments, maxWidth, font);

		setWidth(maxWidth);
		setHeight(wrappedLines.size() * LINE_HEIGHT);
	}

	@Override
	public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		Font font = Minecraft.getInstance().font;
		int lineY = y;

		for (List<RenderSegment> line : wrappedLines) {
			float ox = x;
			for (RenderSegment seg : line) {
				if (seg instanceof TextRender tr) {
					graphics.drawString(font, tr.text(), (int) ox, lineY + 1, 0xFFFFFF, false);
					ox += font.width(tr.text());
				} else if (seg instanceof ItemRender ir) {
					List<ItemStack> stacks = ir.stacks();
					ItemStack stack = stacks.get(carouselIndex(stacks.size(), ir.speed()));
					float scale = ir.scale();
					float itemPixels = 16 * scale;
					float iconY = lineY + (LINE_HEIGHT - itemPixels) / 2.0f;
					graphics.pose().pushPose();
					graphics.pose().translate(ox + 1, iconY, 0);
					graphics.pose().scale(scale, scale, 1);
					graphics.renderFakeItem(stack, 0, 0);
					graphics.pose().popPose();
					ox += itemTotalWidth(scale);
				}
			}
			lineY += LINE_HEIGHT;
		}
	}

	private static List<RenderSegment> parseRenderSegments(String rawText) {
		List<RenderSegment> segments = new ArrayList<>();
		String leadingCodes = InlineItemPatternParser.extractLeadingFormatCodes(rawText);
		Matcher matcher = InlineItemPatternParser.ITEM_PATTERN.matcher(rawText);
		int lastEnd = 0;
		boolean pastFirst = false;

		while (matcher.find()) {
			if (matcher.start() > lastEnd) {
				String seg = rawText.substring(lastEnd, matcher.start());
				if (pastFirst && !leadingCodes.isEmpty()) {
					seg = leadingCodes + seg;
				}
				segments.add(new TextRender(seg));
			}
			pastFirst = true;

			String identifier = matcher.group(1);
			String scaleStr = matcher.group(2);
			String speedStr = matcher.group(3);
			float scale = scaleStr != null ? Float.parseFloat(scaleStr) : InlineItemPatternParser.DEFAULT_SCALE;
			float speed = speedStr != null ? Float.parseFloat(speedStr) : InlineItemPatternParser.DEFAULT_SPEED;

			List<ItemStack> stacks = resolveItemStacks(identifier);
			if (!stacks.isEmpty()) {
				segments.add(new ItemRender(stacks, scale, speed));
			}
			lastEnd = matcher.end();
		}

		if (lastEnd < rawText.length()) {
			String seg = rawText.substring(lastEnd);
			if (pastFirst && !leadingCodes.isEmpty()) {
				seg = leadingCodes + seg;
			}
			segments.add(new TextRender(seg));
		}

		return segments;
	}

	/** 一次性将标识符解析为不可变的 ItemStack 列表, 供后续帧直接索引. */
	private static List<ItemStack> resolveItemStacks(String identifier) {
		List<Item> items;
		if (identifier.startsWith("[")) {
			items = InlineItemPatternParser.parseItemArray(identifier);
		} else if (identifier.startsWith("#")) {
			items = InlineItemPatternParser.resolveTagItems(identifier.substring(1));
		} else {
			Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(identifier));
			if (item == null || item == Items.AIR) return List.of();
			return List.of(new ItemStack(item));
		}
		if (items.isEmpty()) return List.of();
		List<ItemStack> stacks = new ArrayList<>(items.size());
		for (Item item : items) {
			stacks.add(new ItemStack(item));
		}
		return List.copyOf(stacks);
	}

	private static int carouselIndex(int size, float speed) {
		if (size <= 1) return 0;
		long tick = System.currentTimeMillis() / 50;
		int interval = Math.max(1, (int) speed);
		return (int) ((tick / interval) % size);
	}

	private static List<List<RenderSegment>> wrapSegments(List<RenderSegment> segments, int maxWidth, Font font) {
		List<List<RenderSegment>> lines = new ArrayList<>();
		List<RenderSegment> currentLine = new ArrayList<>();
		int currentWidth = 0;

		for (RenderSegment segment : segments) {
			if (segment instanceof TextRender tr) {
				String text = tr.text();
				int textWidth = font.width(text);

				if (currentWidth + textWidth <= maxWidth || currentLine.isEmpty()) {
					currentLine.add(tr);
					currentWidth += textWidth;
				} else {
					String[] words = text.split("(?<=\\s)");
					for (String word : words) {
						int wordWidth = font.width(word);
						if (currentWidth + wordWidth > maxWidth && !currentLine.isEmpty()) {
							lines.add(currentLine);
							currentLine = new ArrayList<>();
							currentWidth = 0;
						}
						currentLine.add(new TextRender(word));
						currentWidth += wordWidth;
					}
				}
			} else if (segment instanceof ItemRender ir) {
				int itemWidth = itemTotalWidth(ir.scale());
				if (currentWidth + itemWidth > maxWidth && !currentLine.isEmpty()) {
					lines.add(currentLine);
					currentLine = new ArrayList<>();
					currentWidth = 0;
				}
				currentLine.add(ir);
				currentWidth += itemWidth;
			}
		}

		if (!currentLine.isEmpty()) {
			lines.add(currentLine);
		}

		return lines;
	}

	private static int itemTotalWidth(float scale) {
		return (int) (16 * scale) + 2 + ITEM_SPACER;
	}
}
