package dev.celestiacraft.libs.compat.patchouli.multiblock;

import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import vazkii.patchouli.api.PatchouliAPI;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Consumer;

/**
 * 用于定义 Patchouli 多方块结构中某个字符位置的匹配规则Examples
 *
 * <p>
 * 在 {@link MultiblockStructureBuilder#define(char, Consumer)} 中使用，
 * 每一个结构字符都可以通过该构建器定义其对应的方块匹配逻辑.
 * </p>
 *
 * <p>
 * 内部通过向 matchers 列表写入成对数据：
 * <pre>
 * [pos, matcher, pos, matcher, ...]
 * </pre>
 * 最终由 {@link PatchouliAPI.IPatchouliAPI#makeMultiblock} 统一解析.
 * </p>
 *
 * <h2>支持的匹配方式</h2>
 * <ul>
 *     <li>指定具体方块</li>
 *     <li>指定具体 BlockState</li>
 *     <li>指定 TagKey&lt;Block&gt;</li>
 *     <li>任意方块匹配</li>
 *     <li>方块 + Predicate&lt;BlockState&gt;</li>
 *     <li>方块 + 状态映射</li>
 * </ul>
 *
 * <p>
 * 该类仅作为 DSL 辅助工具，不持有任何结构逻辑，
 * 所有数据最终由 MultiblockStructureBuilder 统一收集.
 * </p>
 */
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
	 * 支持任意方块
	 */
	@Info("Allows the use of any block, generally used for empty spaces\n\n支持任意方块, 一般用于空位")
	public void anyBlock() {
		any();
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