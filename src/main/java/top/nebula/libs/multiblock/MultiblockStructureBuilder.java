package top.nebula.libs.multiblock;

import dev.latvian.mods.kubejs.typings.Info;
import vazkii.patchouli.api.IMultiblock;
import vazkii.patchouli.api.PatchouliAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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