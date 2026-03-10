package dev.celestiacraft.libs.compat.ftbquests.client;

import dev.ftb.mods.ftblibrary.util.CustomComponentParser;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * FTB Library 自定义组件解析器, 拦截 {@code {modid:itemid}} 内联物品标记,
 * 阻止 FTB Library 的 {@link dev.ftb.mods.ftblibrary.util.TextComponentParser} 将其作为替代变量消费.
 * <p>
 * 当检测到匹配的标记时, 将花括号重新包裹回文本,
 * 使下游的 {@link dev.celestiacraft.libs.mixin.client.ViewQuestPanelMixin} 能够检测并替换为物品图标.
 */
public class InlineItemComponentParser implements CustomComponentParser {

	/**
	 * 匹配花括号内容 (不含花括号本身).
	 * <ul>
	 *   <li>{@code modid:itemid} — 单物品</li>
	 *   <li>{@code modid:itemid,0.5} — 带缩放</li>
	 *   <li>{@code modid:itemid,0.5,20} — 带缩放和速度</li>
	 *   <li>{@code [modid:item1,modid:item2],0.5,20} — 数组轮播</li>
	 *   <li>{@code #modid:tagid,0.5,20} — 标签轮播</li>
	 * </ul>
	 */
	private static final Pattern CONTENT_PATTERN = Pattern.compile(
			"^(?:" +
					"\\[[^\\]]+\\]" +
					"|#[a-z_][a-z0-9_.\\-]*:[a-z_][a-z0-9_.\\-/]*" +
					"|[a-z_][a-z0-9_.\\-]*:[a-z_][a-z0-9_.\\-/]*" +
					")(?:,\\s*\\d+(?:\\.\\d+)?)?(?:,\\s*\\d+(?:\\.\\d+)?)?$"
	);

	@Override
	public Component parse(String string, Map<String, String> properties) {
		// 不干扰 FTB Library 内建的 image / open_url 模式
		if (properties.containsKey("image") || properties.containsKey("open_url")) {
			return null;
		}

		if (CONTENT_PATTERN.matcher(string).matches()) {
			return Component.literal("{" + string + "}");
		}
		return null;
	}
}
