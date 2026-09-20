package dev.celestiacraft.libs.common.material;

import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.common.material.event.RegisterMaterialEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * <h2>NebulaMaterial</h2>
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
@Accessors(fluent = true)
@RequiredArgsConstructor
public class NebulaMaterial {
	private final ResourceLocation id;
	private final Map<String, IMaterialType> types = new LinkedHashMap<>();
	private final Map<String, Boolean> overlays = new LinkedHashMap<>();
	private final Map<String, ResourceLocation> models = new LinkedHashMap<>();
	private final Map<String, List<ResourceLocation>> typeTextures = new LinkedHashMap<>();

	/**
	 * 主色(layer0 / tint 0)
	 */
	@Getter
	private int primaryColor = 0xFFFFFF;

	/**
	 * 副色(layer1 / tint 1)
	 */
	@Getter
	private int secondaryColor = 0xFFFFFF;

	private boolean metal;

	/**
	 * 方块挖掘等级
	 */
	@Getter
	private IMiningLevel level = MiningLevels.STONE;

	/**
	 * 方块硬度
	 */
	@Getter
	private float hardness = 5.0F;

	/**
	 * 方块爆炸抗性
	 */
	@Getter
	private float resistance = 5.0F;

	private String textureNamespace;

	/**
	 * 方块音效, 没有设置时为 null
	 */
	@Getter
	@Nullable
	private SoundType sound;

	/**
	 * 熔融流体的静止贴图, 没有覆盖时为 null
	 */
	@Getter
	@Nullable
	private ResourceLocation moltenStill;

	/**
	 * 熔融流体的流动贴图, 没有覆盖时为 null
	 */
	@Getter
	@Nullable
	private ResourceLocation moltenFlowing;

	/**
	 * 创造模式标签页, 没有设置时为 null
	 */
	@Getter
	@Nullable
	private ResourceLocation creativeTab;

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
	public NebulaMaterial color(int color) {
		return color(color, color);
	}

	/**
	 * 设置材料颜色
	 *
	 * @param primary   主色, 对应模型 layer0 (tint index 0)
	 * @param secondary 副色, 对应模型 layer1 (tint index 1)
	 * @return 当前材料
	 */
	public NebulaMaterial color(int primary, int secondary) {
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
	public NebulaMaterial metal() {
		metal = true;
		return this;
	}

	/**
	 * {@link #metal()} 的别名, 便于从旧脚本迁移
	 *
	 * @return 当前材料
	 */
	public NebulaMaterial isMetal() {
		return metal();
	}

	/**
	 * 设置方块的挖掘等级
	 *
	 * @param level 挖掘等级, 例如 {@link MiningLevels#IRON}
	 * @return 当前材料
	 */
	public NebulaMaterial level(IMiningLevel level) {
		this.level = level == null ? MiningLevels.NONE : level;
		return this;
	}

	/**
	 * 设置方块硬度
	 *
	 * @param hardness 硬度
	 * @return 当前材料
	 */
	public NebulaMaterial hardness(float hardness) {
		this.hardness = hardness;
		return this;
	}

	/**
	 * 设置方块爆炸抗性
	 *
	 * @param resistance 爆炸抗性
	 * @return 当前材料
	 */
	public NebulaMaterial resistance(float resistance) {
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
	public NebulaMaterial destroy(float hardness, float resistance) {
		return hardness(hardness).resistance(resistance);
	}

	/**
	 * 设置方块音效
	 *
	 * @param sound 音效类型, 例如 {@code SoundType.METAL}
	 * @return 当前材料
	 */
	public NebulaMaterial sound(SoundType sound) {
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
	public NebulaMaterial textures(String namespace) {
		textureNamespace = namespace;
		return this;
	}

	/**
	 * 设置该材料生成的物品放进哪个创造模式标签页
	 *
	 * <p>
	 * 不设置时使用 {@link RegisterMaterialEvent#setCreativeTab(ResourceLocation)} 上的默认值;
	 * 两边都没设置就哪个标签页都不进(仍然可以通过数据包 / {@code BuildCreativeModeTabContentsEvent} 自己加).
	 * </p>
	 *
	 * @param tab 标签页 ID, 例如 {@code cmi:main} 或原版的 {@code minecraft:ingredients}
	 * @return 当前材料
	 */
	public NebulaMaterial setCreativeTab(ResourceLocation tab) {
		creativeTab = tab;
		return this;
	}

	public NebulaMaterial fluidTextures(ResourceLocation still, ResourceLocation flowing) {
		moltenStill = still;
		moltenFlowing = flowing;
		return this;
	}

	public NebulaMaterial ingot() {
		return type(MaterialTypes.INGOT, true);
	}

	public NebulaMaterial ingot(boolean overlay) {
		return type(MaterialTypes.INGOT, overlay);
	}

	public NebulaMaterial ingotWithTexture(ResourceLocation texture) {
		return type(MaterialTypes.INGOT, false)
				.texture(MaterialTypes.INGOT, texture);
	}

	public NebulaMaterial ingotWithModel(ResourceLocation model) {
		return type(MaterialTypes.INGOT, false)
				.model(MaterialTypes.INGOT, model);
	}

	public NebulaMaterial plate() {
		return type(MaterialTypes.PLATE, true);
	}

	public NebulaMaterial plate(boolean overlay) {
		return type(MaterialTypes.PLATE, overlay);
	}

	public NebulaMaterial plateWithTexture(ResourceLocation texture) {
		return type(MaterialTypes.PLATE, false)
				.texture(MaterialTypes.PLATE, texture);
	}

	public NebulaMaterial plateWithModel(ResourceLocation model) {
		return type(MaterialTypes.PLATE, false)
				.model(MaterialTypes.PLATE, model);
	}

	public NebulaMaterial nugget() {
		return type(MaterialTypes.NUGGET, true);
	}

	public NebulaMaterial nugget(boolean overlay) {
		return type(MaterialTypes.NUGGET, overlay);
	}

	public NebulaMaterial nuggetWithTexture(ResourceLocation texture) {
		return type(MaterialTypes.NUGGET, false)
				.texture(MaterialTypes.NUGGET, texture);
	}

	public NebulaMaterial nuggetWithModel(ResourceLocation model) {
		return type(MaterialTypes.NUGGET, false)
				.model(MaterialTypes.NUGGET, model);
	}

	public NebulaMaterial dust() {
		return type(MaterialTypes.DUST, true);
	}

	public NebulaMaterial dust(boolean overlay) {
		return type(MaterialTypes.DUST, overlay);
	}

	public NebulaMaterial dustWithTexture(ResourceLocation texture) {
		return type(MaterialTypes.DUST, false)
				.texture(MaterialTypes.DUST, texture);
	}

	public NebulaMaterial dustWithModel(ResourceLocation model) {
		return type(MaterialTypes.DUST, false)
				.model(MaterialTypes.DUST, model);
	}

	public NebulaMaterial rod() {
		return type(MaterialTypes.ROD, true);
	}

	public NebulaMaterial rod(boolean overlay) {
		return type(MaterialTypes.ROD, overlay);
	}

	public NebulaMaterial rodWithTexture(ResourceLocation texture) {
		return type(MaterialTypes.ROD, false)
				.texture(MaterialTypes.ROD, texture);
	}

	public NebulaMaterial rodWithModel(ResourceLocation model) {
		return type(MaterialTypes.ROD, false)
				.model(MaterialTypes.ROD, model);
	}

	public NebulaMaterial gear() {
		return type(MaterialTypes.GEAR, true);
	}

	public NebulaMaterial gear(boolean overlay) {
		return type(MaterialTypes.GEAR, overlay);
	}

	public NebulaMaterial gearWithTexture(ResourceLocation texture) {
		return type(MaterialTypes.GEAR, false)
				.texture(MaterialTypes.GEAR, texture);
	}

	public NebulaMaterial gearWithModel(ResourceLocation model) {
		return type(MaterialTypes.GEAR, false)
				.model(MaterialTypes.GEAR, model);
	}

	public NebulaMaterial wire() {
		return type(MaterialTypes.WIRE, true);
	}

	public NebulaMaterial wire(boolean overlay) {
		return type(MaterialTypes.WIRE, overlay);
	}

	public NebulaMaterial wireWithTexture(ResourceLocation texture) {
		return type(MaterialTypes.WIRE, false)
				.texture(MaterialTypes.WIRE, texture);
	}

	public NebulaMaterial wireWithModel(ResourceLocation model) {
		return type(MaterialTypes.WIRE, false)
				.model(MaterialTypes.WIRE, model);
	}

	public NebulaMaterial prism() {
		return type(MaterialTypes.PRISM, true);
	}

	public NebulaMaterial prism(boolean overlay) {
		return type(MaterialTypes.PRISM, overlay);
	}

	public NebulaMaterial prismWithTexture(ResourceLocation texture) {
		return type(MaterialTypes.PRISM, false)
				.texture(MaterialTypes.PRISM, texture);
	}

	public NebulaMaterial prismWithModel(ResourceLocation model) {
		return type(MaterialTypes.PRISM, false)
				.model(MaterialTypes.PRISM, model);
	}

	public NebulaMaterial rawOre() {
		return type(MaterialTypes.RAW_ORE, true);
	}

	public NebulaMaterial rawOreWithTexture(ResourceLocation texture) {
		return type(MaterialTypes.RAW_ORE, false)
				.texture(MaterialTypes.RAW_ORE, texture);
	}

	public NebulaMaterial rawOreWithModel(ResourceLocation model) {
		return type(MaterialTypes.RAW_ORE, true)
				.model(MaterialTypes.RAW_ORE, model);
	}

	public NebulaMaterial dirty() {
		return type(MaterialTypes.DIRTY_DUST, false);
	}

	public NebulaMaterial dirtyWithTexture(ResourceLocation texture) {
		return type(MaterialTypes.DIRTY_DUST, false)
				.texture(MaterialTypes.DIRTY_DUST, texture);
	}

	public NebulaMaterial dirtyWithModel(ResourceLocation model) {
		return type(MaterialTypes.DIRTY_DUST, false)
				.model(MaterialTypes.DIRTY_DUST, model);
	}

	public NebulaMaterial clump() {
		return type(MaterialTypes.CLUMP, false);
	}

	public NebulaMaterial clumpWithTexture(ResourceLocation texture) {
		return type(MaterialTypes.CLUMP, false)
				.texture(MaterialTypes.CLUMP, texture);
	}

	public NebulaMaterial clumpWithModel(ResourceLocation model) {
		return type(MaterialTypes.CLUMP, false)
				.model(MaterialTypes.CLUMP, model);
	}

	public NebulaMaterial shard() {
		return type(MaterialTypes.SHARD, false);
	}

	public NebulaMaterial shardWithTexture(ResourceLocation texture) {
		return type(MaterialTypes.SHARD, false)
				.texture(MaterialTypes.SHARD, texture);
	}

	public NebulaMaterial shardWithModel(ResourceLocation model) {
		return type(MaterialTypes.SHARD, false)
				.model(MaterialTypes.SHARD, model);
	}

	public NebulaMaterial crystal() {
		return type(MaterialTypes.CRYSTAL, false);
	}

	public NebulaMaterial crystalWithTexture(ResourceLocation texture) {
		return type(MaterialTypes.CRYSTAL, false)
				.texture(MaterialTypes.CRYSTAL, texture);
	}

	public NebulaMaterial crystalWithModel(ResourceLocation model) {
		return type(MaterialTypes.CRYSTAL, false)
				.model(MaterialTypes.CRYSTAL, model);
	}

	public NebulaMaterial slurry() {
		return type(MaterialTypes.SLURRY, false);
	}

	public NebulaMaterial dirtySlurry() {
		return type(MaterialTypes.DIRTY_SLURRY, false);
	}

	public NebulaMaterial block() {
		return type(MaterialTypes.BLOCK, true);
	}

	public NebulaMaterial blockWithTexture(ResourceLocation texture) {
		return type(MaterialTypes.BLOCK, true)
				.texture(MaterialTypes.BLOCK, texture);
	}

	public NebulaMaterial blockWithModel(ResourceLocation model) {
		return type(MaterialTypes.BLOCK, true)
				.model(MaterialTypes.BLOCK, model);
	}

	public NebulaMaterial rawBlock() {
		return type(MaterialTypes.RAW_BLOCK, true);
	}

	public NebulaMaterial rawBlockWithTexture(ResourceLocation texture) {
		return type(MaterialTypes.RAW_BLOCK, true)
				.texture(MaterialTypes.RAW_BLOCK, texture);
	}

	public NebulaMaterial rawBlockWithModel(ResourceLocation model) {
		return type(MaterialTypes.RAW_BLOCK, true)
				.model(MaterialTypes.RAW_BLOCK, model);
	}

	public NebulaMaterial molten() {
		return type(MaterialTypes.MOLTEN, false);
	}

	public NebulaMaterial type(IMaterialType type) {
		return type(type, true);
	}

	public NebulaMaterial type(IMaterialType type, boolean overlay) {
		types.put(type.id(), type);
		overlays.put(type.id(), overlay && type.hasOverlay());
		return this;
	}

	public NebulaMaterial model(IMaterialType type, ResourceLocation model) {
		models.put(type.id(), model);
		return this;
	}

	public NebulaMaterial textures(IMaterialType type, ResourceLocation... textures) {
		typeTextures.put(type.id(), List.of(textures));
		return this;
	}

	public NebulaMaterial texture(IMaterialType type, ResourceLocation texture) {
		return textures(type, texture);
	}

	public List<ResourceLocation> typeTextures(IMaterialType type) {
		return typeTextures.getOrDefault(type.id(), List.of());
	}

	public Collection<IMaterialType> types() {
		return Collections.unmodifiableCollection(types.values());
	}

	public int typeCount() {
		return types.size();
	}

	public boolean has(IMaterialType type) {
		return types.containsKey(type.id());
	}

	public boolean overlay(IMaterialType type) {
		return Boolean.TRUE.equals(overlays.get(type.id()));
	}

	@Nullable
	public ResourceLocation model(IMaterialType type) {
		return models.get(type.id());
	}

	public ResourceLocation id(IMaterialType type) {
		return type.id(this);
	}

	public boolean isMetallic() {
		return metal;
	}

	public String textureNamespace() {
		return textureNamespace == null ? NebulaLibs.MODID : textureNamespace;
	}

	public Item getItem(IMaterialType type) {
		return ForgeRegistries.ITEMS.getValue(id(type));
	}

	public Block getBlock(IMaterialType type) {
		return ForgeRegistries.BLOCKS.getValue(id(type));
	}

	public Fluid getFluid(IMaterialType type) {
		return ForgeRegistries.FLUIDS.getValue(id(type));
	}


	public Fluid getFluid() {
		return getMolten();
	}


	public Fluid getMolten() {
		return getFluid(MaterialTypes.MOLTEN);
	}


	public Item getBucket() {
		return ForgeRegistries.ITEMS.getValue(MaterialRegistrar.bucketId(id(MaterialTypes.MOLTEN)));
	}


	public Item getIngot() {
		return getItem(MaterialTypes.INGOT);
	}


	public Item getPlate() {
		return getItem(MaterialTypes.PLATE);
	}


	public Item getNugget() {
		return getItem(MaterialTypes.NUGGET);
	}


	public Item getDust() {
		return getItem(MaterialTypes.DUST);
	}


	public Item getRod() {
		return getItem(MaterialTypes.ROD);
	}


	public Item getGear() {
		return getItem(MaterialTypes.GEAR);
	}


	public Item getWire() {
		return getItem(MaterialTypes.WIRE);
	}

	public Item getPrism() {
		return getItem(MaterialTypes.PRISM);
	}


	public Item getRawOre() {
		return getItem(MaterialTypes.RAW_ORE);
	}


	public Item getDirtyDust() {
		return getItem(MaterialTypes.DIRTY_DUST);
	}


	public Item getClump() {
		return getItem(MaterialTypes.CLUMP);
	}


	public Item getShard() {
		return getItem(MaterialTypes.SHARD);
	}


	public Item getCrystal() {
		return getItem(MaterialTypes.CRYSTAL);
	}


	public Block getBlock() {
		return getBlock(MaterialTypes.BLOCK);
	}


	public Item getBlockItem() {
		return getItem(MaterialTypes.BLOCK);
	}


	public Block getRawBlock() {
		return getBlock(MaterialTypes.RAW_BLOCK);
	}

	public Item getRawBlockItem() {
		return getItem(MaterialTypes.RAW_BLOCK);
	}

	@Override
	public String toString() {
		return "NebulaMaterial[" + id + "]";
	}

	private static int withAlpha(int color) {
		return (color & 0xFF000000) == 0 ? color | 0xFF000000 : color;
	}
}