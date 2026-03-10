package dev.celestiacraft.libs.client.tooltip;

import dev.celestiacraft.libs.client.tooltip.InlineItemTooltipComponent.ItemSegment;
import dev.celestiacraft.libs.client.tooltip.InlineItemTooltipComponent.Segment;
import dev.celestiacraft.libs.client.tooltip.InlineItemTooltipComponent.TextSegment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 共享的内联物品标记解析工具类.
 * <p>
 * 提供 {@code {modid:itemid}} 语法的正则匹配、文本段解析、物品解析等通用逻辑,
 * 被 {@link InlineItemTooltipHandler}、Jade 提示框、FTB Quests 描述面板等多处复用.
 */
public final class InlineItemPatternParser {

	public static final float DEFAULT_SCALE = 0.5F;
	public static final float DEFAULT_SPEED = 20F;

	public static final Pattern ITEM_PATTERN = Pattern.compile(
			"\\{((?:\\[[^\\]]+\\])|(?:#[a-z_][a-z0-9_.\\-]*:[a-z_][a-z0-9_.\\-/]*)|(?:[a-z_][a-z0-9_.\\-]*:[a-z_][a-z0-9_.\\-/]*))(?:,\\s*(\\d+(?:\\.\\d+)?))?(?:,\\s*(\\d+(?:\\.\\d+)?))?}"
	);

	private InlineItemPatternParser() {
	}

	/**
	 * 检查文本是否包含 {@code {modid:itemid}} 标记.
	 */
	public static boolean hasPattern(String text) {
		return text != null && text.indexOf('{') >= 0 && ITEM_PATTERN.matcher(text).find();
	}

	/**
	 * 解析文本, 将 {@code {modid:itemid}} 标记转换为 Segment 列表.
	 *
	 * @param text        原始文本 (可能含 section 格式代码和 {modid:itemid} 标记)
	 * @param stylePrefix Component Style 转换的 section 代码前缀 (如 "§b")
	 */
	public static List<Segment> parseSegments(String text, String stylePrefix) {
		String leadingCodes = stylePrefix + extractLeadingFormatCodes(text);

		List<Segment> segments = new ArrayList<>();
		Matcher matcher = ITEM_PATTERN.matcher(text);
		int lastEnd = 0;
		boolean pastFirstSegment = false;

		while (matcher.find()) {
			if (matcher.start() > lastEnd) {
				String seg = text.substring(lastEnd, matcher.start());
				if (pastFirstSegment && !leadingCodes.isEmpty()) {
					seg = leadingCodes + seg;
				}
				segments.add(new TextSegment(seg));
			}
			pastFirstSegment = true;

			String identifier = matcher.group(1);
			String scaleStr = matcher.group(2);
			String speedStr = matcher.group(3);
			float scale = (scaleStr != null) ? Float.parseFloat(scaleStr) : DEFAULT_SCALE;
			float speed = (speedStr != null) ? Float.parseFloat(speedStr) : DEFAULT_SPEED;

			Item item = resolvePatternItem(identifier, speed);
			if (item == null) {
				lastEnd = matcher.end();
				continue;
			}
			segments.add(new ItemSegment(new ItemStack(item), scale));

			lastEnd = matcher.end();
		}

		if (lastEnd < text.length()) {
			String seg = text.substring(lastEnd);
			if (pastFirstSegment && !leadingCodes.isEmpty()) {
				seg = leadingCodes + seg;
			}
			segments.add(new TextSegment(seg));
		}

		return segments;
	}

	/**
	 * 解析标识符 (单物品 / 数组轮播 / 标签轮播) 为当前帧应显示的物品.
	 *
	 * @return 解析到的物品, 无法解析时返回 null
	 */
	@Nullable
	public static Item resolvePatternItem(String identifier, float speed) {
		if (identifier.startsWith("[")) {
			List<Item> items = parseItemArray(identifier);
			if (items.isEmpty()) return null;
			return pickCarouselItem(items, speed);
		} else if (identifier.startsWith("#")) {
			List<Item> items = resolveTagItems(identifier.substring(1));
			if (items.isEmpty()) return null;
			return pickCarouselItem(items, speed);
		} else {
			Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(identifier));
			if (item == null || item == Items.AIR) return null;
			return item;
		}
	}

	/**
	 * 解析数组格式 {@code [modid:item1,#modid:tag,...]} 为物品列表.
	 */
	public static List<Item> parseItemArray(String arrayContent) {
		String inner = arrayContent.substring(1, arrayContent.length() - 1);
		List<Item> items = new ArrayList<>();
		for (String entry : inner.split(",")) {
			String trimmed = entry.trim();
			if (trimmed.isEmpty()) continue;
			if (trimmed.startsWith("#")) {
				items.addAll(resolveTagItems(trimmed.substring(1)));
			} else {
				Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(trimmed));
				if (item != null && item != Items.AIR) {
					items.add(item);
				}
			}
		}
		return items;
	}

	/**
	 * 将标签 ID 解析为物品列表.
	 */
	public static List<Item> resolveTagItems(String tagId) {
		TagKey<Item> tagKey = TagKey.create(Registries.ITEM, ResourceLocation.parse(tagId));
		ITagManager<Item> tagManager = ForgeRegistries.ITEMS.tags();
		if (tagManager == null) return List.of();
		List<Item> result = new ArrayList<>();
		tagManager.getTag(tagKey).forEach(result::add);
		return result;
	}

	/**
	 * 根据 tick 计数从物品列表中选取当前轮播物品.
	 */
	public static Item pickCarouselItem(List<Item> items, float speed) {
		long tickCounter = System.currentTimeMillis() / 50;
		int interval = Math.max(1, (int) speed);
		int index = (int) ((tickCounter / interval) % items.size());
		return items.get(index);
	}

	/**
	 * 提取文本开头连续的 section 格式代码 (如 "§b§l").
	 */
	public static String extractLeadingFormatCodes(String text) {
		StringBuilder codes = new StringBuilder();
		int i = 0;
		while (i + 1 < text.length() && text.charAt(i) == '\u00A7') {
			codes.append(text, i, i + 2);
			i += 2;
		}
		return codes.toString();
	}

	/**
	 * 将 Component 的 Style color 转换为 section 颜色代码.
	 */
	public static String styleToFormatCode(Style style) {
		TextColor color = style.getColor();
		if (color == null) return "";
		int colorValue = color.getValue();
		for (ChatFormatting cf : ChatFormatting.values()) {
			if (cf.isColor() && cf.getColor() != null && cf.getColor() == colorValue) {
				return "\u00A7" + cf.getChar();
			}
		}
		return "";
	}
}
