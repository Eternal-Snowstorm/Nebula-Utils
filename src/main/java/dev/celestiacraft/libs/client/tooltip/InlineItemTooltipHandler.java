package dev.celestiacraft.libs.client.tooltip;

import com.mojang.datafixers.util.Either;
import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.client.tooltip.InlineItemTooltipComponent.Segment;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Optional;

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
			if (!InlineItemPatternParser.hasPattern(str)) {
				continue;
			}

			String stylePrefix = "";
			if (text instanceof Component comp) {
				stylePrefix = InlineItemPatternParser.styleToFormatCode(comp.getStyle());
			}

			List<Segment> segments = InlineItemPatternParser.parseSegments(str, stylePrefix);
			if (!segments.isEmpty()) {
				elements.set(i, Either.right(new InlineItemTooltipComponent(segments)));
			}
		}
	}
}
