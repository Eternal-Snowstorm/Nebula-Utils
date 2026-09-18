package dev.celestiacraft.libs.common.material.event;

import dev.celestiacraft.libs.common.material.*;
import dev.latvian.mods.kubejs.typings.Info;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
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
 * <h3>如何在别处引用材料</h3>
 *
 * <p>
 * 材料是运行期创建的对象, 所以"静态引用"要自己保存: 把 {@link #register(String, IMiningLevel)}
 * 的返回值赋值给自己的静态字段即可(推荐把字段和监听器放在同一个类里, 一个文件搞定),
 * 之后就能写 {@code CmiMaterials.CHROMIUM.getIngot()}:
 * </p>
 *
 * <pre>{@code
 * @Mod.EventBusSubscriber(modid = "cmi")
 * public final class CmiMaterials {
 *     public static NebulaMaterial CHROMIUM;
 *     public static NebulaMaterial TITANIUM;
 *
 *     @SubscribeEvent
 *     public static void onRegisterMaterial(RegisterMaterialEvent event) {
 *         CHROMIUM = event.register("cmi:chromium", MiningLevels.IRON).color(...).metal().ingot();
 *         TITANIUM = event.register("cmi:titanium", MiningLevels.DIAMOND).color(...).metal().ingot();
 *     }
 * }
 *
 * // 别处
 * Item ingot = CmiMaterials.CHROMIUM.getIngot();
 * }</pre>
 *
 * <p>也可以不持有对象, 按 ID 查:</p>
 *
 * <ul>
 *     <li>{@code MaterialManager.get(id)} - 找不到返回 null</li>
 *     <li>{@code MaterialManager.require(id)} - 找不到直接抛异常, 适合"写错就该炸"的场合</li>
 * </ul>
 *
 * <p>
 * 时序: 材料对象本身在 {@link RegisterMaterialEvent} 时就有了, 但 {@code getIngot()} 这类查询要等
 * {@code RegisterEvent} 把物品真正写进注册表(也就是 {@code FMLCommonSetupEvent} 之前)才有效;
 * 需要在更早的阶段(例如同一批材料之间、或数据包里)引用时, 用
 * {@link NebulaMaterial#id(IMaterialType)} 拿 {@code ResourceLocation}.
 * </p>
 *
 * @see MaterialManager
 */
@Getter
@Accessors(fluent = true)
@NoArgsConstructor
public class RegisterMaterialEvent extends Event {
	private final List<NebulaMaterial> materials = new ArrayList<>();

	/**
	 * 默认命名空间(mod id), 可能为 null
	 */
	@Nullable
	private String namespace;

	/**
	 * 默认创造模式标签页, 可能为 null
	 */
	@Nullable
	private ResourceLocation creativeTab;

	public RegisterMaterialEvent(@Nullable String namespace) {
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
	 * 设置本次事件创建的材料的默认创造模式标签页
	 *
	 * <p>
	 * 单个材料可以用 {@link NebulaMaterial#setCreativeTab(ResourceLocation)} 覆盖;
	 * 两边都没设置的材料不会进入任何标签页. 与调用顺序无关(收集材料时才应用).
	 * </p>
	 *
	 * @param tab 标签页 ID, 例如 {@code cmi:main} 或原版的 {@code minecraft:ingredients}
	 * @return 当前事件
	 */
	@Info("设置本次材料默认放进的创造模式标签页, 例如 cmi:main")
	public RegisterMaterialEvent setCreativeTab(ResourceLocation tab) {
		creativeTab = tab;
		return this;
	}

	/**
	 * 设置本次事件创建的材料的默认创造模式标签页
	 *
	 * @param tab 标签页的 {@link ResourceKey}
	 * @return 当前事件
	 */
	@Info("设置本次材料默认放进的创造模式标签页(ResourceKey)")
	public RegisterMaterialEvent setCreativeTab(ResourceKey<CreativeModeTab> tab) {
		return setCreativeTab(tab.location());
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