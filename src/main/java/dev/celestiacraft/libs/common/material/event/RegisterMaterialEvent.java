package dev.celestiacraft.libs.common.material.event;

import dev.celestiacraft.libs.common.material.NebulaMaterial;
import dev.celestiacraft.libs.common.material.MaterialManager;
import dev.celestiacraft.libs.common.material.IMiningLevel;
import dev.celestiacraft.libs.common.material.MiningLevels;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <h2>RegisterMaterialEvent</h2>
 *
 * <p>
 * 材料注册事件.
 * </p>
 *
 * <p>
 * 该事件在 <b>所有模组构造完成之后, 注册表事件({@code RegisterEvent})之前</b> 由
 * Nebula Libs 在 Forge 事件总线上发布, 因此:
 * </p>
 *
 * <ul>
 *     <li>不需要在主类中加载任何定义类, 只需要一个 {@code @Mod.EventBusSubscriber} 监听器</li>
 *     <li>物品 / 方块 / 流体 / 矿浆 / 模型 / 标签 全部由库自动注册与生成</li>
 *     <li>KubeJS 的 {@code NebulaEvents.registerMaterial} 也会在同一时间点触发, Java 与 JS 定义的材料会被合并处理</li>
 * </ul>
 *
 * <pre>{@code
 * @Mod.EventBusSubscriber(modid = "cmi")
 * public final class CmiMaterials {
 *     @SubscribeEvent
 *     public static void onRegisterMaterial(RegisterMaterialEvent event) {
 *         event.register("cmi:chromium", MiningLevels.IRON)
 *                 .color(0xEBE3E4, 0xB2ACAD)
 *                 .metal()
 *                 .ingot()
 *                 .plate()
 *                 .dust()
 *                 .nugget()
 *                 .block()
 *                 .molten();
 *     }
 * }
 * }</pre>
 *
 * @see MaterialManager
 */
public class RegisterMaterialEvent extends Event {
	private final List<NebulaMaterial> materials;
	private String namespace;

	public RegisterMaterialEvent() {
		this(null);
	}

	public RegisterMaterialEvent(@Nullable String namespace) {
		materials = new ArrayList<>();
		this.namespace = namespace;
	}

	/**
	 * 设置默认命名空间.
	 *
	 * <p>
	 * 设置之后 {@link #register(String)} 可以直接传入不带命名空间的材料名.
	 * </p>
	 *
	 * @param namespace 命名空间(mod id)
	 * @return 当前事件
	 */
	@Info("设置默认命名空间, 之后 create 可以只传材料名")
	public RegisterMaterialEvent namespace(@Nullable String namespace) {
		this.namespace = namespace;
		return this;
	}

	/**
	 * @return 当前的默认命名空间, 可能为 null
	 */
	@Nullable
	public String namespace() {
		return namespace;
	}

	/**
	 * 创建一个材料
	 *
	 * @param name 材料 ID, 可以是 {@code modid:name} 或者(设置了默认命名空间时) {@code name}
	 * @return 材料定义
	 */
	@Info("创建一个材料, 名称可以是 modid:name")
	public NebulaMaterial register(String name) {
		return register(name, null);
	}

	/**
	 * 创建一个材料
	 *
	 * @param name  材料 ID, 可以是 {@code modid:name} 或者(设置了默认命名空间时) {@code name}
	 * @param level 挖掘等级, 例如 {@link MiningLevels#IRON}
	 * @return 材料定义
	 */
	@Info("创建一个材料, 第二个参数为挖掘等级, 例如 MiningLevels.IRON")
	public NebulaMaterial register(String name, @Nullable IMiningLevel level) {
		NebulaMaterial material = new NebulaMaterial(resolve(name));

		if (level != null) {
			material.level(level);
		}

		materials.add(material);
		return material;
	}

	/**
	 * @return 本次事件中声明的所有材料
	 */
	public List<NebulaMaterial> materials() {
		return Collections.unmodifiableList(materials);
	}

	private ResourceLocation resolve(String name) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("材料名不能为空");
		}

		String trimmed = name.trim();

		if (trimmed.indexOf(':') < 0) {
			if (namespace == null || namespace.isBlank()) {
				throw new IllegalArgumentException("材料 '%s' 没有指定命名空间, 请使用 modid:name 或先调用 namespace(modid)".formatted(trimmed));
			}

			trimmed = namespace + ":" + trimmed;
		}

		ResourceLocation id = ResourceLocation.tryParse(trimmed);

		if (id == null || id.getPath().isBlank()) {
			throw new IllegalArgumentException("非法的材料 ID: " + name);
		}

		return id;
	}
}