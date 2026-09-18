package dev.celestiacraft.libs.common.material;

import lombok.experimental.UtilityClass;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
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
@UtilityClass
public class MaterialTypes {
	public final IMaterialType INGOT = item("ingot")
			.namePattern("%s_ingot")
			.layers("ingot/ingot", "ingot/ingot_secondary")
			.overlay("ingot/ingot_overlay")
			.tags("forge:ingots", "forge:ingots/%s")
			.build();

	public final IMaterialType PLATE = item("plate")
			.namePattern("%s_plate")
			.layers("plate/plate", "plate/plate_secondary")
			.overlay("plate/plate_overlay")
			.tags("forge:plates", "forge:plates/%s")
			.build();

	public final IMaterialType NUGGET = item("nugget")
			.namePattern("%s_nugget")
			.layers("nugget/nugget", "nugget/nugget_secondary")
			.overlay("nugget/nugget_overlay")
			.tags("forge:nuggets", "forge:nuggets/%s")
			.build();

	public final IMaterialType DUST = item("dust")
			.namePattern("%s_dust")
			.layers("dust/dust", "dust/dust_secondary")
			.overlay("dust/dust_overlay")
			.tags("forge:dusts", "forge:dusts/%s")
			.build();

	public final IMaterialType ROD = item("rod")
			.namePattern("%s_rod")
			.layers("rod/rod", "rod/rod_secondary")
			.overlay("rod/rod_overlay")
			.tags("forge:rods", "forge:rods/%s")
			.build();

	public final IMaterialType GEAR = item("gear")
			.namePattern("%s_gear")
			.layers("gear/gear", "gear/gear_secondary")
			.overlay("gear/gear_overlay")
			.tags("forge:gears", "forge:gears/%s")
			.build();

	public final IMaterialType WIRE = item("wire")
			.namePattern("%s_wire")
			.layers("wire/wire", "wire/wire_secondary")
			.overlay("wire/wire_overlay")
			.tags("forge:wires", "forge:wires/%s")
			.build();

	public final IMaterialType PRISM = item("prism")
			.namePattern("%s_prism")
			.layers("prism/prism", "prism/prism_secondary")
			.overlay("prism/prism_overlay")
			.tags("forge:prisms", "forge:prisms/%s")
			.build();

	public final IMaterialType RAW_ORE = item("raw_ore")
			.namePattern("raw_%s")
			.layers("raw_ore")
			.tags("forge:raw_materials", "forge:raw_materials/%s")
			.build();

	public final IMaterialType DIRTY_DUST = item("dirty")
			.namePattern("dirty_%s_dust")
			.layers("dirty/0", "dirty/1")
			.noSecondaryTint()
			.tags("mekanism:dirty_dusts", "mekanism:dirty_dusts/%s")
			.build();

	public final IMaterialType CLUMP = item("clump")
			.namePattern("%s_clump")
			.layers("clump")
			.tags("mekanism:clumps", "mekanism:clumps/%s")
			.build();

	public final IMaterialType SHARD = item("shard")
			.namePattern("%s_shard")
			.layers("shard")
			.tags("mekanism:shards", "mekanism:shards/%s")
			.build();

	public final IMaterialType CRYSTAL = item("crystal")
			.namePattern("%s_crystal")
			.layers("crystal")
			.tags("mekanism:crystals", "mekanism:crystals/%s")
			.build();

	public final IMaterialType BLOCK = block("block")
			.namePattern("%s_block")
			.texture("block/material/color/storage_blocks")
			.tags("forge:storage_blocks", "forge:storage_blocks/%s")
			.build();

	public final IMaterialType RAW_BLOCK = block("raw_block")
			.namePattern("raw_%s_block")
			.texture("block/material/color/storage_blocks")
			.tags("forge:storage_blocks", "forge:storage_blocks/raw_%s")
			.build();

	public final IMaterialType MOLTEN = fluid("molten")
			.namePattern("molten_%s")
			.tags("forge:molten_materials", "forge:molten_%s", "tconstruct:molten_%s")
			.build();

	public final IMaterialType SLURRY = slurry("slurry")
			.namePattern("%s_slurry")
			.slurryTexture("mekanism:slurry/clean")
			.tags("mekanism:clean", "mekanism:clean/%s")
			.build();

	public final IMaterialType DIRTY_SLURRY = slurry("dirty_slurry")
			.namePattern("dirty_%s_slurry")
			.slurryTexture("mekanism:slurry/dirty")
			.tags("mekanism:dirty", "mekanism:dirty/%s")
			.build();

	private final List<IMaterialType> VALUES;
	private final Map<String, IMaterialType> BY_ID;

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


	/**
	 * @return 所有内置材料类型
	 */
	public List<IMaterialType> values() {
		return VALUES;
	}

	/**
	 * 按 ID 查找内置材料类型
	 *
	 * @param id 类型 ID, 例如 {@code ingot}
	 * @return 材料类型, 不存在时返回 null
	 */
	@Nullable
	public IMaterialType byId(String id) {
		return BY_ID.get(id);
	}

	/**
	 * 创建一个物品类型
	 *
	 * @param id 类型 ID
	 * @return 类型构造器
	 */
	public Builder item(String id) {
		return of(MaterialKinds.ITEM, id);
	}

	/**
	 * 创建一个方块类型
	 *
	 * @param id 类型 ID
	 * @return 类型构造器
	 */
	public Builder block(String id) {
		return of(MaterialKinds.BLOCK, id);
	}

	/**
	 * 创建一个流体类型
	 *
	 * @param id 类型 ID
	 * @return 类型构造器
	 */
	public Builder fluid(String id) {
		return of(MaterialKinds.FLUID, id);
	}

	/**
	 * 创建一个 Mekanism 矿浆类型
	 *
	 * @param id 类型 ID
	 * @return 类型构造器
	 */
	public Builder slurry(String id) {
		return of(MaterialKinds.SLURRY, id);
	}

	/**
	 * 创建一个自定义分类的材料类型
	 *
	 * @param kind 分类, 内置分类见 {@link MaterialKinds}, 也可以用 {@link MaterialKinds#of(String)} 自定义
	 * @param id   类型 ID
	 * @return 类型构造器
	 */
	public Builder of(IMaterialKind kind, String id) {
		return new Builder(kind, id);
	}

	/**
	 * 材料类型构造器
	 *
	 * <p>
	 * 除了 {@link #layers(String...)} / {@link #tags(String...)} 这种可变参数方法,
	 * 其余设置项都由 Lombok 生成(链式, 方法名与字段同名).
	 * </p>
	 */
	@Setter
	@Accessors(chain = true, fluent = true)
	public class Builder {
		private final IMaterialKind kind;
		private final String id;
		private final List<String> layers = new ArrayList<>();
		private final List<String> tags = new ArrayList<>();

		private String namePattern;
		private String overlay;
		private String texture;
		@Setter(AccessLevel.NONE)
		private ResourceLocation slurryTexture;
		private boolean tintsSecondary;

		private Function<NebulaMaterial, Item> itemFactory;
		private BiFunction<NebulaMaterial, BlockBehaviour.Properties, Block> blockFactory;
		private Function<NebulaMaterial, FluidType> fluidTypeFactory;

		private Builder(IMaterialKind kind, String id) {
			this.kind = Objects.requireNonNull(kind, "kind");
			this.id = Objects.requireNonNull(id, "id");
			namePattern = "%s_" + id;
			tintsSecondary = true;
		}

		public Builder layers(String... layers) {
			this.layers.addAll(Arrays.asList(layers));
			return this;
		}

		public Builder tags(String... tags) {
			this.tags.addAll(Arrays.asList(tags));
			return this;
		}

		/**
		 * 设置矿浆贴图(字符串形式, 方便脚本调用)
		 *
		 * @param texture 贴图 ID, 例如 {@code mekanism:slurry/dirty}
		 * @return 当前构造器
		 */
		public Builder slurryTexture(String texture) {
			slurryTexture = ResourceLocation.parse(texture);
			return this;
		}

		public Builder noSecondaryTint() {
			return tintsSecondary(false);
		}

		public IMaterialType build() {
			return new BuiltinType(this);
		}
	}

	/**
	 * 工厂生成的材料类型实现
	 *
	 * <p>
	 * 本工厂生成的类型之间按 {@link IMaterialType#id()} 判等, 因此同 ID 的类型可以安全地作为 Map 的键.
	 * </p>
	 */
	@Getter
	@Accessors(fluent = true)
	@EqualsAndHashCode(onlyExplicitlyIncluded = true)
	private class BuiltinType implements IMaterialType {
		@EqualsAndHashCode.Include
		private final String id;
		private final IMaterialKind kind;
		private final String namePattern;
		private final List<String> layers;
		private final List<String> tags;
		private final String overlay;
		private final String texture;
		private final ResourceLocation slurryTexture;
		private final boolean tintsSecondary;
		private final Function<NebulaMaterial, Item> itemFactory;
		private final BiFunction<NebulaMaterial, BlockBehaviour.Properties, Block> blockFactory;
		private final Function<NebulaMaterial, FluidType> fluidTypeFactory;

		private BuiltinType(Builder builder) {
			id = builder.id;
			kind = builder.kind;
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
		public Item createItem(NebulaMaterial material) {
			return itemFactory == null ? IMaterialType.super.createItem(material) : itemFactory.apply(material);
		}

		@Override
		public Block createBlock(NebulaMaterial material, BlockBehaviour.Properties properties) {
			return blockFactory == null ? IMaterialType.super.createBlock(material, properties) : blockFactory.apply(material, properties);
		}

		@Override
		public FluidType createFluidType(NebulaMaterial material) {
			return fluidTypeFactory == null ? IMaterialType.super.createFluidType(material) : fluidTypeFactory.apply(material);
		}

		@Override
		public String toString() {
			return "IMaterialType[" + id + "]";
		}
	}
}
