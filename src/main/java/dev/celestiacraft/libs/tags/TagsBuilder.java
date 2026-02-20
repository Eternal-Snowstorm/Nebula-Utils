package dev.celestiacraft.libs.tags;

import dev.celestiacraft.libs.tags.type.BlockTag;
import dev.celestiacraft.libs.tags.type.EntityTypeTag;
import dev.celestiacraft.libs.tags.type.FluidTag;
import dev.celestiacraft.libs.tags.type.ItemTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

/**
 * <h2>TagsBuilder</h2>
 *
 * <p>
 * Tag 构建统一入口工厂类.
 * </p>
 *
 * <p>
 * 该类作为各类型 TagBuilder 的集中访问点,
 * 用于根据注册类型快速创建对应的标签构建器实例,
 * 从而避免直接实例化具体实现类.
 * </p>
 *
 * <hr>
 *
 * <h3>设计目标</h3>
 *
 * <ul>
 *     <li>提供语义化的标签创建入口</li>
 *     <li>隐藏具体 TagBuilder 实现类</li>
 *     <li>统一不同注册类型的构建方式</li>
 *     <li>提高代码可读性与一致性</li>
 * </ul>
 *
 * <hr>
 *
 * <h3>支持的标签类型</h3>
 *
 * <ul>
 *     <li>Block</li>
 *     <li>Item</li>
 *     <li>Fluid</li>
 *     <li>EntityType</li>
 * </ul>
 *
 * <hr>
 *
 * <h3>使用示例</h3>
 *
 * <pre>{@code
 * // forge:steam
 * TagKey<Fluid> steam = TagsBuilder.fluid("steam")
 *     .forge();
 *
 * // forge:example
 * TagKey<Block> blockTag = TagsBuilder.block("example")
 *     .forge();
 *
 * // ccb:example_item
 * TagKey<Item> itemTag = TagsBuilder.item("example_item")
 *     .custom("ccb");
 *
 * // minecraft:logs
 * TagKey<Block> logs = TagsBuilder.block("logs")
 *     .vanilla();
 * }</pre>
 *
 * <p>
 * 调用流程语义为：
 * </p>
 *
 * <ol>
 *     <li>选择标签所属注册类型(block / item / fluid / entity)</li>
 *     <li>指定标签路径(name)</li>
 *     <li>通过 namespace 终态方法直接获得 {@link TagKey}</li>
 * </ol>
 *
 * <hr>
 *
 * <p>
 * 该类本身不包含构建逻辑, 
 * 具体 TagKey 创建行为由各类型 Builder 实现.
 * </p>
 *
 * @see AbstractTagBuilder
 */
public class TagsBuilder {
	public static BlockTag block(String name) {
		return new BlockTag(name);
	}

	public static ItemTag item(String name) {
		return new ItemTag(name);
	}

	public static FluidTag fluid(String name) {
		return new FluidTag(name);
	}

	public static EntityTypeTag entity(String name) {
		return new EntityTypeTag(name);
	}

	public static TagKey<Block> block(String name, String namespace) {
		return new BlockTag(name).namespace(namespace);
	}

	public static TagKey<Item> item(String name, String namespace) {
		return new ItemTag(name).namespace(namespace);
	}

	public static TagKey<Fluid> fluid(String name, String namespace) {
		return new FluidTag(name).namespace(namespace);
	}

	public static TagKey<EntityType<?>> entity(String name, String namespace) {
		return new EntityTypeTag(name).namespace(namespace);
	}
}