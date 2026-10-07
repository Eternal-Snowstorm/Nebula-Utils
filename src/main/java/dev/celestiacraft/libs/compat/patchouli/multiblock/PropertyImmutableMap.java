package dev.celestiacraft.libs.compat.patchouli.multiblock;

import com.google.common.collect.ImmutableMap;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Map;

/**
 * 一个类型安全的 ImmutableMap 构建器, 
 * 专用于构建 BlockState 的 Property 映射.
 *
 * <p>
 * 相比直接使用 {@link ImmutableMap.Builder}, 
 * 该类通过泛型方法确保：
 * <pre>
 * Property<T> 只能匹配 T 类型的值
 * </pre>
 * 从而在编译期保证类型安全, 避免运行时 ClassCastException.
 * </p>
 *
 * <h2>设计目的</h2>
 * <ul>
 *     <li>为 BlockState 属性映射提供类型安全构建方式</li>
 *     <li>避免原生 Map&lt;Property&lt;?&gt;, Comparable&lt;?&gt;&gt; 的类型不匹配问题</li>
 *     <li>支持链式调用</li>
 * </ul>
 *
 * <h2>示例</h2>
 *
 * <pre>{@code
 * Map<Property<?>, Comparable<?>> map = PropertyImmutableMap.create()
 *         .add(BlockStateProperties.FACING, Direction.NORTH)
 *         .add(BlockStateProperties.LIT, true)
 *         .build();
 * }</pre>
 *
 * <p>
 * 构建后的映射通常用于：
 * <ul>
 *     <li>DefineBlockBuilder#stateMap</li>
 *     <li>自定义 BlockState 生成逻辑</li>
 * </ul>
 * </p>
 *
 * <p>
 * 注意：不允许添加 null 的 Property 或 value, 
 * 否则会抛出 IllegalArgumentException.
 * </p>
 */
public class PropertyImmutableMap {
	// 底层的ImmutableMap构建器
	private final ImmutableMap.Builder<Property<?>, Comparable<?>> internalBuilder;

	private PropertyImmutableMap() {
		this.internalBuilder = ImmutableMap.builder();
	}

	/**
	 * 创建一个新的类型安全构建器实例
	 */
	@Info("Creates a new type-safe builder instance")
	public static PropertyImmutableMap create() {
		return new PropertyImmutableMap();
	}

	/**
	 * 添加一个类型严格匹配的属性-值对
	 *
	 * @param property 方块属性(泛型类型T)
	 * @param value    对应的值(必须是T类型的实例)
	 * @param <T>      属性值的类型, 必须实现Comparable
	 * @return 构建器自身, 支持链式调用
	 */
	@Info("Adds a strictly typed property-value pair\n\n添加一个类型严格匹配的属性-值对")
	public <T extends Comparable<T>> PropertyImmutableMap add(Property<T> property, T value) {
		if (property == null) {
			throw new IllegalArgumentException("Property不能为null");
		}
		if (value == null) {
			throw new IllegalArgumentException("属性值不能为null: " + property.getName());
		}
		// 这里会进行编译期类型检查, 确保value类型与property的泛型匹配
		internalBuilder.put(property, value);
		return this;
	}

	/**
	 * 批量添加其他映射中的属性-值对
	 *
	 * @param other 其他属性映射
	 * @return 构建器自身, 支持链式调用
	 */
	@Info("Adds property-value pairs from other mappings in bulk\n\n批量添加其他映射中的属性-值对")
	public PropertyImmutableMap putAll(Map<? extends Property<?>, ? extends Comparable<?>> other) {
		internalBuilder.putAll(other);
		return this;
	}

	/**
	 * 构建不可变映射
	 *
	 * @return 包含所有添加的属性-值对的ImmutableMap
	 */
	@Info("Builds an immutable map containing all added property-value pairs as an ImmutableMap\n\n构建不可变映射, 包含所有添加的属性-值对的ImmutableMap")
	public ImmutableMap<Property<?>, Comparable<?>> build() {
		return internalBuilder.build();
	}
}