package dev.celestiacraft.libs.compat.kubejs.event;

import dev.celestiacraft.libs.common.material.IMiningLevel;
import dev.celestiacraft.libs.common.material.NebulaMaterial;
import dev.celestiacraft.libs.common.material.event.RegisterMaterialEvent;
import dev.latvian.mods.kubejs.event.StartupEventJS;
import dev.latvian.mods.kubejs.typings.Info;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * <h2>RegisterMaterialEventJS</h2>
 *
 * <p>
 * {@code NebulaEvents.registerMaterial} 的 JS 事件对象, 用法与 Forge 的
 * {@link RegisterMaterialEvent} 完全一致: 命名空间是事件上的方法.
 * </p>
 *
 * <pre>{@code
 * // startup_scripts/xxx.js
 * NebulaEvents.registerMaterial((event) => {
 *     event.namespace("cmi")                 // 只写一次, 之后 create 可以只给材料名
 *
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
 * @see RegisterMaterialEvent
 */
@Accessors(fluent = true)
public class RegisterMaterialEventJS extends StartupEventJS {
	private final RegisterMaterialEvent event;

	/**
	 * 当前设置的默认命名空间, 没有则为 null
	 */
	@Nullable
	@Getter
	private String namespace;

	public RegisterMaterialEventJS(RegisterMaterialEvent event) {
		this.event = event;
	}

	/**
	 * 设置默认命名空间(mod id)
	 *
	 * <p>
	 * 设置之后 {@link #create(String)} 可以只给材料名; 不设置时必须在材料名里写完整的
	 * {@code modid:name}.
	 * </p>
	 *
	 * @param namespace 命名空间
	 * @return 当前事件, 便于链式调用
	 */
	@Info("设置默认命名空间(mod id), 之后 create 可以只写材料名")
	public RegisterMaterialEventJS namespace(String namespace) {
		this.namespace = namespace;
		event.namespace(namespace);
		return this;
	}

	/**
	 * 创建一个材料
	 *
	 * @param name 材料名, 设置了命名空间时可以省略 {@code modid:}
	 * @return 材料定义
	 */
	@Info("创建一个材料, 例如 event.create('chromium')")
	public NebulaMaterial create(String name) {
		applyNamespace();
		return event.register(name);
	}

	/**
	 * 创建一个材料
	 *
	 * @param name  材料名
	 * @param level 挖掘等级, 例如 {@code MiningLevels.IRON}
	 * @return 材料定义
	 */
	@Info("创建一个材料, 第二个参数是挖掘等级, 例如 event.create('chromium', MiningLevels.IRON)")
	public NebulaMaterial create(String name, IMiningLevel level) {
		applyNamespace();
		return event.register(name, level);
	}

	/**
	 * {@link #create(String)} 的别名, 与 Forge 事件的方法名保持一致
	 *
	 * @param name 材料名
	 * @return 材料定义
	 */
	@Info("create(name) 的别名")
	public NebulaMaterial register(String name) {
		return create(name);
	}

	/**
	 * {@link #create(String, IMiningLevel)} 的别名, 与 Forge 事件的方法名保持一致
	 *
	 * @param name  材料名
	 * @param level 挖掘等级
	 * @return 材料定义
	 */
	@Info("create(name, level) 的别名")
	public NebulaMaterial register(String name, IMiningLevel level) {
		return create(name, level);
	}

	/**
	 * 设置本次创建的材料的默认创造模式标签页
	 *
	 * <pre>{@code
	 * NebulaEvents.registerMaterial(event => {
	 *     event.namespace("cmi")
	 *     event.setCreativeTab("cmi:main")
	 *     event.create("chromium", MiningLevels.IRON).ingot()
	 * })
	 * }</pre>
	 *
	 * @param tab 标签页 ID, 例如 {@code cmi:main}
	 * @return 当前事件
	 */
	@Info("设置本次材料默认放进的创造模式标签页, 例如 cmi:main")
	public RegisterMaterialEventJS setCreativeTab(ResourceLocation tab) {
		event.setCreativeTab(tab);
		return this;
	}

	/**
	 * 设置本次创建的材料的默认创造模式标签页
	 *
	 * @param tab 标签页的 {@link ResourceKey}
	 * @return 当前事件
	 */
	@Info("设置本次材料默认放进的创造模式标签页(ResourceKey)")
	public RegisterMaterialEventJS setCreativeTab(ResourceKey<CreativeModeTab> tab) {
		event.setCreativeTab(tab);
		return this;
	}

	/**
	 * @return 已经创建的所有材料
	 */
	public List<NebulaMaterial> materials() {
		return event.materials();
	}

	private void applyNamespace() {
		if (namespace != null && !namespace.isBlank()) {
			event.namespace(namespace);
		}
	}
}