package dev.celestiacraft.libs.client.material;

import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.common.material.IMaterialType;
import dev.celestiacraft.libs.common.material.Material;
import dev.celestiacraft.libs.common.material.MaterialManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * <h2>MaterialClientColors</h2>
 *
 * <p>
 * 材料物品与方块的颜色(染色)注册.
 * </p>
 *
 * <p>
 * 材料使用的都是灰度贴图, 真正的颜色由 tint 决定:
 * </p>
 *
 * <ul>
 *     <li>layer0 / tint index 0: 主色</li>
 *     <li>layer1 / tint index 1: 副色(类型可以用 {@link IMaterialType#tintsSecondary()} 关掉)</li>
 *     <li>其余层(例如亮层): 不染色</li>
 * </ul>
 *
 * <p>
 * 这里不关心材料分类, 而是直接看注册表里有没有对应 ID 的物品 / 方块,
 * 因此自定义分类生成的物品同样会被染色.
 * </p>
 */
@Mod.EventBusSubscriber(modid = NebulaLibs.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class MaterialClientColors {
	@SubscribeEvent
	public static void onItemColors(RegisterColorHandlersEvent.Item event) {
		for (Material material : MaterialManager.materials()) {
			int primary = material.primaryColor();
			int secondary = material.secondaryColor();

			for (IMaterialType type : material.types()) {
				Item item = ForgeRegistries.ITEMS.getValue(material.id(type));

				if (item == Items.AIR) {
					continue;
				}

				int layer1 = type.tintsSecondary() ? secondary : 0xFFFFFFFF;

				event.register((stack, tint) -> {
					return switch (tint) {
						case 0 -> primary;
						case 1 -> layer1;
						default -> 0xFFFFFFFF;
					};
				}, item);
			}
		}
	}

	@SubscribeEvent
	public static void onBlockColors(RegisterColorHandlersEvent.Block event) {
		for (Material material : MaterialManager.materials()) {
			int color = material.primaryColor();

			for (IMaterialType type : material.types()) {
				Block block = ForgeRegistries.BLOCKS.getValue(material.id(type));

				if (block == Blocks.AIR) {
					continue;
				}

				event.register((state, level, pos, tint) -> {
					return color;
				}, block);
			}
		}
	}
}