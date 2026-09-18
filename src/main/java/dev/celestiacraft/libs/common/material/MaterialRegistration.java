package dev.celestiacraft.libs.common.material;

import lombok.experimental.UtilityClass;
import dev.celestiacraft.libs.NebulaLibs;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegisterEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * <h2>MaterialRegistration</h2>
 *
 * <p>
 * 材料系统内部使用的延迟注册队列.
 * </p>
 *
 * <p>
 * 与 {@code DeferredRegister} 的区别在于: 该队列允许以 <b>任意命名空间</b> 注册内容,
 * 因此库可以代替使用它的模组(例如 CMI)注册 {@code cmi:xxx} 而不需要对方提供任何注册表对象.
 * </p>
 *
 * <p>
 * 队列会在 Nebula Libs 自己的 {@link RegisterEvent} 中被消费, 消费后立即清空.
 * </p>
 */
@UtilityClass
public class MaterialRegistration {
	private final Map<ResourceKey<? extends Registry<?>>, List<Entry<?>>> PENDING = new LinkedHashMap<>();
	private final Set<ResourceKey<? extends Registry<?>>> FLUSHED = new LinkedHashSet<>();


	/**
	 * 将一个注册项加入队列
	 *
	 * @param key      注册表
	 * @param id       注册 ID
	 * @param supplier 实例工厂(只会被调用一次)
	 */
	public <T> void add(ResourceKey<? extends Registry<T>> key, ResourceLocation id, Supplier<? extends T> supplier) {
		if (FLUSHED.contains(key)) {
			NebulaLibs.LOGGER.error("注册表 {} 的事件已经结束, {} 无法再被注册(材料必须在 RegisterMaterialEvent 中定义)", key.location(), id);
			return;
		}

		PENDING.computeIfAbsent(key, k -> new ArrayList<>()).add(new Entry<>(id, supplier));
	}

	/**
	 * @return 队列是否为空
	 */
	public boolean isEmpty() {
		return PENDING.isEmpty();
	}

	/**
	 * @return 指定注册表中排队的注册项数量
	 */
	public int size(ResourceKey<? extends Registry<?>> key) {
		List<Entry<?>> entries = PENDING.get(key);
		return entries == null ? 0 : entries.size();
	}

	/**
	 * @return 队列中所有注册项的总数
	 */
	public int total() {
		int total = 0;

		for (List<Entry<?>> entries : PENDING.values()) {
			total += entries.size();
		}

		return total;
	}

	/**
	 * 消费指定注册表对应的队列
	 *
	 * @param event 注册事件
	 */
	public void flush(RegisterEvent event) {
		FLUSHED.add(event.getRegistryKey());

		List<Entry<?>> entries = PENDING.remove(event.getRegistryKey());

		if (entries == null || entries.isEmpty()) {
			return;
		}

		for (Entry<?> entry : entries) {
			register(event, entry);
		}
	}

	private void register(RegisterEvent event, Entry<?> entry) {
		event.register((ResourceKey) event.getRegistryKey(), entry.id(), (Supplier) entry.supplier());
	}

	private record Entry<T>(ResourceLocation id, Supplier<? extends T> supplier) {
	}
}