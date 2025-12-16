package top.nebula.utils.multiblock;

import com.google.common.collect.ImmutableMap;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import vazkii.patchouli.api.IMultiblock;
import vazkii.patchouli.api.IStateMatcher;
import vazkii.patchouli.api.PatchouliAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class MultiblockStructureBuilder {
	private final String[][] structure;
	private final List<Object> matchers = new ArrayList<>();

	/**
	 * 定义结构时必须有一个"0"的位置作为整个结构的中心位置
	 *
	 * @param structure 定义结构
	 */
	public MultiblockStructureBuilder(String[][] structure) {
		this.structure = structure;
	}

	/**
	 * 添加方块匹配规则
	 *
	 * @param pos   结构中的字符标识
	 * @param block 支持的匹配类型:
	 *              - Block: 具体方块
	 *              - BlockState: 具体方块状态
	 *              - TagKey<Block>: 方块标签
	 *              - IStateMatcher: 状态匹配器
	 * @return 构建器自身
	 */
	public MultiblockStructureBuilder define(char pos, Block block) {
		matchers.add(pos);
		matchers.add(block);
		return this;
	}

	public MultiblockStructureBuilder define(char pos, TagKey<Block> tag) {
		matchers.add(pos);
		matchers.add(tag);
		return this;
	}

	public MultiblockStructureBuilder define(char pos, IStateMatcher matcher) {
		matchers.add(pos);
		matchers.add(matcher);
		return this;
	}

	public MultiblockStructureBuilder define(char pos, Block block, Predicate<BlockState> predicate) {
		matchers.add(pos);
		matchers.add(PatchouliAPI.get().predicateMatcher(block, predicate));
		return this;
	}

	public MultiblockStructureBuilder define(char pos, Block block, ImmutableMap<Property<?>, ? extends Comparable<?>> stateMatcher) {
		matchers.add(pos);
		BlockState state = block.defaultBlockState();
		for (Map.Entry<Property<?>, ? extends Comparable<?>> entry : stateMatcher.entrySet()) {
			Property<?> property = entry.getKey();
			Comparable<?> value = entry.getValue();
			// 使用类型强制转换解决泛型问题
			state = state.setValue((Property) property, (Comparable) value);
		}
		matchers.add(PatchouliAPI.get().stateMatcher(state));
		return this;
	}

	public IMultiblock build() {
		return PatchouliAPI.get().makeMultiblock(structure, matchers.toArray());
	}
}