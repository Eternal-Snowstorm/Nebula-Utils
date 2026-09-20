package dev.celestiacraft.libs.common.material;

import lombok.experimental.UtilityClass;
import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.common.material.event.RegisterMaterialEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.registries.ForgeRegistries;
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
@UtilityClass
public class MaterialManager {
	private final List<NebulaMaterial> MATERIALS = new ArrayList<>();
	private final Map<ResourceLocation, NebulaMaterial> BY_ID = new LinkedHashMap<>();

	private boolean initialized;

	/**
	 * 初始化材料系统
	 *
	 * @param modBus Nebula Libs 的模组事件总线
	 */
	public void bootstrap(IEventBus modBus) {
		modBus.addListener(MaterialManager::onNewRegistry);
		modBus.addListener(MaterialManager::onRegister);
		modBus.addListener(MaterialManager::onBuildCreativeTabContents);
	}

	private void onNewRegistry(NewRegistryEvent event) {
		if (initialized) {
			return;
		}

		initialized = true;

		RegisterMaterialEvent materialEvent = new RegisterMaterialEvent();

		// 必须走 mod bus: Forge 事件总线(MinecraftForge.EVENT_BUS)是带 startShutdown() 创建的,
		// 要等整个模组加载结束才会 start(), 在那之前 post 会被静默丢弃(不触发监听器也不报错).
		// 见 RegisterMaterialEvent 的类注释.
		ModLoader.get().postEventWrapContainerInModOrder(materialEvent);

		MaterialKubeJSHook.post(materialEvent);

		for (NebulaMaterial material : materialEvent.materials()) {
			// 材料自己没指定标签页时, 用事件上的默认值
			if (material.creativeTab() == null && materialEvent.creativeTab() != null) {
				material.setCreativeTab(materialEvent.creativeTab());
			}

			accept(material);
		}

		if (MATERIALS.isEmpty()) {
			NebulaLibs.LOGGER.info("没有需要注册的材料 (如果你已经用 RegisterMaterialEvent 定义过材料, 却看到这条: " +
					"请确认监听器上的 @Mod.EventBusSubscriber 写的是 bus = Mod.EventBusSubscriber.Bus.MOD, " +
					"因为材料事件是 mod bus 事件)");
			return;
		}

		for (NebulaMaterial material : MATERIALS) {
			MaterialRegistrar.register(material);
		}

		MaterialAssets.bake();

		NebulaLibs.LOGGER.info("已处理 {} 个材料, 共 {} 项注册内容", MATERIALS.size(), MaterialRegistration.total());
	}

	private void onRegister(RegisterEvent event) {
		MaterialRegistration.flush(event);
	}

	/**
	 * 把材料生成的物品放进对应的创造模式标签页
	 *
	 * <p>
	 * 标签页由 {@link NebulaMaterial#setCreativeTab(ResourceLocation)} 或
	 * {@link RegisterMaterialEvent#setCreativeTab(ResourceLocation)} 指定;
	 * 没指定的材料不会进任何标签页. 该事件只在客户端触发.
	 * </p>
	 *
	 * @param event 标签页内容事件
	 */
	private void onBuildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
		ResourceLocation tab = event.getTabKey().location();

		for (NebulaMaterial material : MATERIALS) {
			if (!tab.equals(material.creativeTab())) {
				continue;
			}

			// 按声明顺序加入各个类型的物品
			for (IMaterialType type : material.types()) {
				Item item = ForgeRegistries.ITEMS.getValue(material.id(type));

				if (item != null && item != Items.AIR) {
					event.accept(item);
				}
			}

			// 熔融流体的桶
			Item bucket = material.getBucket();

			if (bucket != null && bucket != Items.AIR) {
				event.accept(bucket);
			}
		}
	}

	private void accept(NebulaMaterial material) {
		ResourceLocation id = material.id();
		NebulaMaterial previous = BY_ID.get(id);

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
	public List<NebulaMaterial> materials() {
		return Collections.unmodifiableList(MATERIALS);
	}

	/**
	 * 按 ID 查找材料
	 *
	 * @param id 材料 ID
	 * @return 材料, 不存在时返回 null
	 */
	@Nullable
	public NebulaMaterial get(ResourceLocation id) {
		return BY_ID.get(id);
	}

	/**
	 * 按 ID 查找材料, 找不到时抛异常
	 *
	 * <p>
	 * 适合"这个材料必须存在, 不存在就是写错了"的场合(例如配方代码里按 ID 取材料);
	 * 想自己处理缺失就用 {@link #get(ResourceLocation)}.
	 * </p>
	 *
	 * @param id 材料 ID
	 * @return 材料
	 * @throws IllegalStateException 材料不存在时抛出
	 */
	public NebulaMaterial require(ResourceLocation id) {
		NebulaMaterial material = BY_ID.get(id);

		if (material == null) {
			throw new IllegalStateException("材料 %s 不存在(材料必须在 RegisterMaterialEvent / NebulaEvents.registerMaterial 里定义)".formatted(id));
		}

		return material;
	}

	/**
	 * 获取指定命名空间下的所有材料
	 *
	 * @param namespace 命名空间(mod id)
	 * @return 材料列表
	 */
	public List<NebulaMaterial> materials(String namespace) {
		List<NebulaMaterial> result = new ArrayList<>();

		for (NebulaMaterial material : MATERIALS) {
			if (material.namespace().equals(namespace)) {
				result.add(material);
			}
		}

		return result;
	}
}