package dev.celestiacraft.libs.common.material;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * <h2>IMaterialKind</h2>
 *
 * <p>
 * 材料 <b>分类</b> —— 描述"这类材料的内容长什么样, 标签写到哪里".
 * </p>
 *
 * <p>
 * 它是 {@link IMaterialType} 的一部分: 多个类型可以共用同一个分类
 * (例如锭 / 板 / 粉都是 {@link MaterialKinds#ITEM}), 而分类决定:
 * </p>
 *
 * <ul>
 *     <li>默认使用哪套注册流程(物品 / 方块 / 流体 / 矿浆)</li>
 *     <li>标签写入哪些目录</li>
 * </ul>
 *
 * <p>
 * 本模组内置的分类见 {@link MaterialKinds}; 需要新分类(例如 Mekanism 气体)时,
 * 用 {@link MaterialKinds#of(String)} 创建, 并在自己的 {@link IMaterialType} 里覆盖
 * {@link IMaterialType#register(NebulaMaterial)} 实现对应的注册流程.
 * </p>
 *
 * @see MaterialKinds
 */
public interface IMaterialKind {
	/**
	 * @return 分类 ID
	 */
	String id();

	/**
	 * 该分类的标签写入哪些目录(相对 {@code data/<标签命名空间>/})
	 *
	 * <p>
	 * 例如物品是 {@code tags/items}, 而方块需要同时写入 {@code tags/blocks} 与
	 * {@code tags/items}(方块物品).
	 * </p>
	 *
	 * @return 标签目录, 空列表表示不生成标签
	 */
	default List<String> tagDirectories() {
		return List.of();
	}

	/**
	 * 该分类的标签所属的注册表 ID
	 *
	 * <p>
	 * 用于把标签 ID 包装成 {@link net.minecraft.tags.TagKey}. 方块分类的标签虽然同时写入
	 * 方块与物品两个目录, 但配方里用到的是方块物品那一份, 所以这里给物品注册表.
	 * </p>
	 *
	 * @return 注册表 ID, 例如 {@code minecraft:item}; 为 null 时不提供 {@code TagKey}
	 */
	@Nullable
	default ResourceLocation tagRegistry() {
		return null;
	}
}