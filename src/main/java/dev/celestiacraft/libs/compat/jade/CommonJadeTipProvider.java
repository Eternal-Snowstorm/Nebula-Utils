package dev.celestiacraft.libs.compat.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElement.Align;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.impl.ui.TextElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Jade tooltip 后处理器, 自动扫描所有 tooltip 文本中的 {@code {modid:itemid}} 标记,
 * 并将其替换为内联的物品/方块小图标.
 * <p>
 * 支持自定义图标缩放: {@code {modid:itemid,scale}}, 例如 {@code {minecraft:stone,1.0}}.
 * 逗号后可带空格: {@code {minecraft:stone, 0.75}}. 不指定 scale 时默认 0.5.
 *
 * <h2>通过 lang 使用 (推荐)</h2>
 * <p>直接在 KubeJS lang 翻译值中写入标记, 无需额外 API 调用:</p>
 * <pre>{@code
 * // KubeJS lang script
 * addCommonJadeTipLang("immersiveindustry:crucible",
 *     "自身最多提供 1000°C, 使用{immersiveengineering:blastfurnace_preheater,0.75}预热器升温")
 * }</pre>
 * <p>渲染效果: {@code "自身最多提供 1000°C, 使用 [图标] 预热器升温"}</p>
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

	private static final Pattern ITEM_PATTERN = Pattern.compile(
			"\\{([a-z_][a-z0-9_.\\-]*:[a-z_][a-z0-9_.\\-/]*)(?:,\\s*(\\d+(?:\\.\\d+)?))?}"
	);

	/**
	 * (可选) 通过 Java API 直接注册 Jade 提示, 支持 {@code {modid:itemid}} 内联图标.
	 *
	 * @param blockId 方块注册名, 例如 {@code "immersiveindustry:crucible"}
	 * @param tipText 提示文本, 可包含 {@code {modid:itemid}} 标记
	 */
	public static void addCommonJadeTipLang(String blockId, String tipText) {
		TIPS.computeIfAbsent(ResourceLocation.parse(blockId), k -> new ArrayList<>()).add(tipText);
	}

	public static void removeTips(String blockId) {
		TIPS.remove(ResourceLocation.parse(blockId));
	}

	public static void clearAllTips() {
		TIPS.clear();
	}

	static void onTooltipCollected(ITooltip tooltip, Accessor<?> accessor) {
		// 添加通过 Java API 直接注册的提示
		if (accessor instanceof BlockAccessor blockAccessor) {
			ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(blockAccessor.getBlock());
			if (blockId != null) {
				List<String> tips = TIPS.get(blockId);
				if (tips != null) {
					IElementHelper helper = IElementHelper.get();
					for (String tipText : tips) {
						List<IElement> elements = parseElements(tipText, helper, Style.EMPTY.withColor(ChatFormatting.AQUA));
						if (!elements.isEmpty()) {
							tooltip.add(elements);
						}
					}
				}
			}
		}

		// 自动扫描所有已有 tooltip 行, 替换 {modid:itemid} 标记为物品图标
		processInlineItemMarkup(tooltip);
	}

	/**
	 * 扫描 tooltip 中所有行, 如果某个文本元素包含 {@code {modid:itemid}} 或 {@code {modid:itemid,scale}} 标记,
	 * 则将该元素拆分为: 文本 + 物品图标 + 文本, 保留原始文本样式.
	 */
	private static void processInlineItemMarkup(ITooltip tooltip) {
		IElementHelper helper = IElementHelper.get();

		for (int i = 0; i < tooltip.size(); i++) {
			List<IElement> elements = tooltip.get(i, Align.LEFT);

			// 快速检查: 这一行是否有需要处理的标记
			boolean needsRebuild = false;
			for (IElement element : elements) {
				String msg = element.getCachedMessage();
				if (msg != null && ITEM_PATTERN.matcher(msg).find()) {
					needsRebuild = true;
					break;
				}
			}
			if (!needsRebuild) continue;

			// 重建这一行的元素列表
			List<IElement> newElements = new ArrayList<>();
			for (IElement element : elements) {
				String msg = element.getCachedMessage();
				if (msg == null || !ITEM_PATTERN.matcher(msg).find()) {
					// 没有标记, 保留原样
					newElements.add(element);
					continue;
				}

				// 从原始 Component 中提取样式 (颜色等)
				Style style = extractStyle(element);
				newElements.addAll(parseElements(msg, helper, style));
			}

			// 直接替换行内元素 (get 返回的是可变的内部列表)
			elements.clear();
			elements.addAll(newElements);
		}
	}

	/**
	 * 尝试从 IElement 中提取原始 Component 的 Style.
	 * 如果无法提取, 返回空样式.
	 */
	private static Style extractStyle(IElement element) {
		if (element instanceof TextElement te && te.text instanceof Component comp) {
			return comp.getStyle();
		}
		return Style.EMPTY;
	}

	/**
	 * 将包含 {@code {modid:itemid}} 或 {@code {modid:itemid,scale}} 标记的文本解析为 IElement 列表.
	 * <p>
	 * 纯文本部分使用指定的 Style 渲染, 标记部分替换为物品图标.
	 * 可通过 {@code ,scale} 后缀自定义图标大小, 例如 {@code {minecraft:stone,1.0}}.
	 */
	private static List<IElement> parseElements(String tipText, IElementHelper helper, Style style) {
		List<IElement> elements = new ArrayList<>();
		Matcher matcher = ITEM_PATTERN.matcher(tipText);
		int lastEnd = 0;
		while (matcher.find()) {
			if (matcher.start() > lastEnd) {
				String before = tipText.substring(lastEnd, matcher.start());
				elements.add(helper.text(Component.literal(before).withStyle(style)));
			}
			String itemId = matcher.group(1);
			String scaleStr = matcher.group(2);
			float scale = (scaleStr != null) ? Float.parseFloat(scaleStr) : DEFAULT_SCALE;
			ResourceLocation itemRL = ResourceLocation.parse(itemId);
			Item item = ForgeRegistries.ITEMS.getValue(itemRL);
			if (item != null && item != Items.AIR) {
				int pixelSize = (int) (16 * scale) + 2;
				elements.add(helper.item(new ItemStack(item), scale)
						.size(new Vec2(pixelSize, pixelSize))
						.translate(new Vec2(0, -1))
						.message(null));
				elements.add(helper.spacer(1, 0));
			}
			lastEnd = matcher.end();
		}
		if (lastEnd < tipText.length()) {
			String remaining = tipText.substring(lastEnd);
			elements.add(helper.text(Component.literal(remaining).withStyle(style)));
		}
		return elements;
	}
}
