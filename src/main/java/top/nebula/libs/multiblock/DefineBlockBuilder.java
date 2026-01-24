package top.nebula.libs.multiblock;

import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import vazkii.patchouli.api.PatchouliAPI;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class DefineBlockBuilder {
	private final char pos;
	private final List<Object> matchers;

	public DefineBlockBuilder(char pos, List<Object> matchers) {
		this.pos = pos;
		this.matchers = matchers;
	}

	/**
	 * 指定具体方块
	 *
	 * @param block 具体方块
	 */
	@Info("Specify a block\n\n指定具体方块")
	public void block(Block block) {
		matchers.add(pos);
		matchers.add(block);
	}

	/**
	 * 指定具体方块状态
	 *
	 * @param state 方块状态
	 */
	@Info("Specify a block states\n\n指定具体方块状态")
	public void state(BlockState state) {
		matchers.add(pos);
		matchers.add(PatchouliAPI.get().stateMatcher(state));
	}

	/**
	 * 指定方块标签
	 */
	@Info("Specify a block tag\n\n指定方块标签")
	public void tag(TagKey<Block> tag) {
		matchers.add(pos);
		matchers.add(tag);
	}

	/**
	 * 支持任意方块
	 */
	@Info("Allows the use of any block, generally used for empty spaces\n\n支持任意方块, 一般用于空位")
	public void any() {
		matchers.add(pos);
		matchers.add(PatchouliAPI.get().anyMatcher());
	}

	/**
	 * 指定方块 + Predicate<BlockState>
	 */
	@Info("Specifies a block + Predicate<BlockState>\n\n指定方块 + Predicate<BlockState>")
	public void predicate(Block block, Predicate<BlockState> pred) {
		matchers.add(pos);
		matchers.add(PatchouliAPI.get().predicateMatcher(block, pred));
	}

	/**
	 * 指定方块 + 状态映射 (Map<Property, Comparable>)
	 */
	@Info("Specifies a block + state mapping (Map<Property, Comparable>)\n\n指定方块 + 状态映射 (Map<Property, Comparable>)")
	public void stateMap(Block block, Map<Property<?>, ? extends Comparable<?>> stateMap) {
		BlockState state = block.defaultBlockState();
		for (Map.Entry<Property<?>, ? extends Comparable<?>> entry : stateMap.entrySet()) {
			state = state.setValue((Property) entry.getKey(), (Comparable) entry.getValue());
		}
		matchers.add(pos);
		matchers.add(PatchouliAPI.get().stateMatcher(state));
	}
}