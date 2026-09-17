package dev.celestiacraft.libs.common.material;

import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <h2>MiningLevels</h2>
 *
 * <p>
 * 本模组 <b>内置</b> 的挖掘等级.
 * </p>
 *
 * <p>
 * 自定义等级用 {@link IMiningLevel#of(net.minecraft.tags.TagKey)} / {@code IMiningLevel.of(ResourceLocation)},
 * 或者直接实现 {@link IMiningLevel}(函数式接口).
 * </p>
 *
 * <p>JS 侧用法:</p>
 *
 * <pre>{@code
 * event.create("chromium", MiningLevels.IRON)
 * // 自定义等级
 * event.create("chromium", IMiningLevel.ofTag(MyModBlockTags.NEED_XXX_TOOL))
 * }</pre>
 *
 * @see IMiningLevel
 */
public class MiningLevels {
	/**
	 * 无挖掘等级要求
	 */
	public static final IMiningLevel NONE = () -> null;

	public static final IMiningLevel WOODEN = IMiningLevel.of(Tags.Blocks.NEEDS_WOOD_TOOL);
	public static final IMiningLevel STONE = IMiningLevel.of(BlockTags.NEEDS_STONE_TOOL);
	public static final IMiningLevel GOLD = IMiningLevel.of(Tags.Blocks.NEEDS_GOLD_TOOL);
	public static final IMiningLevel IRON = IMiningLevel.of(BlockTags.NEEDS_IRON_TOOL);
	public static final IMiningLevel DIAMOND = IMiningLevel.of(BlockTags.NEEDS_DIAMOND_TOOL);
	public static final IMiningLevel NETHER = IMiningLevel.of(Tags.Blocks.NEEDS_NETHERITE_TOOL);

	/**
	 * {@link WOODEN} 的别名
	 */
	public static final IMiningLevel WOOD = WOODEN;
	/**
	 * {@link GOLD} 的别名
	 */
	public static final IMiningLevel GOLDEN = GOLD;
	/**
	 * {@link NETHER} 的别名
	 */
	public static final IMiningLevel NETHERITE = NETHER;

	private static final List<IMiningLevel> VALUES;
	private static final Map<String, IMiningLevel> BY_NAME;

	static {
		VALUES = List.of(NONE, WOODEN, STONE, GOLD, IRON, DIAMOND, NETHER);

		Map<String, IMiningLevel> names = new LinkedHashMap<>();
		names.put("none", NONE);
		names.put("wood", WOODEN);
		names.put("wooden", WOODEN);
		names.put("stone", STONE);
		names.put("gold", GOLD);
		names.put("golden", GOLD);
		names.put("iron", IRON);
		names.put("diamond", DIAMOND);
		names.put("nether", NETHER);
		names.put("netherite", NETHER);
		BY_NAME = Map.copyOf(names);
	}

	/**
	 * @return 所有内置挖掘等级
	 */
	public static List<IMiningLevel> values() {
		return VALUES;
	}

	/**
	 * 按名称查找内置挖掘等级(大小写不敏感, 支持别名)
	 *
	 * @param name 等级名称, 例如 {@code iron}
	 * @return 挖掘等级, 名称为空时返回 {@link #NONE}
	 * @throws IllegalArgumentException 名称无法识别时抛出
	 */
	public static IMiningLevel byName(@Nullable String name) {
		if (name == null || name.isBlank()) {
			return NONE;
		}

		IMiningLevel level = BY_NAME.get(name.trim().toLowerCase(Locale.ROOT));

		if (level == null) {
			throw new IllegalArgumentException("未知的挖掘等级: %s (可用: %s)".formatted(name, String.join(", ", BY_NAME.keySet())));
		}

		return level;
	}
}