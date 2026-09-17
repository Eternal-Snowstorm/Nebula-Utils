package dev.celestiacraft.libs.common.material;

import dev.latvian.mods.rhino.util.RemapForJS;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * <h2>IMiningLevel</h2>
 *
 * <p>
 * 材料方块的挖掘等级(工具等级).
 * </p>
 *
 * <p>
 * 这是一个 <b>函数式接口</b>: 只要能给出对应的方块标签 ID 就是一个等级,
 * 因此扩展非常简单 —— 整合包自己的等级标签, 其它模组的工具等级, 甚至运行时计算出来的标签都可以.
 * </p>
 *
 * <pre>{@code
 * // 直接实现(自定义等级)
 * IMiningLevel MY_LEVEL = () -> ResourceLocation.parse("mymod:needs_my_tool");
 *
 * // 或者用现成的工厂
 * IMiningLevel fromId = IMiningLevel.of(ResourceLocation.parse("mymod:needs_my_tool"));
 * IMiningLevel fromTag = IMiningLevel.of(MyModBlockTags.NEED_MY_TOOL);
 * // JS 里 of(TagKey) 的名字是 ofTag, 例如 IMiningLevel.ofTag(MyModBlockTags.NEED_MY_TOOL)
 * }</pre>
 *
 * <p>
 * 内置等级见 {@link MiningLevels}.
 * </p>
 */
@FunctionalInterface
public interface IMiningLevel {
	/**
	 * @return 该等级对应的方块标签 ID, 没有等级要求时返回 null
	 */
	@Nullable
	ResourceLocation id();

	/**
	 * @return 该等级对应的方块标签, 没有等级要求时返回 null
	 */
	@Nullable
	default TagKey<Block> tag() {
		ResourceLocation id = id();
		return id == null ? null : TagKey.create(Registries.BLOCK, id);
	}

	/**
	 * @return 是否存在等级要求
	 */
	default boolean hasTag() {
		return id() != null;
	}

	/**
	 * 基于方块标签创建一个等级
	 *
	 * @param tag 方块标签 ID
	 * @return 挖掘等级
	 */
	static IMiningLevel of(ResourceLocation tag) {
		Objects.requireNonNull(tag, "tag");
		return () -> tag;
	}

	/**
	 * 基于方块标签创建一个等级
	 *
	 * @param tag 方块标签
	 * @return 挖掘等级
	 */
	@RemapForJS("ofTag")
	static IMiningLevel of(TagKey<Block> tag) {
		Objects.requireNonNull(tag, "tag");
		return tag::location;
	}
}