package top.nebula.libs.tags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

/**
 * <p>
 * AbstractTagBuilder 是一个通用的 TagKey 构建抽象基类,
 * 用于统一生成同 id 不同 namespace 的标签.
 * </p>
 *
 * <p>
 * 设计思路来源:
 * 在实际开发过程中, 经常需要为同一个 name
 * 在不同 namespace 下分别创建 Tag.
 * 原版写法需要重复构造 ResourceLocation 与 TagKey,
 * 代码较为冗余且可读性一般.
 * 因此基于这一使用场景,
 * 封装出一个支持链式调用的构建 API,
 * 使多 namespace 同名 Tag 的定义更加简洁, 清晰.
 * </p>
 *
 * <p>
 * 该类采用链式构建设计, 通过设置 namespace 后
 * 由子类实现 build() 完成最终 TagKey 创建.
 * </p>
 *
 * <p>
 * 设计目标:
 * <ul>
 *     <li>避免手动拼接 ResourceLocation</li>
 *     <li>统一不同类型 Tag 的构建流程</li>
 *     <li>提供清晰可读的链式 API</li>
 *     <li>强制在 build() 前显式指定 namespace</li>
 * </ul>
 *
 * <h2>推荐使用方式</h2>
 *
 * <p>
 * 通过上层工厂类 例如 TagsBuilder 进行创建.
 * </p>
 *
 * <pre>{@code
 * // 构建 forge:steam 和 ccb:steam 流体标签
 * TagKey<Fluid> steam = TagsBuilder.fluid("steam")
 *                 .forge()
 *                 .custom("ccb")
 *                 .build();
 *
 * // 构建自定义命名空间方块标签
 * TagKey<Block> grillSources = TagsBuilder.block("grill_sources")
 *                 .forge()
 *                 .custom(Cmi.MODID)
 *                 .build();
 * }</pre>
 *
 * <h2>命名空间方法说明</h2>
 *
 * <ul>
 *     <li>vanilla() - 设置 namespace 为 "minecraft"</li>
 *     <li>forge() - 设置 namespace 为 "forge"</li>
 *     <li>tconstruct() - 设置 namespace 为 "tconstruct"</li>
 *     <li>create() - 设置 namespace 为 "create"</li>
 *     <li>custom(String namespace) - 使用自定义 namespace</li>
 * </ul>
 *
 * <h2>内部机制说明</h2>
 *
 * <ul>
 *     <li>name 表示标签路径</li>
 *     <li>namespace 表示命名空间</li>
 *     <li>id() 会生成 namespace:name 形式的 ResourceLocation</li>
 *     <li>若未指定 namespace 调用 build(), 将抛出 IllegalStateException</li>
 * </ul>
 *
 * @param <T> Tag 所属注册类型, 例如 Item, Block, Fluid 和
 */
public abstract class AbstractTagBuilder<T> {
	protected final String name;
	protected String namespace;

	protected AbstractTagBuilder(String name) {
		this.name = name;
	}

	private AbstractTagBuilder<T> namespace(String namespace) {
		this.namespace = namespace;
		return this;
	}

	public AbstractTagBuilder<T> vanilla() {
		return namespace("minecraft");
	}

	public AbstractTagBuilder<T> forge() {
		return namespace("forge");
	}

	public AbstractTagBuilder<T> cmi() {
		return namespace("cmi");
	}

	public AbstractTagBuilder<T> nebulaTinker() {
		return namespace("nebula_tinker");
	}

	public AbstractTagBuilder<T> industrialPlatform() {
		return namespace("industrial_platform");
	}

	public AbstractTagBuilder<T> tconstruct() {
		return namespace("tconstruct");
	}

	public AbstractTagBuilder<T> create() {
		return namespace("create");
	}

	public AbstractTagBuilder<T> custom(String namespace) {
		return namespace(namespace);
	}

	protected ResourceLocation id() {
		if (namespace == null) {
			throw new IllegalStateException("Tag namespace not specified: " + name);
		}
		return ResourceLocation.fromNamespaceAndPath(namespace, name);
	}

	public abstract TagKey<T> build();
}