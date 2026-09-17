package dev.celestiacraft.libs.common.material;

import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.common.material.event.RegisterMaterialEvent;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <h2>Material</h2>
 *
 * <p>
 * 一个材料(金属 / 非金属)的定义.
 * </p>
 *
 * <p>
 * 材料本身只是一份数据描述: 它声明了名称, 颜色, 挖掘等级, 以及需要生成哪些
 * {@link IMaterialType}. 真正的注册由 {@link MaterialRegistrar} 在
 * {@link RegisterMaterialEvent} 之后统一完成.
 * </p>
 *
 * <p>
 * 材料不会(也不应该)直接在静态初始化块中创建, 而是通过事件获得:
 * </p>
 *
 * <pre>{@code
 * // Java
 * @SubscribeEvent
 * public static void onRegisterMaterial(RegisterMaterialEvent event) {
 *     event.register("cmi:chromium", MiningLevels.IRON)
 *             .color(0xEBE3E4, 0xB2ACAD)
 *             .metal()
 *             .ingot()
 *             .plate()
 *             .block()
 *             .molten();
 * }
 *
 * // JS
 * NebulaEvents.registerMaterial(event => {
 *     event.namespace("cmi")
 *     event.create("chromium", MiningLevels.IRON)
 *             .color(0xEBE3E4, 0xB2ACAD)
 *             .metal()
 *             .ingot()
 *             .plate()
 *             .block()
 *             .molten()
 * })
 * }</pre>
 *
 * @see MaterialTypes
 */
public class Material {
	private final ResourceLocation id;
	private final Map<String, IMaterialType> types;
	private final Map<String, Boolean> overlays;
	private final Map<String, ResourceLocation> models;

	private int primaryColor;
	private int secondaryColor;
	private boolean metal;
	private IMiningLevel level;
	private float hardness;
	private float resistance;
	private String textureNamespace;

	@Nullable
	private SoundType sound;
	@Nullable
	private ResourceLocation moltenStill;
	@Nullable
	private ResourceLocation moltenFlowing;

	public Material(ResourceLocation id) {
		this.id = id;
		types = new LinkedHashMap<>();
		overlays = new LinkedHashMap<>();
		models = new LinkedHashMap<>();
		primaryColor = 0xFFFFFF;
		secondaryColor = 0xFFFFFF;
		metal = false;
		level = MiningLevels.STONE;
		hardness = 5.0F;
		resistance = 5.0F;
	}

	/**
	 * @return 材料的完整 ID, 例如 {@code cmi:chromium}
	 */
	public ResourceLocation id() {
		return id;
	}

	/**
	 * @return 材料的命名空间(mod id)
	 */
	public String namespace() {
		return id.getNamespace();
	}

	/**
	 * @return 材料名, 例如 {@code chromium}
	 */
	public String name() {
		return id.getPath();
	}

	/**
	 * 设置材料颜色(主色与副色相同)
	 *
	 * @param color 颜色, 支持 {@code 0xRRGGBB} 与 {@code 0xAARRGGBB}
	 * @return 当前材料
	 */
	@Info("设置材料颜色, 同时作为主色与副色")
	public Material color(int color) {
		return color(color, color);
	}

	/**
	 * 设置材料颜色
	 *
	 * @param primary   主色, 对应模型 layer0 (tint index 0)
	 * @param secondary 副色, 对应模型 layer1 (tint index 1)
	 * @return 当前材料
	 */
	@Info("设置材料颜色, primary 对应 layer0, secondary 对应 layer1")
	public Material color(int primary, int secondary) {
		primaryColor = withAlpha(primary);
		secondaryColor = withAlpha(secondary);
		return this;
	}

	/**
	 * 将该材料标记为金属
	 *
	 * <p>
	 * 金属材料生成的所有物品与方块都会被加入 {@code <namespace>:metals} 标签
	 * </p>
	 *
	 * @return 当前材料
	 */
	@Info("将该材料标记为金属, 生成的物品与方块会加入 <namespace>:metals 标签")
	public Material metal() {
		metal = true;
		return this;
	}

	/**
	 * {@link #metal()} 的别名, 便于从旧脚本迁移
	 *
	 * @return 当前材料
	 */
	@Info("metal() 的别名")
	public Material isMetal() {
		return metal();
	}

	/**
	 * 设置方块的挖掘等级
	 *
	 * @param level 挖掘等级, 例如 {@link MiningLevels#IRON}
	 * @return 当前材料
	 */
	@Info("设置方块挖掘等级, 例如 MiningLevels.IRON")
	public Material level(IMiningLevel level) {
		this.level = level == null ? MiningLevels.NONE : level;
		return this;
	}

	/**
	 * 设置方块硬度
	 *
	 * @param hardness 硬度
	 * @return 当前材料
	 */
	public Material hardness(float hardness) {
		this.hardness = hardness;
		return this;
	}

	/**
	 * 设置方块爆炸抗性
	 *
	 * @param resistance 爆炸抗性
	 * @return 当前材料
	 */
	public Material resistance(float resistance) {
		this.resistance = resistance;
		return this;
	}

	/**
	 * 同时设置方块硬度与爆炸抗性
	 *
	 * @param hardness   硬度
	 * @param resistance 爆炸抗性
	 * @return 当前材料
	 */
	@Info("同时设置方块硬度与爆炸抗性")
	public Material destroy(float hardness, float resistance) {
		return hardness(hardness).resistance(resistance);
	}

	/**
	 * 设置方块音效
	 *
	 * @param sound 音效类型, 例如 {@code SoundType.METAL}
	 * @return 当前材料
	 */
	@Info("设置方块音效, 例如 SoundType.METAL")
	public Material sound(SoundType sound) {
		this.sound = sound;
		return this;
	}

	/**
	 * 覆盖贴图命名空间
	 *
	 * <p>
	 * 默认使用 Nebula Libs 自带的灰度材料贴图({@code nebula_libs:item/material/color/...}),
	 * 需要整合包自己的贴图时(例如 {@code cmi:item/material/color/...})使用该方法覆盖.
	 * </p>
	 *
	 * @param namespace 贴图命名空间
	 * @return 当前材料
	 */
	@Info("覆盖贴图命名空间, 默认使用模组自带的 nebula_libs 贴图")
	public Material textures(String namespace) {
		textureNamespace = namespace;
		return this;
	}

	/**
	 * 覆盖熔融流体的贴图
	 *
	 * <p>
	 * 默认使用 Nebula Libs 自带的熔融贴图, 整合包想换成自己的贴图时使用该方法,
	 * 例如 {@code fluidTextures(ResourceLocation.fromNamespaceAndPath("cmi", "fluid/metal/still"), ResourceLocation.fromNamespaceAndPath("cmi", "fluid/metal/flow"))}.
	 * </p>
	 *
	 * @param still   静止贴图
	 * @param flowing 流动贴图
	 * @return 当前材料
	 */
	@Info("覆盖熔融流体的静止/流动贴图")
	public Material fluidTextures(ResourceLocation still, ResourceLocation flowing) {
		moltenStill = still;
		moltenFlowing = flowing;
		return this;
	}

	@Info("注册锭")
	public Material ingot() {
		return type(MaterialTypes.INGOT, true);
	}

	@Info("注册锭, overlay 为 false 时不生成亮层")
	public Material ingot(boolean overlay) {
		return type(MaterialTypes.INGOT, overlay);
	}

	@Info("注册板")
	public Material plate() {
		return type(MaterialTypes.PLATE, true);
	}

	@Info("注册板, overlay 为 false 时不生成亮层")
	public Material plate(boolean overlay) {
		return type(MaterialTypes.PLATE, overlay);
	}

	@Info("注册粒")
	public Material nugget() {
		return type(MaterialTypes.NUGGET, true);
	}

	@Info("注册粒, overlay 为 false 时不生成亮层")
	public Material nugget(boolean overlay) {
		return type(MaterialTypes.NUGGET, overlay);
	}

	@Info("注册粉")
	public Material dust() {
		return type(MaterialTypes.DUST, true);
	}

	@Info("注册粉, overlay 为 false 时不生成亮层")
	public Material dust(boolean overlay) {
		return type(MaterialTypes.DUST, overlay);
	}

	@Info("注册杆")
	public Material rod() {
		return type(MaterialTypes.ROD, true);
	}

	@Info("注册杆, overlay 为 false 时不生成亮层")
	public Material rod(boolean overlay) {
		return type(MaterialTypes.ROD, overlay);
	}

	@Info("注册齿轮")
	public Material gear() {
		return type(MaterialTypes.GEAR, true);
	}

	@Info("注册齿轮, overlay 为 false 时不生成亮层")
	public Material gear(boolean overlay) {
		return type(MaterialTypes.GEAR, overlay);
	}

	@Info("注册线材")
	public Material wire() {
		return type(MaterialTypes.WIRE, true);
	}

	@Info("注册线材, overlay 为 false 时不生成亮层")
	public Material wire(boolean overlay) {
		return type(MaterialTypes.WIRE, overlay);
	}

	@Info("注册棱镜")
	public Material prism() {
		return type(MaterialTypes.PRISM, true);
	}

	@Info("注册棱镜, overlay 为 false 时不生成亮层")
	public Material prism(boolean overlay) {
		return type(MaterialTypes.PRISM, overlay);
	}

	@Info("注册粗矿")
	public Material rawOre() {
		return type(MaterialTypes.RAW_ORE, true);
	}

	@Info("注册 Mekanism 脏粉")
	public Material dirty() {
		return type(MaterialTypes.DIRTY_DUST, false);
	}

	@Info("注册 Mekanism 团块")
	public Material clump() {
		return type(MaterialTypes.CLUMP, false);
	}

	@Info("注册 Mekanism 碎片")
	public Material shard() {
		return type(MaterialTypes.SHARD, false);
	}

	@Info("注册 Mekanism 晶体")
	public Material crystal() {
		return type(MaterialTypes.CRYSTAL, false);
	}

	@Info("注册 Mekanism 矿浆")
	public Material slurry() {
		return type(MaterialTypes.SLURRY, false);
	}

	@Info("注册 Mekanism 脏矿浆")
	public Material dirtySlurry() {
		return type(MaterialTypes.DIRTY_SLURRY, false);
	}

	@Info("注册金属块, 使用默认的彩色方块贴图")
	public Material block() {
		return type(MaterialTypes.BLOCK, true);
	}

	@Info("注册金属块, 并使用指定的方块模型, 例如ResourceLocation.parse(immersiveengineering:block/storage_steel")
	public Material block(ResourceLocation model) {
		return type(MaterialTypes.BLOCK, true)
				.model(MaterialTypes.BLOCK, model);
	}

	@Info("注册粗矿块, 使用默认的彩色方块贴图")
	public Material rawBlock() {
		return type(MaterialTypes.RAW_BLOCK, true);
	}

	@Info("注册粗矿块, 并使用指定的方块模型")
	public Material rawBlock(ResourceLocation model) {
		return type(MaterialTypes.RAW_BLOCK, true)
				.model(MaterialTypes.RAW_BLOCK, model);
	}

	@Info("注册熔融流体")
	public Material molten() {
		return type(MaterialTypes.MOLTEN, false);
	}

	/**
	 * 声明一种材料类型(默认生成亮层)
	 *
	 * @param type 类型, 内置类型见 {@link MaterialTypes}, 也可以是自己实现的 {@link IMaterialType}
	 * @return 当前材料
	 */
	@Info("声明一种材料类型, 默认生成亮层")
	public Material type(IMaterialType type) {
		return type(type, true);
	}

	/**
	 * 声明一种材料类型
	 *
	 * @param type    类型, 内置类型见 {@link MaterialTypes}, 也可以是自己实现的 {@link IMaterialType}
	 * @param overlay 是否生成亮层(该类型不支持亮层时忽略)
	 * @return 当前材料
	 */
	@Info("声明一种材料类型, 例如 MaterialTypes.INGOT")
	public Material type(IMaterialType type, boolean overlay) {
		types.put(type.id(), type);
		overlays.put(type.id(), overlay && type.hasOverlay());
		return this;
	}

	/**
	 * 为指定类型指定一个自定义模型
	 *
	 * @param type  类型
	 * @param model 模型 ID, 例如 {@code immersiveengineering:block/storage_steel}
	 * @return 当前材料
	 */
	public Material model(IMaterialType type, ResourceLocation model) {
		models.put(type.id(), model);
		return this;
	}

	/**
	 * @return 该材料声明的所有类型(按声明顺序)
	 */
	public Collection<IMaterialType> types() {
		return Collections.unmodifiableCollection(types.values());
	}

	/**
	 * @return 该材料声明的类型数量
	 */
	public int typeCount() {
		return types.size();
	}

	/**
	 * 判断材料是否声明了指定类型
	 */
	public boolean has(IMaterialType type) {
		return types.containsKey(type.id());
	}

	/**
	 * 判断指定类型是否生成亮层
	 */
	public boolean overlay(IMaterialType type) {
		return Boolean.TRUE.equals(overlays.get(type.id()));
	}

	/**
	 * @return 指定类型的自定义模型, 没有则为 null
	 */
	@Nullable
	public ResourceLocation model(IMaterialType type) {
		return models.get(type.id());
	}

	/**
	 * 计算指定类型的注册 ID
	 */
	public ResourceLocation id(IMaterialType type) {
		return type.id(this);
	}

	public int primaryColor() {
		return primaryColor;
	}

	public int secondaryColor() {
		return secondaryColor;
	}

	public boolean isMetallic() {
		return metal;
	}

	public IMiningLevel level() {
		return level;
	}

	public float hardness() {
		return hardness;
	}

	public float resistance() {
		return resistance;
	}

	@Nullable
	public SoundType sound() {
		return sound;
	}

	/**
	 * @return 熔融流体的静止贴图, 没有覆盖时返回 null
	 */
	@Nullable
	public ResourceLocation moltenStill() {
		return moltenStill;
	}

	/**
	 * @return 熔融流体的流动贴图, 没有覆盖时返回 null
	 */
	@Nullable
	public ResourceLocation moltenFlowing() {
		return moltenFlowing;
	}

	/**
	 * @return 贴图命名空间, 默认是 Nebula Libs 自带贴图
	 */
	public String textureNamespace() {
		return textureNamespace == null ? NebulaLibs.MODID : textureNamespace;
	}

	/**
	 * 获取该材料指定类型的物品
	 *
	 * @param type 类型
	 * @return 已注册的物品, 不存在时返回 {@code Items.AIR}
	 */
	public Item getItem(IMaterialType type) {
		return ForgeRegistries.ITEMS.getValue(id(type));
	}

	/**
	 * 获取该材料指定类型的方块
	 *
	 * @param type 类型
	 * @return 已注册的方块, 不存在时返回 {@code Blocks.AIR}
	 */
	public Block getBlock(IMaterialType type) {
		return ForgeRegistries.BLOCKS.getValue(id(type));
	}

	/**
	 * 获取该材料指定类型的流体
	 *
	 * @param type 类型
	 * @return 已注册的流体, 不存在时返回 {@code Fluids.EMPTY}
	 */
	public Fluid getFluid(IMaterialType type) {
		return ForgeRegistries.FLUIDS.getValue(id(type));
	}

	/**
	 * @return 熔融流体
	 */
	public Fluid getMolten() {
		return getFluid(MaterialTypes.MOLTEN);
	}

	@Override
	public String toString() {
		return "Material[" + id + "]";
	}

	private static int withAlpha(int color) {
		return (color & 0xFF000000) == 0 ? color | 0xFF000000 : color;
	}
}