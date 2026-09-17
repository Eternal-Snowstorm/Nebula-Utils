package dev.celestiacraft.libs.common.material;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <h2>MaterialAssets</h2>
 *
 * <p>
 * 材料资源生成器.
 * </p>
 *
 * <p>
 * 由于材料是由脚本(或事件)在运行时定义的, 无法使用数据生成器(datagen),
 * 因此材料需要的 <b>模型 / 方块状态 / 标签</b> 会在这里被动态生成,
 * 之后由 {@link MaterialPack} 以"运行时资源包"的形式注入游戏.
 * </p>
 *
 * <ul>
 *     <li>{@link PackType#CLIENT_RESOURCES}: 物品模型, 方块模型, 方块状态, 默认名称(en_us)</li>
 *     <li>{@link PackType#SERVER_DATA}: 标签, 方块掉落表</li>
 * </ul>
 *
 * <p>
 * 标签写入哪个目录由 {@link IMaterialKind#tagDirectories()} 决定,
 * 所以自定义分类(例如气体)也能正确生成标签.
 * </p>
 */
public class MaterialAssets {
	/**
	 * 物品标签目录
	 */
	public static final String ITEM_TAG_DIRECTORY = "tags/items";
	/**
	 * 方块标签目录
	 */
	public static final String BLOCK_TAG_DIRECTORY = "tags/blocks";
	/**
	 * 流体标签目录
	 */
	public static final String FLUID_TAG_DIRECTORY = "tags/fluids";
	/**
	 * Mekanism 矿浆标签目录(非原版注册表使用 {@code tags/<注册表命名空间>/<注册表路径>})
	 */
	public static final String SLURRY_TAG_DIRECTORY = "tags/mekanism/slurry";

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	private static final Map<ResourceLocation, byte[]> ASSETS;
	private static final Map<ResourceLocation, byte[]> DATA;

	/**
	 * 目录 -&gt; 标签 -&gt; 内容
	 */
	private static final Map<String, Map<ResourceLocation, Set<ResourceLocation>>> TAGS;

	/**
	 * 命名空间 -&gt; 翻译键 -&gt; 默认名称(自动生成的 en_us)
	 */
	private static final Map<String, Map<String, String>> LANG;

	public static final String PACK_META;

	static {
		ASSETS = new LinkedHashMap<>();
		DATA = new LinkedHashMap<>();
		TAGS = new LinkedHashMap<>();
		LANG = new LinkedHashMap<>();

		JsonObject pack = new JsonObject();
		pack.addProperty("pack_format", 15);
		pack.addProperty("description", "Nebula Libs generated material resources");

		JsonObject root = new JsonObject();
		root.add("pack", pack);
		PACK_META = GSON.toJson(root);
	}

	private MaterialAssets() {
	}

	/**
	 * 获取指定类型的生成文件
	 *
	 * @param type 资源包类型
	 * @return 文件表(路径 -&gt; 内容)
	 */
	public static Map<ResourceLocation, byte[]> files(PackType type) {
		return type == PackType.CLIENT_RESOURCES ? ASSETS : DATA;
	}

	/**
	 * @return 指定资源包类型是否有任何生成内容
	 */
	public static boolean isEmpty(PackType type) {
		return files(type).isEmpty();
	}

	/**
	 * 生成物品模型
	 *
	 * @param material 材料
	 * @param type     类型
	 */
	public static void itemModel(Material material, IMaterialType type) {
		ResourceLocation itemId = material.id(type);

		JsonObject textures = new JsonObject();
		List<String> layers = type.layers();

		for (int i = 0; i < layers.size(); i++) {
			textures.addProperty("layer" + i, itemTexture(material, layers.get(i)).toString());
		}

		if (type.overlay() != null && material.overlay(type)) {
			textures.addProperty("layer" + layers.size(), itemTexture(material, type.overlay()).toString());
		}

		JsonObject model = new JsonObject();
		model.addProperty("parent", "minecraft:item/generated");
		model.add("textures", textures);

		assets(itemModelId(itemId), model);
	}

	/**
	 * 生成方块的默认掉落表(自己掉落自己)
	 *
	 * <p>
	 * 方块没有掉落表就不会掉落任何东西, 所以这里为每个材料方块生成一份
	 * 与原版存储方块一致的掉落表.
	 * </p>
	 *
	 * @param blockId 方块 ID
	 */
	public static void blockLootTable(ResourceLocation blockId) {
		JsonObject entry = new JsonObject();
		entry.addProperty("type", "minecraft:item");
		entry.addProperty("name", blockId.toString());

		JsonArray entries = new JsonArray();
		entries.add(entry);

		JsonObject condition = new JsonObject();
		condition.addProperty("condition", "minecraft:survives_explosion");

		JsonArray conditions = new JsonArray();
		conditions.add(condition);

		JsonObject pool = new JsonObject();
		pool.addProperty("rolls", 1.0);
		pool.addProperty("bonus_rolls", 0.0);
		pool.add("conditions", conditions);
		pool.add("entries", entries);

		JsonArray pools = new JsonArray();
		pools.add(pool);

		JsonObject json = new JsonObject();
		json.addProperty("type", "minecraft:block");
		json.add("pools", pools);

		data(ResourceLocation.fromNamespaceAndPath(blockId.getNamespace(), "loot_tables/blocks/" + blockId.getPath() + ".json"), json);
	}

	/**
	 * 写入物品的默认名称(仅 {@code en_us}, 整合包可以用自己的语言文件覆盖)
	 *
	 * @param itemId 物品 ID
	 */
	public static void itemName(ResourceLocation itemId) {
		name("item." + itemId.getNamespace() + "." + itemId.getPath(), itemId);
	}

	/**
	 * 写入方块的默认名称(仅 {@code en_us}, 整合包可以用自己的语言文件覆盖)
	 *
	 * <p>
	 * 方块物品的翻译键与方块相同, 都是 {@code block.<命名空间>.<ID>}.
	 * </p>
	 *
	 * @param blockId 方块 ID
	 */
	public static void blockName(ResourceLocation blockId) {
		name("block." + blockId.getNamespace() + "." + blockId.getPath(), blockId);
	}

	private static void name(String key, ResourceLocation id) {
		LANG.computeIfAbsent(id.getNamespace(), namespace -> new LinkedHashMap<>())
				.putIfAbsent(key, defaultName(id.getPath()));
	}

	/**
	 * 由 ID 生成默认名称, 例如 {@code chromium_ingot} -&gt; {@code Chromium Ingot}
	 *
	 * @param path ID 路径
	 * @return 默认名称
	 */
	public static String defaultName(String path) {
		StringBuilder builder = new StringBuilder();

		for (String word : path.split("_")) {
			if (word.isEmpty()) {
				continue;
			}

			if (!builder.isEmpty()) {
				builder.append(' ');
			}

			builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
		}

		return builder.toString();
	}

	private static void bakeLang() {
		LANG.forEach((namespace, entries) -> {
			JsonObject json = new JsonObject();
			entries.forEach(json::addProperty);
			assets(ResourceLocation.fromNamespaceAndPath(namespace, "lang/en_us.json"), json);
		});
	}

	/**
	 * 生成方块(方块状态 + 方块模型 + 方块物品模型)
	 *
	 * @param material 材料
	 * @param type     类型
	 */
	public static void blockAssets(Material material, IMaterialType type) {
		ResourceLocation blockId = material.id(type);
		ResourceLocation custom = material.model(type);
		ResourceLocation modelId = custom != null
				? custom
				: ResourceLocation.fromNamespaceAndPath(blockId.getNamespace(), "block/" + blockId.getPath());

		// 方块状态
		JsonObject variant = new JsonObject();
		variant.addProperty("model", modelId.toString());

		JsonObject variants = new JsonObject();
		variants.add("", variant);

		JsonObject state = new JsonObject();
		state.add("variants", variants);
		assets(blockStateId(blockId), state);

		// 方块模型(仅在使用默认彩色贴图时生成)
		if (custom == null) {
			assets(blockModelId(blockId), cubeModel(material, type));
		}

		// 方块物品模型
		JsonObject item = new JsonObject();
		item.addProperty("parent", modelId.toString());
		assets(itemModelId(blockId), item);
	}

	/**
	 * 生成流体桶的模型
	 *
	 * @param material 材料
	 * @param type     流体类型(决定流体的注册 ID)
	 */
	public static void bucketModel(Material material, IMaterialType type) {
		ResourceLocation fluidId = material.id(type);
		ResourceLocation bucketId = MaterialRegistrar.bucketId(fluidId);

		JsonObject model = new JsonObject();
		model.addProperty("parent", "forge:item/bucket_drip");
		model.addProperty("loader", "forge:fluid_container");
		model.addProperty("fluid", fluidId.toString());

		assets(itemModelId(bucketId), model);
	}

	/**
	 * 写入材料类型声明的标签
	 *
	 * <p>
	 * 标签会写入 {@link IMaterialKind#tagDirectories()} 给出的所有目录;
	 * 金属材料额外写入 {@code <材料命名空间>:metals}.
	 * </p>
	 *
	 * @param material 材料
	 * @param type     类型
	 */
	public static void tags(Material material, IMaterialType type) {
		List<String> directories = type.kind().tagDirectories();

		if (directories.isEmpty()) {
			return;
		}

		ResourceLocation id = material.id(type);

		for (String raw : type.tags()) {
			ResourceLocation tag = ResourceLocation.parse(raw.formatted(material.name()));

			for (String directory : directories) {
				tag(directory, tag, id);
			}
		}

		if (material.isMetallic()) {
			ResourceLocation metals = ResourceLocation.fromNamespaceAndPath(material.namespace(), "metals");

			for (String directory : directories) {
				tag(directory, metals, id);
			}
		}
	}

	/**
	 * 将标签数据序列化进生成资源包
	 */
	public static void bake() {
		TAGS.forEach(MaterialAssets::bakeTags);
		bakeLang();
	}

	/**
	 * 往指定标签目录写入一个内容
	 *
	 * @param directory 目录, 例如 {@link #ITEM_TAG_DIRECTORY}
	 * @param tag       标签
	 * @param value     内容
	 */
	public static void tag(String directory, ResourceLocation tag, ResourceLocation value) {
		TAGS.computeIfAbsent(directory, key -> new LinkedHashMap<>())
				.computeIfAbsent(tag, key -> new LinkedHashSet<>())
				.add(value);
	}

	public static void itemTag(ResourceLocation tag, ResourceLocation value) {
		tag(ITEM_TAG_DIRECTORY, tag, value);
	}

	public static void blockTag(ResourceLocation tag, ResourceLocation value) {
		tag(BLOCK_TAG_DIRECTORY, tag, value);
	}

	public static void fluidTag(ResourceLocation tag, ResourceLocation value) {
		tag(FLUID_TAG_DIRECTORY, tag, value);
	}

	public static void slurryTag(ResourceLocation tag, ResourceLocation value) {
		tag(SLURRY_TAG_DIRECTORY, tag, value);
	}

	private static void bakeTags(String directory, Map<ResourceLocation, Set<ResourceLocation>> tags) {
		for (Map.Entry<ResourceLocation, Set<ResourceLocation>> entry : tags.entrySet()) {
			ResourceLocation tag = entry.getKey();

			JsonArray values = new JsonArray();

			for (ResourceLocation value : entry.getValue()) {
				values.add(value.toString());
			}

			JsonObject json = new JsonObject();
			json.addProperty("replace", false);
			json.add("values", values);

			data(ResourceLocation.fromNamespaceAndPath(tag.getNamespace(), directory + "/" + tag.getPath() + ".json"), json);
		}
	}

	private static JsonObject cubeModel(Material material, IMaterialType type) {
		String texture = type.texture() == null ? "block/material/color/storage_blocks" : type.texture();

		JsonObject textures = new JsonObject();
		textures.addProperty("particle", "#all");
		textures.addProperty("all", ResourceLocation.fromNamespaceAndPath(material.textureNamespace(), texture).toString());

		JsonArray elements = new JsonArray();
		elements.add(cubeElement());

		JsonObject model = new JsonObject();
		model.addProperty("parent", "minecraft:block/block");
		model.add("textures", textures);
		model.add("elements", elements);

		return model;
	}

	private static JsonObject cubeElement() {
		JsonObject element = new JsonObject();

		JsonArray from = new JsonArray();
		from.add(0);
		from.add(0);
		from.add(0);
		element.add("from", from);

		JsonArray to = new JsonArray();
		to.add(16);
		to.add(16);
		to.add(16);
		element.add("to", to);

		JsonObject faces = new JsonObject();

		for (String face : new String[]{"down", "up", "north", "south", "west", "east"}) {
			JsonObject faceJson = new JsonObject();

			JsonArray uv = new JsonArray();
			uv.add(0);
			uv.add(0);
			uv.add(16);
			uv.add(16);
			faceJson.add("uv", uv);

			faceJson.addProperty("texture", "#all");
			faceJson.addProperty("cullface", face);
			faceJson.addProperty("tintindex", 0);
			faces.add(face, faceJson);
		}

		element.add("faces", faces);
		return element;
	}

	private static ResourceLocation itemTexture(Material material, String layer) {
		return ResourceLocation.fromNamespaceAndPath(material.textureNamespace(), "item/material/color/" + layer);
	}

	private static ResourceLocation itemModelId(ResourceLocation id) {
		return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "models/item/" + id.getPath() + ".json");
	}

	private static ResourceLocation blockStateId(ResourceLocation id) {
		return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "blockstates/" + id.getPath() + ".json");
	}

	private static ResourceLocation blockModelId(ResourceLocation id) {
		return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "models/block/" + id.getPath() + ".json");
	}

	private static void assets(ResourceLocation path, JsonObject json) {
		ASSETS.put(path, GSON.toJson(json).getBytes(StandardCharsets.UTF_8));
	}

	private static void data(ResourceLocation path, JsonObject json) {
		DATA.put(path, GSON.toJson(json).getBytes(StandardCharsets.UTF_8));
	}
}