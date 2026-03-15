package dev.celestiacraft.libs.common.material;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * 材料定义类
 *
 * <p>
 * 该类用于描述一个材料在模组中的所有属性, 并声明该材料
 * 所包含的物品, 方块和流体类型
 * </p>
 *
 * <p>
 * {@link Material} 通常不会直接实例化,
 * 而是通过一个预先声明的 {@code Material} 工厂对象创建
 * </p>
 *
 * <p>例如在主类中声明：</p>
 *
 * <pre>{@code
 * public static final Material MATERIAL = new Material(MODID);
 * }</pre>
 *
 * <p>随后在其它类中定义材料：</p>
 *
 * <pre>{@code
 * public static final Material COPPER = MyMod.MATERIAL
 *         .create("copper", "Copper", BlockTags.NEEDS_IRON_TOOL)
 *         .color(0xB87333, 0xD4956A)
 *         .setDestroy(3, 6)
 *         .isMetal()
 *         .ingot()
 *         .nugget()
 *         .plate()
 *         .dust()
 *         .rod()
 *         .gear()
 *         .wire()
 *         .rawOre()
 *         .metalBlock()
 *         .rawBlock()
 *         .molten();
 * }</pre>
 *
 * <p>
 * 所有定义的材料会自动加入 {@link #MATERIALS},
 * 注册系统可以遍历该列表自动生成对应内容
 * </p>
 */
public class Material {

	/**
	 * 材料类型条目
	 *
	 * <p>
	 * 每一个 {@link MaterialEntry} 表示该材料声明的一种
	 * {@link MaterialType} 类型
	 * </p>
	 *
	 * @param type    材料类型
	 * @param overlay 是否启用亮层覆盖
	 */
	public record MaterialEntry(MaterialType type, boolean overlay) {
	}

	/**
	 * 所有已创建的材料列表
	 *
	 * <p>
	 * 注册系统通常会遍历该列表以自动注册物品, 方块和流体
	 * </p>
	 */
	public static final List<Material> MATERIALS = new ArrayList<>();

	/**
	 * 当前材料声明的所有类型
	 */
	public final List<MaterialEntry> types = new ArrayList<>();

	/**
	 * 材料所属命名空间(通常为 mod id)
	 */
	public final String namespace;

	/**
	 * 材料内部名称
	 *
	 * <p>
	 * 该名称用于生成注册 ID
	 * </p>
	 */
	public String name;

	/**
	 * 材料显示名称
	 */
	public String displayName;

	/**
	 * 方块挖掘等级标签
	 */
	public TagKey<Block> level;

	/**
	 * 是否启用亮层覆盖
	 */
	public boolean overlay = false;

	/**
	 * 方块硬度
	 */
	public int hardness = 5;

	/**
	 * 方块爆炸抗性
	 */
	public int resistance = 5;

	/**
	 * 基础颜色
	 */
	public int color0 = 0xFFFFFF;

	/**
	 * 覆盖层颜色
	 */
	public int color1 = 0xFFFFFF;

	/**
	 * 方块声音类型
	 */
	public SoundType sound;

	/**
	 * 是否为金属材料
	 */
	public boolean metal = false;

	/**
	 * 创建一个材料工厂实例
	 *
	 * @param namespace 命名空间(mod id)
	 */
	public Material(String namespace) {
		this.namespace = namespace;
		this.name = null;
		this.level = null;
		this.sound = SoundType.METAL;
	}

	/**
	 * 创建一个新的材料定义
	 *
	 * @param name  材料名称
	 * @param level 挖掘等级
	 * @return 当前材料实例
	 */
	public Material create(String name, TagKey<Block> level) {
		this.name = name;
		this.level = level;
		MATERIALS.add(this);
		return this;
	}

	/**
	 * 创建一个新的材料定义并指定显示名称
	 *
	 * @param name        材料名称
	 * @param displayName 显示名称
	 * @param level       挖掘等级
	 * @return 当前材料实例
	 */
	public Material create(String name, String displayName, TagKey<Block> level) {
		this.name = name;
		this.displayName = displayName;
		this.level = level;
		MATERIALS.add(this);
		return this;
	}

	/**
	 * 设置方块硬度
	 *
	 * @param hardness 硬度
	 * @return 当前材料实例
	 */
	public Material setHardness(int hardness) {
		this.hardness = hardness;
		return this;
	}

	/**
	 * 设置方块爆炸抗性
	 *
	 * @param resistance 爆炸抗性
	 * @return 当前材料实例
	 */
	public Material setResistance(int resistance) {
		this.resistance = resistance;
		return this;
	}

	/**
	 * 同时设置硬度和爆炸抗性
	 *
	 * @param hardness   硬度
	 * @param resistance 抗性
	 * @return 当前材料实例
	 */
	public Material setDestroy(int hardness, int resistance) {
		this.hardness = hardness;
		this.resistance = resistance;
		return this;
	}

	/**
	 * 设置显示名称
	 *
	 * <p>
	 * 由于 Registrate 的限制,
	 * 显示名称通常需要使用英文
	 * </p>
	 *
	 * @param displayName 显示名称
	 * @return 当前材料实例
	 */
	public Material displayName(String displayName) {
		this.displayName = displayName;
		return this;
	}

	/**
	 * 设置材料颜色
	 *
	 * @param color0 底层颜色
	 * @param color1 覆盖层颜色
	 * @return 当前材料实例
	 */
	public Material color(int color0, int color1) {
		this.color0 = color0;
		this.color1 = color1;
		return this;
	}

	/**
	 * 设置统一颜色
	 *
	 * @param color 颜色
	 * @return 当前材料实例
	 */
	public Material color(int color) {
		this.color0 = color;
		this.color1 = color;
		return this;
	}

	/**
	 * 设置方块声音类型
	 *
	 * @param sound 声音类型
	 * @return 当前材料实例
	 */
	public Material sound(SoundType sound) {
		this.sound = sound;
		return this;
	}

	/**
	 * 将该材料标记为金属
	 *
	 * @return 当前材料实例
	 */
	public Material isMetal() {
		this.metal = true;
		return this;
	}

	/**
	 * 添加一种材料类型
	 *
	 * @param type    类型
	 * @param overlay 是否启用亮层
	 * @return 当前材料实例
	 */
	private Material type(MaterialType type, boolean overlay) {
		types.add(new MaterialEntry(type, overlay));
		return this;
	}

	/**
	 * 添加一种材料类型(默认启用亮层)
	 *
	 * @param type 材料类型
	 * @return 当前材料实例
	 */
	private Material type(MaterialType type) {
		return type(type, true);
	}

	/**
	 * 定义锭
	 */
	public Material ingot() {
		return type(MaterialType.INGOT);
	}

	/**
	 * 定义锭
	 */
	public Material ingot(boolean overlay) {
		return type(MaterialType.INGOT, overlay);
	}

	/**
	 * 定义板
	 */
	public Material plate() {
		return type(MaterialType.PLATE);
	}

	/**
	 * 定义板
	 */
	public Material plate(boolean overlay) {
		return type(MaterialType.PLATE, overlay);
	}

	/**
	 * 定义粒
	 */
	public Material nugget() {
		return type(MaterialType.NUGGET);
	}

	public Material nugget(boolean overlay) {
		return type(MaterialType.NUGGET, overlay);
	}

	/**
	 * 定义粉
	 */
	public Material dust() {
		return type(MaterialType.DUST);
	}

	public Material dust(boolean overlay) {
		return type(MaterialType.DUST, overlay);
	}

	/**
	 * 定义棒
	 */
	public Material rod() {
		return type(MaterialType.ROD);
	}

	public Material rod(boolean overlay) {
		return type(MaterialType.ROD, overlay);
	}

	/**
	 * 定义齿轮
	 */
	public Material gear() {
		return type(MaterialType.GEAR);
	}

	public Material gear(boolean overlay) {
		return type(MaterialType.GEAR, overlay);
	}

	/**
	 * 定义棱镜
	 */
	public Material prism() {
		return type(MaterialType.PRISM);
	}

	public Material prism(boolean overlay) {
		return type(MaterialType.PRISM, overlay);
	}

	/**
	 * 定义线材
	 */
	public Material wire() {
		return type(MaterialType.WIRE);
	}

	public Material wire(boolean overlay) {
		return type(MaterialType.WIRE, overlay);
	}

	/**
	 * 定义粗矿
	 */
	public Material rawOre() {
		return type(MaterialType.RAW_ORE);
	}

	/**
	 * Mekanism 处理中间产物
	 */
	public Material dirty() {
		return type(MaterialType.DIRTY);
	}

	public Material clump() {
		return type(MaterialType.CLUMP);
	}

	public Material shard() {
		return type(MaterialType.SHARD);
	}

	public Material crystal() {
		return type(MaterialType.CRYSTAL);
	}

	public Material slurry() {
		return type(MaterialType.SLURRY);
	}

	public Material dirtySlurry() {
		return type(MaterialType.DIRTY_SLURRY);
	}

	/**
	 * 金属块
	 */
	public Material metalBlock() {
		return type(MaterialType.BLOCK);
	}

	/**
	 * 粗矿块
	 */
	public Material rawBlock() {
		return type(MaterialType.RAW_BLOCK);
	}

	/**
	 * 熔融流体
	 */
	public Material molten() {
		return type(MaterialType.MOLTEN);
	}

	/**
	 * 生成资源 ID
	 */
	private ResourceLocation getId(String path) {
		return ResourceLocation.fromNamespaceAndPath(namespace, path);
	}

	/**
	 * 获取指定类型的物品
	 *
	 * @param type 材料类型
	 * @return 注册的物品
	 */
	public Item getItem(MaterialType type) {
		return ForgeRegistries.ITEMS.getValue(getId(String.format("%s_%s", name, type.getId())));
	}

	/**
	 * 获取指定类型方块
	 */
	private Block getBlock(MaterialType type) {
		return ForgeRegistries.BLOCKS.getValue(getId(String.format("%s_%s", name, type.getId())));
	}

	/**
	 * 获取金属块
	 */
	public Block getMetalBlock() {
		return getBlock(MaterialType.BLOCK);
	}

	/**
	 * 获取粗矿块
	 */
	public Block getRawBlock() {
		return getBlock(MaterialType.RAW_BLOCK);
	}

	/**
	 * 获取熔融流体
	 */
	public Fluid getMolten() {
		return ForgeRegistries.FLUIDS.getValue(getId(String.format("molten_%s", name)));
	}

	/**
	 * 获取材料流体
	 *
	 * <p>
	 * 默认返回 {@link #getMolten()}
	 * </p>
	 */
	public Fluid getFluid() {
		return getMolten();
	}
}