package dev.celestiacraft.libs.common.material;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * <h2>MaterialTypes</h2>
 *
 * <p>
 * 本模组 <b>内置</b> 的材料类型, 以及创建自定义类型的工厂.
 * </p>
 *
 * <p>内置类型:</p>
 *
 * <ul>
 *     <li>物品: 锭 / 板 / 粒 / 粉 / 杆 / 齿轮 / 线材 / 棱镜 / 粗矿 / Mekanism 脏粉 / 团块 / 碎片 / 晶体</li>
 *     <li>方块: 金属块 / 粗矿块</li>
 *     <li>流体: 熔融流体(含桶)</li>
 *     <li>矿浆: 干净矿浆 / 脏矿浆</li>
 * </ul>
 *
 * <p>
 * 想加本模组没有的类型时不用改这里的代码, 直接用工厂构造即可:
 * </p>
 *
 * <pre>{@code
 * public static final IMaterialType SPRING = MaterialTypes.item("spring")
 *         .namePattern("%s_spring")
 *         .layers("spring/spring", "spring/spring_secondary")
 *         .overlay("spring/spring_overlay")
 *         .tags("mymod:springs", "mymod:springs/%s")
 *         .build();
 * }</pre>
 *
 * @see IMaterialType
 */
public class MaterialTypes {
	public static final IMaterialType INGOT = item("ingot")
			.namePattern("%s_ingot")
			.layers("ingot/ingot", "ingot/ingot_secondary")
			.overlay("ingot/ingot_overlay")
			.tags("forge:ingots", "forge:ingots/%s")
			.build();

	public static final IMaterialType PLATE = item("plate")
			.namePattern("%s_plate")
			.layers("plate/plate", "plate/plate_secondary")
			.overlay("plate/plate_overlay")
			.tags("forge:plates", "forge:plates/%s")
			.build();

	public static final IMaterialType NUGGET = item("nugget")
			.namePattern("%s_nugget")
			.layers("nugget/nugget", "nugget/nugget_secondary")
			.overlay("nugget/nugget_overlay")
			.tags("forge:nuggets", "forge:nuggets/%s")
			.build();

	public static final IMaterialType DUST = item("dust")
			.namePattern("%s_dust")
			.layers("dust/dust", "dust/dust_secondary")
			.overlay("dust/dust_overlay")
			.tags("forge:dusts", "forge:dusts/%s")
			.build();

	public static final IMaterialType ROD = item("rod")
			.namePattern("%s_rod")
			.layers("rod/rod", "rod/rod_secondary")
			.overlay("rod/rod_overlay")
			.tags("forge:rods", "forge:rods/%s")
			.build();

	public static final IMaterialType GEAR = item("gear")
			.namePattern("%s_gear")
			.layers("gear/gear", "gear/gear_secondary")
			.overlay("gear/gear_overlay")
			.tags("forge:gears", "forge:gears/%s")
			.build();

	public static final IMaterialType WIRE = item("wire")
			.namePattern("%s_wire")
			.layers("wire/wire", "wire/wire_secondary")
			.overlay("wire/wire_overlay")
			.tags("forge:wires", "forge:wires/%s")
			.build();

	public static final IMaterialType PRISM = item("prism")
			.namePattern("%s_prism")
			.layers("prism/prism", "prism/prism_secondary")
			.overlay("prism/prism_overlay")
			.tags("forge:prisms", "forge:prisms/%s")
			.build();

	public static final IMaterialType RAW_ORE = item("raw_ore")
			.namePattern("raw_%s")
			.layers("raw_ore")
			.tags("forge:raw_materials", "forge:raw_materials/%s")
			.build();

	public static final IMaterialType DIRTY_DUST = item("dirty")
			.namePattern("dirty_%s_dust")
			.layers("dirty/0", "dirty/1")
			.noSecondaryTint()
			.tags("mekanism:dirty_dusts", "mekanism:dirty_dusts/%s")
			.build();

	public static final IMaterialType CLUMP = item("clump")
			.namePattern("%s_clump")
			.layers("clump")
			.tags("mekanism:clumps", "mekanism:clumps/%s")
			.build();

	public static final IMaterialType SHARD = item("shard")
			.namePattern("%s_shard")
			.layers("shard")
			.tags("mekanism:shards", "mekanism:shards/%s")
			.build();

	public static final IMaterialType CRYSTAL = item("crystal")
			.namePattern("%s_crystal")
			.layers("crystal")
			.tags("mekanism:crystals", "mekanism:crystals/%s")
			.build();

	public static final IMaterialType BLOCK = block("block")
			.namePattern("%s_block")
			.texture("block/material/color/storage_blocks")
			.tags("forge:storage_blocks", "forge:storage_blocks/%s")
			.build();

	public static final IMaterialType RAW_BLOCK = block("raw_block")
			.namePattern("raw_%s_block")
			.texture("block/material/color/storage_blocks")
			.tags("forge:storage_blocks", "forge:storage_blocks/raw_%s")
			.build();

	public static final IMaterialType MOLTEN = fluid("molten")
			.namePattern("molten_%s")
			.tags("forge:molten_materials", "forge:molten_%s", "tconstruct:molten_%s")
			.build();

	public static final IMaterialType SLURRY = slurry("slurry")
			.namePattern("%s_slurry")
			.slurryTexture("mekanism:slurry/clean")
			.tags("mekanism:clean", "mekanism:clean/%s")
			.build();

	public static final IMaterialType DIRTY_SLURRY = slurry("dirty_slurry")
			.namePattern("dirty_%s_slurry")
			.slurryTexture("mekanism:slurry/dirty")
			.tags("mekanism:dirty", "mekanism:dirty/%s")
			.build();

	private static final List<IMaterialType> VALUES;
	private static final Map<String, IMaterialType> BY_ID;

	static {
		VALUES = List.of(
				INGOT, PLATE, NUGGET, DUST, ROD, GEAR, WIRE, PRISM, RAW_ORE,
				DIRTY_DUST, CLUMP, SHARD, CRYSTAL,
				BLOCK, RAW_BLOCK,
				MOLTEN,
				SLURRY, DIRTY_SLURRY
		);

		Map<String, IMaterialType> byId = new LinkedHashMap<>();

		for (IMaterialType type : VALUES) {
			byId.put(type.id(), type);
		}

		BY_ID = Map.copyOf(byId);
	}

	private MaterialTypes() {
	}

	/**
	 * @return 所有内置材料类型
	 */
	public static List<IMaterialType> values() {
		return VALUES;
	}

	/**
	 * 按 ID 查找内置材料类型
	 *
	 * @param id 类型 ID, 例如 {@code ingot}
	 * @return 材料类型, 不存在时返回 null
	 */
	@Nullable
	public static IMaterialType byId(String id) {
		return BY_ID.get(id);
	}

	/**
	 * 创建一个物品类型
	 *
	 * @param id 类型 ID
	 * @return 类型构造器
	 */
	public static Builder item(String id) {
		return of(MaterialKinds.ITEM, id);
	}

	/**
	 * 创建一个方块类型
	 *
	 * @param id 类型 ID
	 * @return 类型构造器
	 */
	public static Builder block(String id) {
		return of(MaterialKinds.BLOCK, id);
	}

	/**
	 * 创建一个流体类型
	 *
	 * @param id 类型 ID
	 * @return 类型构造器
	 */
	public static Builder fluid(String id) {
		return of(MaterialKinds.FLUID, id);
	}

	/**
	 * 创建一个 Mekanism 矿浆类型
	 *
	 * @param id 类型 ID
	 * @return 类型构造器
	 */
	public static Builder slurry(String id) {
		return of(MaterialKinds.SLURRY, id);
	}

	/**
	 * 创建一个自定义分类的材料类型
	 *
	 * @param kind 分类, 内置分类见 {@link MaterialKinds}, 也可以用 {@link MaterialKinds#of(String)} 自定义
	 * @param id   类型 ID
	 * @return 类型构造器
	 */
	public static Builder of(IMaterialKind kind, String id) {
		return new Builder(kind, id);
	}

	/**
	 * 材料类型构造器
	 */
	public static class Builder {
		private final IMaterialKind kind;
		private final String id;
		private final List<String> layers;
		private final List<String> tags;

		private String namePattern;
		private String overlay;
		private String texture;
		private ResourceLocation slurryTexture;
		private boolean tintsSecondary;

		private Function<Material, Item> itemFactory;
		private BiFunction<Material, BlockBehaviour.Properties, Block> blockFactory;
		private Function<Material, FluidType> fluidTypeFactory;

		private Builder(IMaterialKind kind, String id) {
			this.kind = Objects.requireNonNull(kind, "kind");
			this.id = Objects.requireNonNull(id, "id");
			namePattern = "%s_" + id;
			layers = new ArrayList<>();
			tags = new ArrayList<>();
			tintsSecondary = true;
		}

		public Builder namePattern(String namePattern) {
			this.namePattern = namePattern;
			return this;
		}

		public Builder layers(String... layers) {
			this.layers.addAll(Arrays.asList(layers));
			return this;
		}

		public Builder overlay(String overlay) {
			this.overlay = overlay;
			return this;
		}

		public Builder texture(String texture) {
			this.texture = texture;
			return this;
		}

		public Builder slurryTexture(String texture) {
			slurryTexture = ResourceLocation.parse(texture);
			return this;
		}

		public Builder tags(String... tags) {
			this.tags.addAll(Arrays.asList(tags));
			return this;
		}

		public Builder secondaryTint(boolean secondaryTint) {
			tintsSecondary = secondaryTint;
			return this;
		}

		public Builder noSecondaryTint() {
			return secondaryTint(false);
		}

		public Builder itemFactory(Function<Material, Item> factory) {
			itemFactory = factory;
			return this;
		}

		public Builder blockFactory(BiFunction<Material, BlockBehaviour.Properties, Block> factory) {
			blockFactory = factory;
			return this;
		}

		public Builder fluidTypeFactory(Function<Material, FluidType> factory) {
			fluidTypeFactory = factory;
			return this;
		}

		public IMaterialType build() {
			return new BuiltinType(this);
		}
	}

	/**
	 * 工厂生成的材料类型实现
	 *
	 * <p>
	 * 相等性基于 {@link IMaterialType#id()}, 因此同 ID 的类型可以安全地作为 Map 的键.
	 * </p>
	 */
	private static class BuiltinType implements IMaterialType {
		private final IMaterialKind kind;
		private final String id;
		private final String namePattern;
		private final List<String> layers;
		private final List<String> tags;
		private final String overlay;
		private final String texture;
		private final ResourceLocation slurryTexture;
		private final boolean tintsSecondary;
		private final Function<Material, Item> itemFactory;
		private final BiFunction<Material, BlockBehaviour.Properties, Block> blockFactory;
		private final Function<Material, FluidType> fluidTypeFactory;

		private BuiltinType(Builder builder) {
			kind = builder.kind;
			id = builder.id;
			namePattern = builder.namePattern;
			layers = List.copyOf(builder.layers);
			tags = List.copyOf(builder.tags);
			overlay = builder.overlay;
			texture = builder.texture;
			slurryTexture = builder.slurryTexture;
			tintsSecondary = builder.tintsSecondary;
			itemFactory = builder.itemFactory;
			blockFactory = builder.blockFactory;
			fluidTypeFactory = builder.fluidTypeFactory;
		}

		@Override
		public String id() {
			return id;
		}

		@Override
		public IMaterialKind kind() {
			return kind;
		}

		@Override
		public String namePattern() {
			return namePattern;
		}

		@Override
		public List<String> layers() {
			return layers;
		}

		@Nullable
		@Override
		public String overlay() {
			return overlay;
		}

		@Nullable
		@Override
		public String texture() {
			return texture;
		}

		@Nullable
		@Override
		public ResourceLocation slurryTexture() {
			return slurryTexture;
		}

		@Override
		public List<String> tags() {
			return tags;
		}

		@Override
		public boolean tintsSecondary() {
			return tintsSecondary;
		}

		@Override
		public Item createItem(Material material) {
			return itemFactory == null ? IMaterialType.super.createItem(material) : itemFactory.apply(material);
		}

		@Override
		public Block createBlock(Material material, BlockBehaviour.Properties properties) {
			return blockFactory == null ? IMaterialType.super.createBlock(material, properties) : blockFactory.apply(material, properties);
		}

		@Override
		public FluidType createFluidType(Material material) {
			return fluidTypeFactory == null ? IMaterialType.super.createFluidType(material) : fluidTypeFactory.apply(material);
		}

		@Override
		public boolean equals(Object object) {
			return this == object || object instanceof IMaterialType type && id.equals(type.id());
		}

		@Override
		public int hashCode() {
			return id.hashCode();
		}

		@Override
		public String toString() {
			return "IMaterialType[" + id + "]";
		}
	}
}
