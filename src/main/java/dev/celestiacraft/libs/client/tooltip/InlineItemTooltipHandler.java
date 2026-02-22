package dev.celestiacraft.libs.client.tooltip;

import com.mojang.datafixers.util.Either;
import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.client.tooltip.InlineItemTooltipComponent.ItemSegment;
import dev.celestiacraft.libs.client.tooltip.InlineItemTooltipComponent.Segment;
import dev.celestiacraft.libs.client.tooltip.InlineItemTooltipComponent.TextSegment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Forge 事件处理器: 扫描所有普通 tooltip 文本中的 {@code {modid:itemid}} 或 {@code {modid:itemid,scale}} 标记,
 * 将其替换为 {@link InlineItemTooltipComponent} 以渲染内联物品图标.
 * <p>
 * 支持自定义缩放: {@code {minecraft:stone,1.0}} 或 {@code {minecraft:stone, 0.75}}.
 * 不指定 scale 时默认 0.5.
 * <p>
 * 支持轮播动画:
 * <ul>
 *   <li>数组轮播: {@code {[minecraft:stone,minecraft:dirt],0.5,20}} — 在列出的物品间循环</li>
 *   <li>标签轮播: {@code {#forge:ingots,0.5,20}} — 在标签内所有物品间循环</li>
 * </ul>
 * 第三个参数 speed 为切换间隔 (tick), 默认 20 (1秒/物品).
 * <p>
 * 适用于物品 tooltip, JEI 信息, 以及所有经过 {@code GuiGraphics.renderTooltip} 的 tooltip.
 */
@Mod.EventBusSubscriber(modid = NebulaLibs.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class InlineItemTooltipHandler {

	private static final float DEFAULT_SCALE = 0.5F;
	private static final float DEFAULT_SPEED = 20F;

	private static final Pattern ITEM_PATTERN = Pattern.compile(
			"\\{((?:\\[[^\\]]+\\])|(?:#[a-z_][a-z0-9_.\\-]*:[a-z_][a-z0-9_.\\-/]*)|(?:[a-z_][a-z0-9_.\\-]*:[a-z_][a-z0-9_.\\-/]*))(?:,\\s*(\\d+(?:\\.\\d+)?))?(?:,\\s*(\\d+(?:\\.\\d+)?))?}"
	);

	@SubscribeEvent
	public static void onGatherComponents(RenderTooltipEvent.GatherComponents event) {
		List<Either<FormattedText, TooltipComponent>> elements = event.getTooltipElements();

		for (int i = 0; i < elements.size(); i++) {
			Either<FormattedText, TooltipComponent> entry = elements.get(i);
			Optional<FormattedText> leftOpt = entry.left();
			if (leftOpt.isEmpty()) {
				continue;
			}

			FormattedText text = leftOpt.get();
			String str = text.getString();
			if (!ITEM_PATTERN.matcher(str).find()) {
				continue;
			}

			// 提取 Component 的 Style, 转换为 § 代码前缀
			String stylePrefix = "";
			if (text instanceof Component comp) {
				stylePrefix = styleToFormatCode(comp.getStyle());
			}

			List<Segment> segments = parseSegments(str, stylePrefix);
			if (!segments.isEmpty()) {
				elements.set(i, Either.right(new InlineItemTooltipComponent(segments)));
			}
		}
	}

	/**
	 * 解析文本, 将 {@code {modid:itemid}} 或 {@code {modid:itemid,scale}} 标记转换为 Segment 列表.
	 * <p>
	 * TextSegment 存储原始 String (含 § 码), 由 Font 的 String 方法正确渲染.
	 * 自动处理 § 格式代码的跨段传播.
	 *
	 * @param text        原始文本 (可能含 § 格式代码和 {modid:itemid} 标记)
	 * @param stylePrefix Component Style 转换的 § 代码前缀 (如 "§b")
	 */
	private static List<Segment> parseSegments(String text, String stylePrefix) {
		// 收集文本开头的 § 格式代码 + Component 的 Style 代码, 用于传播到后续段
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

			Item item;
			if (identifier.startsWith("[")) {
				List<Item> items = parseItemArray(identifier);
				if (items.isEmpty()) {
					lastEnd = matcher.end();
					continue;
				}
				item = pickCarouselItem(items, speed);
			} else if (identifier.startsWith("#")) {
				List<Item> items = resolveTagItems(identifier.substring(1));
				if (items.isEmpty()) {
					lastEnd = matcher.end();
					continue;
				}
				item = pickCarouselItem(items, speed);
			} else {
				ResourceLocation itemRL = ResourceLocation.parse(identifier);
				item = ForgeRegistries.ITEMS.getValue(itemRL);
				if (item == null || item == Items.AIR) {
					lastEnd = matcher.end();
					continue;
				}
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
	 * 提取文本开头连续的 § 格式代码 (如 "§b§l" => "§b§l").
	 */
	private static String extractLeadingFormatCodes(String text) {
		StringBuilder codes = new StringBuilder();
		int i = 0;
		while (i + 1 < text.length() && text.charAt(i) == '§') {
			codes.append(text, i, i + 2);
			i += 2;
		}
		return codes.toString();
	}

	/**
	 * 将 Component 的 Style color 转换为 § 颜色代码.
	 * 例如 AQUA style → "§b", RED style → "§c".
	 * 如果 Style 没有颜色或不是标准 16 色, 返回空字符串.
	 */
	private static String styleToFormatCode(Style style) {
		TextColor color = style.getColor();

		if (color == null) {
			return "";
		}
		int colorValue = color.getValue();
		for (ChatFormatting cf : ChatFormatting.values()) {
			if (cf.isColor() && cf.getColor() != null && cf.getColor() == colorValue) {
				return "§" + cf.getChar();
			}
		}
		return "";
	}

	/**
	 * 根据 tick 计数从物品列表中选取当前轮播物品.
	 */
	private static Item pickCarouselItem(List<Item> items, float speed) {
		long tickCounter = System.currentTimeMillis() / 50;
		int interval = Math.max(1, (int) speed);
		int index = (int) ((tickCounter / interval) % items.size());
		return items.get(index);
	}

	/**
	 * 解析数组格式 {@code [modid:item1,#modid:tag,...]} 为物品列表.
	 * 数组内条目以 {@code #} 开头时视为标签, 展开为该标签下所有物品.
	 */
	private static List<Item> parseItemArray(String arrayContent) {
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
	private static List<Item> resolveTagItems(String tagId) {
		TagKey<Item> tagKey = TagKey.create(Registries.ITEM, ResourceLocation.parse(tagId));
		ITagManager<Item> tagManager = ForgeRegistries.ITEMS.tags();
		if (tagManager == null) return List.of();

		return tagManager.getTag(tagKey)
				.stream()
				.collect(Collectors.toList());
	}
}