package dev.celestiacraft.libs.common.material;

import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.common.material.event.RegisterMaterialEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <h2>MaterialManager</h2>
 *
 * <p>
 * 材料系统的入口.
 * </p>
 *
 * <p>工作流程:</p>
 *
 * <ol>
 *     <li>模组构造阶段注册 {@link NewRegistryEvent} 与 {@link RegisterEvent} 监听器</li>
 *     <li>{@link NewRegistryEvent}(所有模组构造完成, 注册表事件之前)发布
 *     {@link RegisterMaterialEvent} 给所有模组, 并桥接 KubeJS 的 {@code NebulaEvents.registerMaterial}</li>
 *     <li>收集到的材料由 {@link MaterialRegistrar} 加入延迟注册队列, 模型与标签由
 *     {@link MaterialAssets} 生成</li>
 *     <li>{@link RegisterEvent} 中真正写入注册表</li>
 * </ol>
 *
 * <p>
 * 也因此, 使用该系统的模组 <b>不需要</b> 在主类中加载任何定义类,
 * 只需要一个监听 {@link RegisterMaterialEvent} 的 {@code @Mod.EventBusSubscriber}.
 * </p>
 */
public class MaterialManager {
	private static final List<Material> MATERIALS;
	private static final Map<ResourceLocation, Material> BY_ID;

	private static boolean initialized;

	static {
		MATERIALS = new ArrayList<>();
		BY_ID = new LinkedHashMap<>();
	}

	private MaterialManager() {
	}

	/**
	 * 初始化材料系统
	 *
	 * @param modBus Nebula Libs 的模组事件总线
	 */
	public static void bootstrap(IEventBus modBus) {
		modBus.addListener(MaterialManager::onNewRegistry);
		modBus.addListener(MaterialManager::onRegister);
	}

	private static void onNewRegistry(NewRegistryEvent event) {
		if (initialized) {
			return;
		}

		initialized = true;

		RegisterMaterialEvent materialEvent = new RegisterMaterialEvent();

		MinecraftForge.EVENT_BUS.post(materialEvent);
		MaterialKubeJSHook.post(materialEvent);

		for (Material material : materialEvent.materials()) {
			accept(material);
		}

		if (MATERIALS.isEmpty()) {
			NebulaLibs.LOGGER.info("没有需要注册的材料");
			return;
		}

		for (Material material : MATERIALS) {
			MaterialRegistrar.register(material);
		}

		MaterialAssets.bake();

		NebulaLibs.LOGGER.info("已处理 {} 个材料, 共 {} 项注册内容", MATERIALS.size(), MaterialRegistration.total());
	}

	private static void onRegister(RegisterEvent event) {
		MaterialRegistration.flush(event);
	}

	private static void accept(Material material) {
		ResourceLocation id = material.id();
		Material previous = BY_ID.get(id);

		if (previous != null) {
			NebulaLibs.LOGGER.error("材料 {} 被重复定义, 后一个定义已被忽略", id);
			return;
		}

		BY_ID.put(id, material);
		MATERIALS.add(material);
	}

	/**
	 * @return 所有已注册的材料
	 */
	public static List<Material> materials() {
		return Collections.unmodifiableList(MATERIALS);
	}

	/**
	 * 按 ID 查找材料
	 *
	 * @param id 材料 ID
	 * @return 材料, 不存在时返回 null
	 */
	@Nullable
	public static Material get(ResourceLocation id) {
		return BY_ID.get(id);
	}

	/**
	 * 获取指定命名空间下的所有材料
	 *
	 * @param namespace 命名空间(mod id)
	 * @return 材料列表
	 */
	public static List<Material> materials(String namespace) {
		List<Material> result = new ArrayList<>();

		for (Material material : MATERIALS) {
			if (material.namespace().equals(namespace)) {
				result.add(material);
			}
		}

		return result;
	}
}