package dev.celestiacraft.libs.compat;

import net.minecraftforge.fml.ModList;

/**
 * 用于检测指定模组是否已加载的工具接口.
 *
 * <p>
 * 该接口基于 {@link ModList} 提供的查询能力,
 * 对常用模组的加载状态进行了简单封装,
 * 便于在运行时进行条件逻辑判断或兼容分支控制.
 * </p>
 *
 * <p>
 * 所有方法均为 static,
 * 无需实例化即可直接调用.
 * </p>
 *
 * <h2>设计目的</h2>
 * <ul>
 *     <li>统一模组存在性检测入口</li>
 *     <li>避免在多个位置重复编写 ModList 查询代码</li>
 *     <li>提高兼容逻辑的可读性</li>
 * </ul>
 *
 * <h2>示例</h2>
 *
 * <pre>{@code
 * if (ICheckModLoaded.hasJei()) {
 *     // 注册 JEI 兼容内容
 * }
 *
 * if (ICheckModLoaded.hasCreate()) {
 *     // 注册 Create 相关内容
 * }
 * }</pre>
 *
 * <p>
 * 注意: 本工具仅用于检测模组是否存在,
 * 不保证其 API 或相关类在当前环境中一定安全可访问.
 * 在进行类引用时仍需确保使用安全的延迟加载或条件注册机制.
 * </p>
 */
public interface ICheckModLoaded {
	/**
	 * 检查指定 modid 的模组是否已加载.
	 *
	 * @param modid 模组 ID
	 * @return 若模组存在则返回 true, 否则返回 false
	 */
	static boolean hasMod(String modid) {
		return ModList.get().isLoaded(modid);
	}

	/**
	 * @return 是否已加载 Create 模组
	 */
	static boolean hasCreate() {
		return hasMod("create");
	}

	/**
	 * @return 是否已加载 Tinkers' Construct 模组
	 */
	static boolean hasTCon() {
		return hasMod("tconstruct");
	}

	/**
	 * @return 是否已加载 KubeJS 模组
	 */
	static boolean hasKubeJS() {
		return hasMod("kubejs");
	}

	/**
	 * @return 是否已加载 Jade 模组
	 */
	static boolean hasJade() {
		return hasMod("jade");
	}

	/**
	 * @return 是否已加载 JEI 模组
	 */
	static boolean hasJei() {
		return hasMod("jei");
	}

	/**
	 * @return 是否已加载 Curios 模组
	 */
	static boolean hasCurios() {
		return hasMod("curios");
	}

	/**
	 * @return 是否已加载 Patchouli 模组
	 */
	static boolean hasPatchouli() {
		return hasMod("patchouli");
	}
}