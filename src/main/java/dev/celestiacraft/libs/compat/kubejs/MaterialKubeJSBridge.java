package dev.celestiacraft.libs.compat.kubejs;

import dev.celestiacraft.libs.common.material.event.RegisterMaterialEvent;
import dev.celestiacraft.libs.compat.kubejs.event.NebulaEventJS;
import dev.celestiacraft.libs.compat.kubejs.event.RegisterMaterialEventJS;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.script.ScriptType;

/**
 * <h2>MaterialKubeJSBridge</h2>
 *
 * <p>
 * 将 {@link RegisterMaterialEvent} 桥接给 KubeJS 的 {@code NebulaEvents.registerMaterial}.
 * </p>
 *
 * <pre>{@code
 * NebulaEvents.registerMaterial((event) => {
 *     event.namespace("cmi")
 *     event.create("chromium", MiningLevels.IRON).ingot().plate()
 * })
 * }</pre>
 */
public class MaterialKubeJSBridge {
	public static void post(RegisterMaterialEvent event) {
		EventHandler handler = NebulaEventJS.REGISTER_MATERIAL_EVENT;

		if (!handler.hasListeners()) {
			return;
		}

		handler.post(ScriptType.STARTUP, new RegisterMaterialEventJS(event));
	}
}