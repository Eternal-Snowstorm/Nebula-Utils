package dev.celestiacraft.libs.common.material;

import dev.celestiacraft.libs.common.material.event.RegisterMaterialEvent;
import dev.celestiacraft.libs.compat.ICheckModLoaded;
import dev.celestiacraft.libs.compat.kubejs.MaterialKubeJSBridge;

/**
 * <h2>MaterialKubeJSHook</h2>
 *
 * <p>
 * 材料事件到 KubeJS 的桥接入口.
 * </p>
 *
 * <p>
 * 真正的桥接代码位于 {@code compat.kubejs} 包中; 这里通过一层间接调用,
 * 保证 <b>没有安装 KubeJS 时不会加载任何 KubeJS 类</b>.
 * </p>
 */
public class MaterialKubeJSHook {
	public static void post(RegisterMaterialEvent event) {
		if (!ICheckModLoaded.hasKubeJS()) {
			return;
		}

		postToKubeJS(event);
	}

	private static void postToKubeJS(RegisterMaterialEvent event) {
		MaterialKubeJSBridge.post(event);
	}
}