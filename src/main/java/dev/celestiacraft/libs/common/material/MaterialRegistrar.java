package dev.celestiacraft.libs.common.material;

import lombok.experimental.UtilityClass;
import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.compat.ICheckModLoaded;
import dev.celestiacraft.libs.compat.mekanism.MekanismMaterialCompat;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.concurrent.atomic.AtomicReference;

/**
 * <h2>MaterialRegistrar</h2>
 *
 * <p>
 * 材料内容注册器.
 * </p>
 *
 * <p>
 * 每个 {@link IMaterialType} 自己决定怎么注册({@link IMaterialType#register(NebulaMaterial)}),
 * 默认实现会按 {@link IMaterialType#kind()} 选择这里的内置流程:
 * </p>
 *
 * <ul>
 *     <li>{@link MaterialKinds#ITEM} → {@link #registerItem(NebulaMaterial, IMaterialType)}</li>
 *     <li>{@link MaterialKinds#BLOCK} → {@link #registerBlock(NebulaMaterial, IMaterialType)}</li>
 *     <li>{@link MaterialKinds#FLUID} → {@link #registerFluid(NebulaMaterial, IMaterialType)}</li>
 *     <li>{@link MaterialKinds#SLURRY} → {@link #registerSlurry(NebulaMaterial, IMaterialType)}</li>
 * </ul>
 *
 * <p>
 * 自定义分类的材料类型可以直接复用这些公开方法, 组合出自己的注册流程.
 * 所有注册都是"延迟"的: 内容先进入 {@link MaterialRegistration} 队列,
 * 并在 {@code RegisterEvent} 中真正写入注册表.
 * </p>
 */
@UtilityClass
public class MaterialRegistrar {
	/**
	 * 注册单个材料声明的全部内容
	 *
	 * @param material 材料
	 */
	public void register(NebulaMaterial material) {
		if (material.typeCount() == 0) {
			NebulaLibs.LOGGER.warn("材料 {} 没有声明任何类型, 已跳过", material.id());
			return;
		}

		for (IMaterialType type : material.types()) {
			try {
				if (type.register(material)) {
					MaterialAssets.tags(material, type);
				}
			} catch (Exception exception) {
				NebulaLibs.LOGGER.error("注册材料 {} 的 {} 时发生错误", material.id(), type.id(), exception);
			}
		}
	}

	/**
	 * 按 {@link IMaterialType#kind()} 走内置注册流程
	 *
	 * @param material 材料
	 * @param type     类型
	 * @return 是否注册成功
	 */
	public boolean registerType(NebulaMaterial material, IMaterialType type) {
		IMaterialKind kind = type.kind();

		if (MaterialKinds.ITEM.equals(kind)) {
			registerItem(material, type);
			return true;
		}

		if (MaterialKinds.BLOCK.equals(kind)) {
			registerBlock(material, type);
			return true;
		}

		if (MaterialKinds.FLUID.equals(kind)) {
			registerFluid(material, type);
			return true;
		}

		if (MaterialKinds.SLURRY.equals(kind)) {
			return registerSlurry(material, type);
		}

		throw new IllegalStateException("材料类型 %s 使用了自定义分类 %s, 需要覆盖 IMaterialType#register(NebulaMaterial)".formatted(type.id(), kind.id()));
	}

	/**
	 * 注册物品
	 *
	 * @param material 材料
	 * @param type     类型
	 */
	public void registerItem(NebulaMaterial material, IMaterialType type) {
		ResourceLocation id = material.id(type);

		MaterialRegistration.add(Registries.ITEM, id, () -> type.createItem(material));
		MaterialAssets.itemModel(material, type);
		MaterialAssets.itemName(id);
	}

	/**
	 * 注册方块与对应的方块物品
	 *
	 * @param material 材料
	 * @param type     类型
	 */
	public void registerBlock(NebulaMaterial material, IMaterialType type) {
		ResourceLocation id = material.id(type);
		Block block = type.createBlock(material, BlockBehaviour.Properties.of()
				.strength(material.hardness(), material.resistance())
				.sound(blockSound(material)));

		MaterialRegistration.add(Registries.BLOCK, id, () -> block);
		MaterialRegistration.add(Registries.ITEM, id, () -> {
			return new BlockItem(block, new Item.Properties());
		});

		MaterialAssets.blockAssets(material, type);
		MaterialAssets.blockName(id);
		MaterialAssets.blockLootTable(id);

		// 方块专属标签: 挖掘工具与挖掘等级
		MaterialAssets.blockTag(ResourceLocation.parse("minecraft:mineable/pickaxe"), id);

		ResourceLocation level = material.level().id();

		if (level != null) {
			MaterialAssets.blockTag(level, id);
		}
	}

	/**
	 * 注册熔融流体(流体类型 + 源/流动流体 + 桶)
	 *
	 * @param material 材料
	 * @param type     类型
	 */
	public void registerFluid(NebulaMaterial material, IMaterialType type) {
		ResourceLocation id = material.id(type);
		ResourceLocation flowingId = flowingFluidId(id);
		ResourceLocation bucketId = bucketId(id);

		FluidType fluidType = type.createFluidType(material);

		AtomicReference<ForgeFlowingFluid.Source> still = new AtomicReference<>();
		AtomicReference<ForgeFlowingFluid.Flowing> flowing = new AtomicReference<>();
		AtomicReference<Item> bucket = new AtomicReference<>();

		ForgeFlowingFluid.Properties properties = new ForgeFlowingFluid.Properties(() -> fluidType, still::get, flowing::get)
				.bucket(bucket::get);

		MaterialRegistration.add(ForgeRegistries.Keys.FLUID_TYPES, id, () -> fluidType);
		MaterialRegistration.add(Registries.FLUID, id, () -> {
			ForgeFlowingFluid.Source fluid = new ForgeFlowingFluid.Source(properties);
			still.set(fluid);
			return fluid;
		});
		MaterialRegistration.add(Registries.FLUID, flowingId, () -> {
			ForgeFlowingFluid.Flowing fluid = new ForgeFlowingFluid.Flowing(properties);
			flowing.set(fluid);
			return fluid;
		});
		MaterialRegistration.add(Registries.ITEM, bucketId, () -> {
			Item item = new BucketItem(still::get, new Item.Properties()
					.craftRemainder(Items.BUCKET)
					.stacksTo(1));
			bucket.set(item);
			return item;
		});

		MaterialAssets.bucketModel(material, type);
		MaterialAssets.itemName(bucketId);
	}

	/**
	 * 流体"流动形态"的 ID 规则: 在原 ID 后追加 {@code _flowing}
	 *
	 * @param fluidId 源流体 ID
	 * @return 流动流体 ID
	 */
	public ResourceLocation flowingFluidId(ResourceLocation fluidId) {
		return ResourceLocation.fromNamespaceAndPath(fluidId.getNamespace(), fluidId.getPath() + "_flowing");
	}

	/**
	 * 流体桶的 ID 规则: 在原 ID 后追加 {@code _bucket}
	 *
	 * @param fluidId 源流体 ID
	 * @return 桶的物品 ID
	 */
	public ResourceLocation bucketId(ResourceLocation fluidId) {
		return ResourceLocation.fromNamespaceAndPath(fluidId.getNamespace(), fluidId.getPath() + "_bucket");
	}

	/**
	 * 注册 Mekanism 矿浆
	 *
	 * @param material 材料
	 * @param type     类型
	 * @return 是否注册成功(没有加载 Mekanism 时返回 false, 此时不会生成标签)
	 */
	public boolean registerSlurry(NebulaMaterial material, IMaterialType type) {
		if (!ICheckModLoaded.hasMekanism()) {
			NebulaLibs.LOGGER.warn("材料 {} 声明了 {}, 但当前没有加载 Mekanism, 已跳过", material.id(), type.id());
			return false;
		}

		MekanismMaterialCompat.register(material, type);
		return true;
	}

	private SoundType blockSound(NebulaMaterial material) {
		SoundType sound = material.sound();
		return sound == null ? SoundType.METAL : sound;
	}
}