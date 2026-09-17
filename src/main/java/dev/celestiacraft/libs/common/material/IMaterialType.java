package dev.celestiacraft.libs.common.material;

import dev.celestiacraft.libs.common.fluid.type.MoltenType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * <h2>IMaterialType</h2>
 *
 * <p>
 * 材料类型接口 —— 描述"一个材料可以生成什么".
 * </p>
 *
 * <p>
 * 所有方法都有默认实现, 所以扩展只需要实现 {@link #id()} 与 {@link #kind()}, 再按需覆盖
 * 贴图 / 标签 / 内容工厂 / 注册流程即可. 材料类型不限于本模组内置的那些:
 * </p>
 *
 * <ul>
 *     <li><b>同分类的新类型</b>(例如本模组没有的"弹簧"): 用 {@link MaterialTypes#item(String)} 等工厂声明即可,
 *     注册流程会按 {@link #kind()} 自动选择</li>
 *     <li><b>新分类</b>(例如气体): 用 {@link MaterialKinds#of(String)} 建一个分类, 再覆盖
 *     {@link #register(NebulaMaterial)} 实现自己的注册流程(也可以直接复用
 *     {@link MaterialRegistrar} 里现成的流程)</li>
 *     <li><b>完全自定义</b>: 直接实现本接口, 覆盖需要的方法</li>
 * </ul>
 *
 * <p>
 * 本模组内置的类型(锭 / 板 / 粉 / 块 / 熔融流体 / 矿浆 ...)见 {@link MaterialTypes}.
 * </p>
 *
 * @see MaterialTypes
 * @see MaterialKinds
 */
public interface IMaterialType {
	/**
	 * @return 类型 ID, 用于内部识别与 {@link MaterialTypes#byId(String)}
	 */
	String id();

	/**
	 * @return 该类型所属的分类(决定默认注册流程与标签目录)
	 */
	IMaterialKind kind();

	/**
	 * 注册 ID 规则, {@code %s} 会被替换为材料名
	 *
	 * @return 例如 {@code %s_ingot}
	 */
	default String namePattern() {
		return "%s_" + id();
	}

	/**
	 * 物品贴图层(相对于 {@code <贴图命名空间>:item/material/color/})
	 *
	 * <p>
	 * 第 i 层对应模型 layer{i} 与 tint index i.
	 * </p>
	 *
	 * @return 贴图层, 没有则为空列表
	 */
	default List<String> layers() {
		return List.of();
	}

	/**
	 * @return 亮层贴图(相对于 {@code item/material/color/}), 没有则为 null
	 */
	@Nullable
	default String overlay() {
		return null;
	}

	/**
	 * @return 是否支持亮层
	 */
	default boolean hasOverlay() {
		return overlay() != null;
	}

	/**
	 * 方块贴图(相对于贴图命名空间根目录)
	 *
	 * @return 例如 {@code block/material/color/storage_blocks}, 没有则为 null
	 */
	@Nullable
	default String texture() {
		return null;
	}

	/**
	 * 矿浆贴图(绝对 ID, {@link MaterialKinds#SLURRY} 使用)
	 *
	 * @return 例如 {@code mekanism:slurry/dirty}, 没有则为 null
	 */
	@Nullable
	default ResourceLocation slurryTexture() {
		return null;
	}

	/**
	 * 需要写入的标签, {@code %s} 会被替换为材料名
	 *
	 * @return 例如 {@code forge:ingots} 与 {@code forge:ingots/%s}
	 */
	default List<String> tags() {
		return List.of();
	}

	/**
	 * 第二层(layer1 / tint index 1)是否参与染色
	 *
	 * @return 是否染色
	 */
	default boolean tintsSecondary() {
		return true;
	}

	/**
	 * 计算该类型在指定材料下的注册 ID
	 *
	 * @param material 材料
	 * @return 注册 ID
	 */
	default ResourceLocation id(NebulaMaterial material) {
		return ResourceLocation.fromNamespaceAndPath(material.namespace(), namePattern().formatted(material.name()));
	}

	/**
	 * 创建该类型的物品实例
	 *
	 * @param material 材料
	 * @return 物品实例
	 */
	default Item createItem(NebulaMaterial material) {
		return new Item(new Item.Properties());
	}

	/**
	 * 创建该类型的方块实例
	 *
	 * @param material   材料
	 * @param properties 已经按材料设置好的方块属性(硬度 / 抗性 / 音效)
	 * @return 方块实例
	 */
	default Block createBlock(NebulaMaterial material, BlockBehaviour.Properties properties) {
		return new Block(properties);
	}

	/**
	 * 创建该类型的流体类型
	 *
	 * @param material 材料
	 * @return 流体类型
	 */
	default FluidType createFluidType(NebulaMaterial material) {
		return new MoltenType(
				FluidType.Properties.create(),
				material.primaryColor(),
				material.moltenStill(),
				material.moltenFlowing()
		);
	}

	/**
	 * 注册该类型的内容
	 *
	 * <p>
	 * 默认按 {@link #kind()} 走 {@link MaterialRegistrar} 里的内置流程;
	 * 自定义分类的类型需要覆盖该方法(可以调用 {@link MaterialRegistrar} 里的公开流程复用逻辑).
	 * </p>
	 *
	 * @param material 材料
	 * @return 是否真的注册了内容; 返回 false 时不会为该类型生成标签
	 */
	default boolean register(NebulaMaterial material) {
		return MaterialRegistrar.registerType(material, this);
	}
}