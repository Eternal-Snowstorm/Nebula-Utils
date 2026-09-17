package dev.celestiacraft.libs.compat.kubejs.event;

import dev.celestiacraft.libs.compat.kubejs.event.client.jei.RegisterIngredientAliasesEventJS;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

/**
 * <h2>NebulaEventJS</h2>
 *
 * <p>
 * Nebula Libs 暴露给 KubeJS 的事件组.
 * </p>
 *
 * <ul>
 *     <li>{@code NebulaEvents.registerMaterial((event) => {})} - 注册材料(启动脚本), 命名空间用
 *     {@code event.namespace("cmi")} 设置</li>
 *     <li>{@code NebulaEvents.registerJeiAliases((event) => {})} - 注册 JEI 别名(客户端脚本)</li>
 * </ul>
 */
public class NebulaEventJS {
	private static final EventGroup GROUP;
	public static final EventHandler JEI_ALIASES_EVENT;
	public static final EventHandler REGISTER_MATERIAL_EVENT;

	static {
		GROUP = EventGroup.of("NebulaEvents");

		JEI_ALIASES_EVENT = GROUP.client("registerJeiAliases", () -> {
			return RegisterIngredientAliasesEventJS.class;
		});

		REGISTER_MATERIAL_EVENT = GROUP.startup("registerMaterial", () -> {
			return RegisterMaterialEventJS.class;
		});
	}

	public static void init() {
		GROUP.register();
	}
}