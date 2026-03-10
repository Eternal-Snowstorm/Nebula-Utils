package dev.celestiacraft.libs.compat.ftbquests.client;

import dev.ftb.mods.ftblibrary.util.client.ClientTextComponentUtils;

/**
 * FTB Quests 客户端兼容初始化.
 * <p>
 * 注册 {@link InlineItemComponentParser}, 使 FTB Library 的文本解析器
 * 保留 {@code {modid:itemid}} 标记而非将其作为替代变量消费.
 * <p>
 * 本类仅在 FTB Quests 存在时被加载, 避免缺少 FTB Library 类时的 ClassNotFoundException.
 */
public class FTBQuestsClientCompat {

	public static void init() {
		ClientTextComponentUtils.addCustomParser(new InlineItemComponentParser());
	}
}
