package dev.celestiacraft.libs.tags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

/**
 * <h2>AbstractTagBuilder</h2>
 *
 * <p>
 * 一个通用的 {@link TagKey} 构建抽象基类,
 * 用于为不同注册类型(如 Item, Block, Fluid, EntityType)
 * 提供统一, 简洁且语义清晰的标签创建 API.
 * </p>
 *
 * <hr>
 *
 * <h3>设计背景</h3>
 *
 * <p>
 * 在 Minecraft 原生 API 中, 创建一个 TagKey 通常需要显式构造
 * {@link ResourceLocation},
 * 并调用对应类型的 create 方法, 例如:
 * </p>
 *
 * <pre>{@code
 * TagKey<Item> tag = ItemTags.create(
 *     ResourceLocation.fromNamespaceAndPath("forge", "stone")
 * );
 * }</pre>
 *
 * <p>
 * 该写法虽然明确, 但在大量标签定义场景下会显得重复, 冗长,
 * 且 namespace 与 path 需要手动拼接, 降低可读性.
 * </p>
 *
 * <p>
 * 本抽象类的目标是:
 * 将 "标签路径" 与 "命名空间选择" 拆分为两个明确阶段,
 * 并通过终态 namespace 方法直接返回最终 {@link TagKey},
 * 使 API 语义与 Minecraft 标签结构完全对齐.
 * </p>
 *
 * <hr>
 *
 * <h3>核心设计理念</h3>
 *
 * <ul>
 *     <li><b>路径优先</b> —— 先定义标签路径(name)</li>
 *     <li><b>命名空间终态</b> —— namespace 方法即最终结果</li>
 *     <li><b>无状态污染</b> —— 不存储 namespace, 不支持覆盖行为</li>
 *     <li><b>语义对齐</b> —— TagKey 本质即 namespace + path</li>
 * </ul>
 *
 * <p>
 * 每个 namespace 方法(如 {@code forge()}, {@code vanilla()} 等)
 * 都会立即构造并返回一个 {@link TagKey},
 * 而不会修改当前 Builder 的内部状态.
 * </p>
 *
 * <hr>
 *
 * <h3>推荐使用方式</h3>
 *
 * <pre>{@code
 * // forge:stone
 * TagKey<Item> stone = TagsBuilder.item("stone")
 *     .forge();
 *
 * // minecraft:logs
 * TagKey<Block> logs = TagsBuilder.block("logs")
 *     .vanilla();
 *
 * // ccb:steam
 * TagKey<Fluid> steam = TagsBuilder.fluid("steam")
 *     .custom("ccb");
 * }</pre>
 *
 * <p>
 * 上述写法读作:
 * "路径为 stone 的标签, 位于 forge 命名空间".
 * </p>
 *
 * <hr>
 *
 * <h3>与传统 Builder 的区别</h3>
 *
 * <ul>
 *     <li>不提供 build() 方法</li>
 *     <li>不缓存 namespace</li>
 *     <li>不支持多 namespace 叠加</li>
 * </ul>
 *
 * <p>
 * 这是一个"终态构建模型":
 * namespace 选择即构建行为本身.
 * </p>
 *
 * <hr>
 *
 * <h3>扩展说明</h3>
 *
 * <p>
 * 子类仅需实现 {@code build(ResourceLocation id)},
 * 用于指定不同注册类型的 TagKey 创建逻辑.
 * </p>
 *
 * <p>
 * 例如:
 * </p>
 *
 * <ul>
 *     <li>Item 使用 {@code ItemTags.create(id)}</li>
 *     <li>Block 使用 {@code BlockTags.create(id)}</li>
 *     <li>Fluid 使用 {@code FluidTags.create(id)}</li>
 *     <li>EntityType 使用 {@code TagKey.create(Registries.ENTITY_TYPE, id)}</li>
 * </ul>
 *
 * <hr>
 *
 * @param <T> 标签所属的注册类型, 例如 Item, Block, Fluid 或 EntityType
 */
public abstract class AbstractTagBuilder<T> {
	protected final String name;

	protected AbstractTagBuilder(String name) {
		this.name = name;
	}

	public TagKey<T> vanilla() {
		return namespace("minecraft");
	}

	public TagKey<T> forge() {
		return namespace("forge");
	}

	public TagKey<T> cmi() {
		return namespace("cmi");
	}

	public TagKey<T> nebulaTinker() {
		return namespace("nebula_tinker");
	}

	public TagKey<T> industrialPlatform() {
		return namespace("industrial_platform");
	}

	public TagKey<T> tconstruct() {
		return namespace("tconstruct");
	}

	public TagKey<T> create() {
		return namespace("create");
	}

	public TagKey<T> namespace(String namespace) {
		if (namespace == null || namespace.isBlank()) {
			throw new IllegalArgumentException("Tag namespace cannot be null for tag: " + name
			);
		}

		ResourceLocation id =
				ResourceLocation.fromNamespaceAndPath(namespace, name);

		return create(id);
	}

	protected abstract TagKey<T> create(ResourceLocation id);
}