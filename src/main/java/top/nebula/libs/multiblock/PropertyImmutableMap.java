package top.nebula.libs.multiblock;

import com.google.common.collect.ImmutableMap;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Map;

/**
 * 类型安全的ImmutableMap构建器，专门用于BlockState属性映射
 * 确保Property与对应的值类型严格匹配
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
	 * @param property 方块属性（泛型类型T）
	 * @param value    对应的值（必须是T类型的实例）
	 * @param <T>      属性值的类型，必须实现Comparable
	 * @return 构建器自身，支持链式调用
	 */
	@Info("Adds a strictly typed property-value pair\n\n添加一个类型严格匹配的属性-值对")
	public <T extends Comparable<T>> PropertyImmutableMap add(Property<T> property, T value) {
		if (property == null) {
			throw new IllegalArgumentException("Property不能为null");
		}
		if (value == null) {
			throw new IllegalArgumentException("属性值不能为null: " + property.getName());
		}
		// 这里会进行编译期类型检查，确保value类型与property的泛型匹配
		internalBuilder.put(property, value);
		return this;
	}

	/**
	 * 批量添加其他映射中的属性-值对
	 *
	 * @param other 其他属性映射
	 * @return 构建器自身，支持链式调用
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