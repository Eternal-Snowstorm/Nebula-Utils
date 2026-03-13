package dev.celestiacraft.libs.client;

import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.LangBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import dev.celestiacraft.libs.NebulaLibs;

/**
 * NebulaLibs 模组的语言工具类, 基于 Create 的 {@link Lang} 和 {@link LangBuilder} 。
 * <p>
 * 提供便捷的方法来创建本地化文本组件, 自动添加 {@code nebula.} 命名空间前缀。
 * 支持动态参数替换({@code %s}、{@code %d} 等格式化占位符)。
 *
 * <h2>使用示例</h2>
 * <pre>{@code
 * // 基础用法 - 创建一个 LangBuilder
 * NebulaLang.builder()
 *     .translate("tooltip.example")
 *     .style(ChatFormatting.GRAY)
 *     .addTo(tooltip);
 *
 * // 带参数的翻译
 * NebulaLang.translate("tooltip.steam_cost", steamAmount)
 *     .style(ChatFormatting.GOLD)
 *     .addTo(tooltip);
 *
 * // 直接获取 Component(不使用 builder 链式调用)
 * MutableComponent text = NebulaLang.translateDirect("message.hello", playerName);
 * }</pre>
 *
 * <h2>语言文件格式</h2>
 * 语言文件中的键需要以 {@code nebula.} 为前缀：
 * <pre>{@code
 * {
 *     "nebula.tooltip.example": "This is an example",
 *     "nebula.tooltip.steam_cost": "Requires %s mB of steam"
 * }
 * }</pre>
 *
 * @see Lang Create 的语言工具类
 * @see LangBuilder 链式文本构建器
 */
public class NebulaLang {
	public static LangBuilder builder() {
		return new LangBuilder(NebulaLibs.MODID);
	}

	/**
	 * 翻译指定的语言键并返回 {@link LangBuilder}, 支持动态参数。
	 * <p>
	 * 自动添加 {@code nebula.} 前缀到语言键。例如, 传入 {@code "tooltip.example"}
	 * 会查找 {@code "nebula.tooltip.example"} 键。
	 *
	 * @param langKey 语言键(不含 {@code nebula.} 前缀)
	 * @param args    格式化参数, 用于替换翻译文本中的占位符(如 {@code %s})
	 * @return 包含翻译结果的 LangBuilder, 可继续链式调用
	 * @see #translateDirect(String, Object...) 直接获取 Component 而非 Builder
	 */
	public static LangBuilder translate(String langKey, Object... args) {
		return builder().add(Components.translatable(
				String.format("%s.%s", NebulaLibs.MODID, langKey),
				Lang.resolveBuilders(args)
		));
	}

	public static MutableComponent translateDirect(String langKey, Object... args) {
		return Components.translatable(
				String.format("%s.%s", NebulaLibs.MODID, langKey),
				Lang.resolveBuilders(args)
		);
	}

	public static class JeiLang {
		public static MutableComponent setTranCategoryTitle(String key) {
			return Component.translatable(
					String.format("jei.category.%s.%s", NebulaLibs.MODID, key)
			);
		}
	}
}