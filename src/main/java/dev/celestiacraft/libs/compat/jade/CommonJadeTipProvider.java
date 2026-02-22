package dev.celestiacraft.libs.compat.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElement.Align;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.impl.ui.TextElement;
import snownee.jade.util.ModIdentification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Jade tooltip 后处理器, 自动扫描所有 tooltip 文本中的 {@code {modid:itemid}} 标记,
 * 并将其替换为内联的物品/方块小图标.
 * <p>
 * 支持自定义图标缩放: {@code {modid:itemid,scale}}, 例如 {@code {minecraft:stone,1.0}}.
 * 逗号后可带空格: {@code {minecraft:stone, 0.75}}. 不指定 scale 时默认 0.5.
 * <p>
 * 支持轮播动画:
 * <ul>
 *   <li>数组轮播: {@code {[minecraft:stone,minecraft:dirt],0.5,20}} — 在列出的物品间循环</li>
 *   <li>标签轮播: {@code {#forge:ingots,0.5,20}} — 在标签内所有物品间循环</li>
 * </ul>
 * 第三个参数 speed 为切换间隔 (tick), 默认 20 (1秒/物品).
 *
 * <h2>通过 lang 使用 (推荐)</h2>
 * <p>直接在 KubeJS lang 翻译值中写入标记, 无需额外 API 调用:</p>
 * <pre>{@code
 * addCommonJadeTipLang("immersiveindustry:crucible",
 *     "自身最多提供 1000°C, 使用{immersiveengineering:blastfurnace_preheater,0.75}预热器升温")
 * }</pre>
 *
 * <h2>通过 Java API 使用</h2>
 * <pre>{@code
 * CommonJadeTipProvider.addCommonJadeTipLang("immersiveindustry:crucible",
 *     "自身最多提供 1000°C, 使用{immersiveengineering:blastfurnace_preheater}预热器升温");
 * }</pre>
 */
public class CommonJadeTipProvider {
	private static final Map<ResourceLocation, List<String>> TIPS = new HashMap<>();
	private static final float DEFAULT_SCALE = 0.5F;
	private static final float DEFAULT_SPEED = 20F;
	private static final Pattern ITEM_PATTERN = Pattern.compile(
			"\\{((?:\\[[^\\]]+\\])|(?:#[a-z_][a-z0-9_.\\-]*:[a-z_][a-z0-9_.\\-/]*)|(?:[a-z_][a-z0-9_.\\-]*:[a-z_][a-z0-9_.\\-/]*))(?:,\\s*(\\d+(?:\\.\\d+)?))?(?:,\\s*(\\d+(?:\\.\\d+)?))?}"
	);

	public static void addCommonJadeTipLang(String blockId, String tipText) {
		TIPS.computeIfAbsent(ResourceLocation.parse(blockId), (location) -> {
			return new ArrayList<>();
		}).add(tipText);
	}

	public static void removeTips(String blockId) {
		TIPS.remove(ResourceLocation.parse(blockId));
	}

	public static void clearAllTips() {
		TIPS.clear();
	}

	static void onTooltipCollected(ITooltip tooltip, Accessor<?> accessor) {
		if (accessor instanceof EntityAccessor ea && ea.getEntity() instanceof ItemEntity ie) {
			rebuildItemEntityTooltip(tooltip, ie);
		}
		addRegisteredTips(tooltip, accessor);
		processInlineItemMarkup(tooltip);
	}

	/**
	 * 移除 Jade 内置 ItemTooltipProvider 的截断行, 用完整原文重新解析 (避免 {} 标记被截断).
	 */
	private static void rebuildItemEntityTooltip(ITooltip tooltip, ItemEntity itemEntity) {
		if (tooltip.get(Identifiers.MC_ITEM_TOOLTIP).isEmpty()) {
			return;
		}
		tooltip.remove(Identifiers.MC_ITEM_TOOLTIP);
		ItemStack stack = itemEntity.getItem();
		List<Component> lines;

		try {
			lines = stack.getTooltipLines(null, TooltipFlag.Default.NORMAL);
		} catch (Throwable e) {
			return;
		}
		String modName = ModIdentification.getModName(stack);
		IElementHelper helper = IElementHelper.get();
		for (int i = 1; i < lines.size(); i++) {
			Component line = stripColor(lines.get(i));
			String text = line.getString();
			if (Objects.equals(ChatFormatting.stripFormatting(text), modName)) {
				continue;
			}
			addLineOrParsed(tooltip, helper, text, line.getStyle());
		}
	}

	private static void addRegisteredTips(ITooltip tooltip, Accessor<?> accessor) {
		ResourceLocation tipKey = resolveTipKey(accessor);
		if (tipKey == null) {
			return;
		}
		List<String> tips = TIPS.get(tipKey);
		if (tips == null) {
			return;
		}
		IElementHelper helper = IElementHelper.get();
		Style aqua = Style.EMPTY.withColor(ChatFormatting.AQUA);
		for (String tipText : tips) {
			List<IElement> elements = parseElements(tipText, helper, aqua);
			if (!elements.isEmpty()) {
				tooltip.add(elements);
			}
		}
	}

	private static ResourceLocation resolveTipKey(Accessor<?> accessor) {
		if (accessor instanceof BlockAccessor ba) {
			return ForgeRegistries.BLOCKS.getKey(ba.getBlock());
		}
		if (!(accessor instanceof EntityAccessor ea) || !(ea.getEntity() instanceof ItemEntity ie)) {
			return null;
		}
		Item item = ie.getItem().getItem();
		if (item instanceof BlockItem bi) {
			ResourceLocation blockKey = ForgeRegistries.BLOCKS.getKey(bi.getBlock());
			if (blockKey != null && TIPS.containsKey(blockKey)) {
				return blockKey;
			}
		}
		return ForgeRegistries.ITEMS.getKey(item);
	}

	private static void processInlineItemMarkup(ITooltip tooltip) {
		IElementHelper helper = IElementHelper.get();

		for (int i = 0; i < tooltip.size(); i++) {
			List<IElement> row = tooltip.get(i, Align.LEFT);
			List<IElement> rebuilt = rebuildRow(row, helper);
			if (rebuilt != null) {
				row.clear();
				row.addAll(rebuilt);
			}
		}
	}

	/**
	 * 如果行中有元素包含 {} 标记则返回重建后的列表, 否则返回 null (无需修改).
	 */
	private static List<IElement> rebuildRow(List<IElement> row, IElementHelper helper) {
		List<IElement> result = null;
		for (int j = 0; j < row.size(); j++) {
			IElement element = row.get(j);
			String msg = element.getCachedMessage();
			boolean hasPattern = msg != null && msg.indexOf('{') >= 0 && ITEM_PATTERN.matcher(msg).find();
			if (hasPattern && result == null) {
				result = new ArrayList<>(row.subList(0, j));
			}
			if (result != null) {
				if (hasPattern) {
					Style style = (element instanceof TextElement te && te.text instanceof Component c)
							? c.getStyle() : Style.EMPTY;
					result.addAll(parseElements(msg, helper, style));
				} else {
					result.add(element);
				}
			}
		}
		return result;
	}

	/**
	 * 如果文本含 {} 标记则解析为图标元素行添加, 否则直接添加文本.
	 */
	private static void addLineOrParsed(ITooltip tooltip, IElementHelper helper, String text, Style style) {
		if (text.indexOf('{') >= 0 && ITEM_PATTERN.matcher(text).find()) {
			List<IElement> elements = parseElements(text, helper, style);
			if (!elements.isEmpty()) {
				tooltip.add(elements);
			}
		} else {
			tooltip.add(helper.text(Component.literal(text).withStyle(style)));
		}
	}

	/**
	 * 将含 {@code {modid:itemid}} 标记的文本解析为 IElement 列表.
	 * 自动将文本开头的 § 格式码传播到 {} 标记后的文本段.
	 */
	private static List<IElement> parseElements(String text, IElementHelper helper, Style style) {
		String leadingCodes = extractLeadingFormatCodes(text);
		List<IElement> elements = new ArrayList<>();
		Matcher matcher = ITEM_PATTERN.matcher(text);
		int lastEnd = 0;
		boolean pastFirstItem = false;

		while (matcher.find()) {
			if (matcher.start() > lastEnd) {
				String seg = text.substring(lastEnd, matcher.start());
				if (pastFirstItem && !leadingCodes.isEmpty()) {
					seg = leadingCodes + seg;
				}
				elements.add(helper.text(Component.literal(seg).withStyle(style)));
			}
			pastFirstItem = true;
			addIconElements(elements, helper, matcher.group(1), matcher.group(2), matcher.group(3));
			lastEnd = matcher.end();
		}
		if (lastEnd < text.length()) {
			String seg = text.substring(lastEnd);
			if (pastFirstItem && !leadingCodes.isEmpty()) {
				seg = leadingCodes + seg;
			}
			elements.add(helper.text(Component.literal(seg).withStyle(style)));
		}
		return elements;
	}

	/**
	 * 解析标识符并添加图标元素. 支持单物品、数组轮播、标签轮播三种格式.
	 */
	private static void addIconElements(List<IElement> elements, IElementHelper helper, String identifier, String scaleStr, String speedStr) {
		float scale = (scaleStr != null) ? Float.parseFloat(scaleStr) : DEFAULT_SCALE;
		float speed = (speedStr != null) ? Float.parseFloat(speedStr) : DEFAULT_SPEED;
		Item item;

		if (identifier.startsWith("[")) {
			List<Item> items = parseItemArray(identifier);
			if (items.isEmpty()) return;
			item = pickCarouselItem(items, speed);
		} else if (identifier.startsWith("#")) {
			List<Item> items = resolveTagItems(identifier.substring(1));
			if (items.isEmpty()) return;
			item = pickCarouselItem(items, speed);
		} else {
			item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(identifier));
			if (item == null || item == Items.AIR) return;
		}

		int pixelSize = (int) (16 * scale) + 2;
		elements.add(helper.item(item.getDefaultInstance(), scale)
				.size(new Vec2(pixelSize, pixelSize))
				.translate(new Vec2(0, -1))
				.message(null));
		elements.add(helper.spacer(1, 0));
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

			if (trimmed.isEmpty()) {
				continue;
			}
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

		if (tagManager == null) {
			return List.of();
		}

		return tagManager.getTag(tagKey)
				.stream()
				.collect(Collectors.toList());
	}

	private static Component stripColor(Component line) {
		if (line instanceof MutableComponent mc && mc.getStyle().getColor() != null) {
			mc.setStyle(mc.getStyle().withColor((TextColor) null));
		}
		return line;
	}

	/**
	 * 提取 '§' 符号
	 */
	private static String extractLeadingFormatCodes(String text) {
		int i = 0;

		while (i + 1 < text.length() && text.charAt(i) == '§') {
			i += 2;
		}
		return i > 0 ? text.substring(0, i) : "";
	}
}
