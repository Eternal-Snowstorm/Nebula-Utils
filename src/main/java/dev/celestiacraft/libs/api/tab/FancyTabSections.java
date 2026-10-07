package dev.celestiacraft.libs.api.tab;

import dev.celestiacraft.libs.api.tab.section.ISection;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 创造模式物品栏分节(Section)的注册入口,
 * 负责把分节挂到指定的创造模式标签页上.
 */
public class FancyTabSections {
	public static final Map<ResourceLocation, List<ISection<?>>> REGISTERED_TABS = new ConcurrentHashMap<>();

	/**
	 * 向指定的创造模式标签页添加一个新的分节
	 *
	 * @since 4.0
	 */
	public static void addSection(ResourceLocation tab, ISection section) {
		REGISTERED_TABS.computeIfAbsent(tab, (location) -> {
			return new ArrayList<>();
		}).add(section);
	}

	/**
	 * @return 已注册的分节列表；若请求的创造模式标签页没有任何分节则返回空列表
	 * @since 4.0
	 */
	public static List<ISection<?>> getSections(CreativeModeTab tab) {
		ResourceLocation creativeTabKey = BuiltInRegistries.CREATIVE_MODE_TAB.getKey(tab);

		return FancyTabSections.REGISTERED_TABS.getOrDefault(creativeTabKey, new ArrayList<>());
	}

	/**
	 * 使用给定的 ResourceLocation 注册一个创造模式标签页.
	 * 标签页标题会被设置为 `itemGroup.[namespace].[path]`
	 *
	 * @param location    标签页的 ResourceLocation
	 * @param icon 显示在标签页图标上的 ItemStack
	 * @since 5.0
	 */
	public static Supplier<CreativeModeTab> registerCreativeModeTab(IEventBus bus, ResourceLocation location, Supplier<ItemStack> icon) {
		return FTSInternal.registerTab(bus, location, icon);
	}

	/**
	 * 使用给定的 ResourceLocation 注册一个创造模式标签页.
	 * 标签页标题会被设置为 `itemGroup.[namespace].[path]`
	 *
	 * @param location    标签页的 ResourceLocation
	 * @param icon 显示在标签页图标上的 Item
	 * @since 5.0
	 */
	public static Supplier<CreativeModeTab> registerCreativeModeTab(IEventBus bus, ResourceLocation location, Item icon) {
		return FTSInternal.registerTab(bus, location, icon::getDefaultInstance);
	}

	/**
	 * 使用给定的 ResourceLocation 注册一个创造模式标签页.
	 * 标签页标题会被设置为 `itemGroup.[namespace].[path]`
	 *
	 * @param location    标签页的 ResourceLocation
	 * @param icon 显示在标签页图标上的 Item
	 * @since 5.0
	 */
	public static Supplier<CreativeModeTab> registerCreativeModeTab(IEventBus bus, ResourceLocation location, RegistryObject<Item> icon) {
		return FTSInternal.registerTab(bus, location, () -> {
			return icon.get().getDefaultInstance();
		});
	}

	/**
	 * @return 已注册的分节, 若未找到任何匹配项则返回 null
	 * @since 4.0
	 */
	public static ISection<?> getSection(ResourceLocation id) {
		for (List<ISection<?>> entry : REGISTERED_TABS.values()) {
			if (entry != null) {
				for (ISection<?> section : entry) {
					if (section.id().equals(id)) {
						return section;
					}
				}
			}
		}
		return null;
	}
}