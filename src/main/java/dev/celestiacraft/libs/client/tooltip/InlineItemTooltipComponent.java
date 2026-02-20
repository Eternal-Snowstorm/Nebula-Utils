package dev.celestiacraft.libs.client.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 自定义 TooltipComponent, 包含一系列文本段和物品图标段.
 * <p>
 * 用于在普通 Minecraft tooltip 中内联渲染物品/方块小图标.
 */
public record InlineItemTooltipComponent(List<Segment> segments) implements TooltipComponent {

	public sealed interface Segment permits TextSegment, ItemSegment {}

	/**
	 * 文本段, 存储原始字符串 (可能包含 § 格式代码).
	 * 使用 String 而非 Component, 以确保 Font 的 String 方法正确解释 § 码.
	 */
	public record TextSegment(String text) implements Segment {}

	public record ItemSegment(ItemStack stack, float scale) implements Segment {}
}
