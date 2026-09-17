package dev.celestiacraft.libs.common.material;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <h2>MaterialKinds</h2>
 *
 * <p>
 * 本模组 <b>内置</b> 的材料分类, 以及创建自定义分类的工厂.
 * </p>
 *
 * <pre>{@code
 * // 自定义分类(例如 Mekanism 气体)
 * public static final IMaterialKind GAS = MaterialKinds.of("gas")
 *         .tagDirectories("tags/mekanism/gas")
 *         .build();
 * }</pre>
 *
 * @see IMaterialKind
 */
public class MaterialKinds {
	/**
	 * 物品: 标签写入 {@code tags/items}
	 */
	public static final IMaterialKind ITEM = of("item")
			.tagDirectories(MaterialAssets.ITEM_TAG_DIRECTORY)
			.build();

	/**
	 * 方块 + 方块物品: 标签同时写入 {@code tags/blocks} 与 {@code tags/items}
	 */
	public static final IMaterialKind BLOCK = of("block")
			.tagDirectories(MaterialAssets.BLOCK_TAG_DIRECTORY, MaterialAssets.ITEM_TAG_DIRECTORY)
			.build();

	/**
	 * 流体: 标签写入 {@code tags/fluids}
	 */
	public static final IMaterialKind FLUID = of("fluid")
			.tagDirectories(MaterialAssets.FLUID_TAG_DIRECTORY)
			.build();

	/**
	 * Mekanism 矿浆: 标签写入 {@code tags/mekanism/slurry}
	 */
	public static final IMaterialKind SLURRY = of("slurry")
			.tagDirectories(MaterialAssets.SLURRY_TAG_DIRECTORY)
			.build();

	private static final List<IMaterialKind> VALUES;
	private static final Map<String, IMaterialKind> BY_ID;

	static {
		VALUES = List.of(ITEM, BLOCK, FLUID, SLURRY);

		Map<String, IMaterialKind> byId = new LinkedHashMap<>();

		for (IMaterialKind kind : VALUES) {
			byId.put(kind.id(), kind);
		}

		BY_ID = Map.copyOf(byId);
	}

	private MaterialKinds() {
	}

	/**
	 * @return 所有内置材料分类
	 */
	public static List<IMaterialKind> values() {
		return VALUES;
	}

	/**
	 * 按 ID 查找内置材料分类
	 *
	 * @param id 分类 ID, 例如 {@code item}
	 * @return 材料分类, 不存在时返回 null
	 */
	@Nullable
	public static IMaterialKind byId(String id) {
		return BY_ID.get(id);
	}

	/**
	 * 创建一个自定义材料分类
	 *
	 * @param id 分类 ID
	 * @return 分类构造器
	 */
	public static Builder of(String id) {
		return new Builder(id);
	}

	/**
	 * 材料分类构造器
	 */
	public static class Builder {
		private final String id;
		private final List<String> tagDirectories;

		private Builder(String id) {
			this.id = Objects.requireNonNull(id, "id");
			tagDirectories = new ArrayList<>();
		}

		/**
		 * 设置标签写入的目录
		 *
		 * @param directories 目录, 例如 {@code tags/mekanism/gas}
		 * @return 当前构造器
		 */
		public Builder tagDirectories(String... directories) {
			tagDirectories.addAll(Arrays.asList(directories));
			return this;
		}

		public IMaterialKind build() {
			return new CustomKind(this);
		}
	}

	private static class CustomKind implements IMaterialKind {
		private final String id;
		private final List<String> tagDirectories;

		private CustomKind(Builder builder) {
			id = builder.id;
			tagDirectories = List.copyOf(builder.tagDirectories);
		}

		@Override
		public String id() {
			return id;
		}

		@Override
		public List<String> tagDirectories() {
			return tagDirectories;
		}

		@Override
		public boolean equals(Object object) {
			return this == object || object instanceof IMaterialKind kind && id.equals(kind.id());
		}

		@Override
		public int hashCode() {
			return id.hashCode();
		}

		@Override
		public String toString() {
			return "IMaterialKind[" + id + "]";
		}
	}
}