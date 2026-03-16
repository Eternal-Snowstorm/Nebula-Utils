package dev.celestiacraft.libs.compat.patchouli.multiblock;

import dev.latvian.mods.kubejs.typings.Info;
import vazkii.patchouli.api.IMultiblock;
import vazkii.patchouli.api.PatchouliAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Patchouli 多方块结构的构建器.
 *
 * <p>
 * 该类用于以声明式方式构建 {@link vazkii.patchouli.api.IMultiblock}，
 * 封装 Patchouli 原生 makeMultiblock 的参数组织流程.
 * </p>
 *
 * <h2>结构定义规则</h2>
 * <ul>
 *     <li>使用 String[][] 表示三维结构切片</li>
 *     <li>必须存在字符 '0' 作为结构中心</li>
 *     <li>每个字符需要通过 define(char, Consumer) 定义匹配规则</li>
 * </ul>
 *
 * <h2>典型使用方式</h2>
 *
 * <pre>{@code
 * IMultiblock multiblock = new MultiblockStructureBuilder(structure)
 *     .define('0', (builder) -> builder.block(Blocks.IRON_BLOCK))
 *     .define('A', (builder) -> builder.any())
 *     .build();
 * }</pre>
 *
 * <p>
 * 内部通过收集所有字符匹配规则，
 * 最终调用 {@link PatchouliAPI.IPatchouliAPI#makeMultiblock}
 * 生成 IMultiblock 实例.
 * </p>
 *
 * <p>
 * 该类本质上是一个 DSL 封装层，
 * 使多方块结构定义更加清晰、集中且可维护.
 * </p>
 */
public class MultiblockStructureBuilder {
	private final String[][] structure;
	private final List<Object> matchers = new ArrayList<>();

	/**
	 * 定义结构时必须有一个"0"的位置作为整个结构的中心位置
	 *
	 * @param structure 多维字符数组表示结构
	 */
	public MultiblockStructureBuilder(String[][] structure) {
		this.structure = structure;
	}

	/**
	 * 定义结构时必须有一个"0"的位置作为整个结构的中心位置
	 *
	 * @param structure 多维字符数组表示结构
	 */
	@Info("When defining a structure, there must be a \"0\" position as the center position of the entire structure\n\n定义结构时必须有一个\"0\"的位置作为整个结构的中心位置")
	public static MultiblockStructureBuilder create(String[][] structure) {
		return new MultiblockStructureBuilder(structure);
	}

	/**
	 * 定义结构中某个位置的匹配规则
	 *
	 * @param pos     结构字符标识
	 * @param handler Lambda 定义方块匹配规则
	 * @return 构建器自身
	 */
	@Info("Defines the matching rule for a specific position in the structure\n\n定义结构中某个位置的匹配规则")
	public MultiblockStructureBuilder define(char pos, Consumer<DefineBlockBuilder> handler) {
		handler.accept(new DefineBlockBuilder(pos, matchers));
		return this;
	}

	/**
	 * 构建 IMultiblock 对象
	 *
	 * @return Patchouli 多方块结构对象
	 */
	public IMultiblock build() {
		return PatchouliAPI.get().makeMultiblock(structure, matchers.toArray());
	}
}