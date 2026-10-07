package dev.celestiacraft.libs.api.tab;

import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.api.tab.creativetab.BannerRenderer;
import dev.celestiacraft.libs.api.tab.mixin.CreativeModeTabAccessor;
import dev.celestiacraft.libs.api.tab.section.Section;
import dev.celestiacraft.libs.api.tab.section.StickySection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 本类属于内部 API, 其中的方法可能会改名或被移除, 强烈建议不要调用.
 * 应当只通过 {@link FancyTabSections} 与本模块交互.
 *
 * @since 4.0
 */
@Mod.EventBusSubscriber(modid = NebulaLibs.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FTSInternal {
	/**
	 * 取出各分节中存放的物品, 并应用到给定的创造模式标签页上
	 *
	 * @since 4.0
	 */
	public static void applyItems(CreativeModeTab tab) {
		List<ItemStack> stacksToDisplay = new ArrayList<>();
		List<Section<?>> sections = FancyTabSections.getSections(tab);

		// 若标签页不包含任何分节, 直接结束流程
		if (sections.isEmpty()) {
			return;
		}

		for (Section<?> section : sections) {
			// 为横幅补充一个空行
			for (int i = 0; i < 9; i++) {
				stacksToDisplay.add(ItemStack.EMPTY);
			}

			// 若分节已折叠, 则不添加物品
			if (isCollapsed(section)) {
				continue;
			}

			stacksToDisplay.addAll(section.items().getStacks());

			// 填充空物品栈, 填满整行后再进入下一个横幅
			int usedInLastRow = stacksToDisplay.size() % 9;
			if (usedInLastRow != 0) {
				for (int i = 0; i < 9 - usedInLastRow; i++) {
					stacksToDisplay.add(ItemStack.EMPTY);
				}
			}
		}

		// 设置展示物品
		((CreativeModeTabAccessor) tab).setDisplayItems(stacksToDisplay);

		// 设置可搜索的物品集合
		((CreativeModeTabAccessor) tab).setDisplayItemsSearchTab(stacksToDisplay.stream()
				.filter((stack) -> {
					return !stack.isEmpty();
				})
				.collect(Collectors.toCollection(LinkedHashSet::new))
		);
	}

	/**
	 * 在进入世界以及 /reload 时触发
	 * 由于 ResourceListener 拿不到标签(tag), 这里作为获取完整 RegistryAccess 的变通方案
	 *
	 * @since 4.0
	 */
	@SubscribeEvent
	public static void tagsUpdatedEvent(TagsUpdatedEvent event) {
		refreshAllItems(event.getRegistryAccess());
	}

	/**
	 * 刷新所有已注册分节中的 ConglomerateOfItems
	 *
	 * @since 4.0
	 */
	public static void refreshAllItems(RegistryAccess access) {
		// 遍历每个已注册的标签页
		FancyTabSections.REGISTERED_TABS.forEach((location, sections) -> {
			// 遍历该标签页下的每个分节
			for (Section<?> section : sections) {
				// 从物品集合中解析出物品栈
				section.items().resolveStacks(access);
			}
		});
	}

	/**
	 * @return 请求的分节所在的行号；若在任何已注册标签页中都找不到该分节则返回 -1
	 * @since 4.0
	 */
	public static int getRowForSection(Section<?> section) {
		// 遍历已注册标签页中的每个分节列表
		for (List<Section<?>> list : FancyTabSections.REGISTERED_TABS.values()) {
			if (list.contains(section)) {
				int currentRow = 0;
				for (Section<?> sectionBeingChecked : list) {
					// 若当前检查的分节就是请求的分节
					if (sectionBeingChecked == section) {
						int contentRows = isCollapsed(sectionBeingChecked) ? 0 : (sectionBeingChecked.items().getStacks().size() - 1) / 9 + 1;
						int sectionEnd = currentRow + contentRows;

						if (sectionBeingChecked instanceof StickySection sticky
								&& sticky.isSticky()
								&& BannerRenderer.CURRENT_ROW >= currentRow
								&& BannerRenderer.CURRENT_ROW <= sectionEnd
						) {
							return BannerRenderer.CURRENT_ROW;
						}

						return currentRow;
					}

					// 横幅行
					currentRow++;

					// 内容行
					if (!isCollapsed(sectionBeingChecked)) {
						currentRow += (sectionBeingChecked.items().getStacks().size() - 1) / 9 + 1;
					}
				}
			}
		}
		return -1;
	}

	/**
	 * 存放所有已折叠分节的集合
	 *
	 * @since 4.0
	 */
	public static final Set<Section<?>> COLLAPSED = new HashSet<>();

	/**
	 * @return 请求的分节是否已折叠
	 * @since 4.0
	 */
	public static boolean isCollapsed(Section<?> section) {
		return COLLAPSED.stream().anyMatch((section1) -> {
			return section1.equals(section);
		});
	}

	/**
	 * 切换请求分节的折叠状态
	 *
	 * @since 4.0
	 */
	public static void toggle(Section<?> section) {
		if (FTSInternal.isCollapsed(section)) {
			expand(section, true);
		} else {
			collapse(section, true);
		}
	}

	/**
	 * 折叠请求的分节
	 *
	 * @since 4.0
	 */
	public static void collapse(Section<?> section, boolean playSound) {
		FTSInternal.COLLAPSED.add(section);

		if (FMLEnvironment.dist.isClient() && playSound) {
			Client.playSound(!FTSInternal.isCollapsed(section));
		}
	}

	/**
	 * 展开请求的分节
	 *
	 * @since 4.0
	 */
	public static void expand(Section<?> section, boolean playSound) {
		FTSInternal.COLLAPSED.removeIf((section1) -> {
			return section1.equals(section);
		});

		if (FMLEnvironment.dist.isClient() && playSound) {
			Client.playSound(!FTSInternal.isCollapsed(section));
		}
	}

	/**
	 * @return 给定行是否为某个分节的首行(即横幅行)
	 * @since 4.0
	 */
	public static boolean isBannerRow(ResourceLocation tab, int row) {
		List<Section<?>> sections = FancyTabSections.REGISTERED_TABS.get(tab);
		if (sections == null) {
			return false;
		}

		for (Section<?> section : sections) {
			if (getRowForSection(section) == row) return true;
		}
		return false;
	}

	/**
	 * 仅客户端使用的类, 负责处理音效, 避免第三方直接调用不安全方法导致崩溃.
	 *
	 * @since 4.0
	 */
	public static class Client {
		public static void playSound(boolean b) {
			if (b) {
				Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(
						SoundEvents.BAMBOO_WOOD_BUTTON_CLICK_ON,
						1.0f,
						1.0f
				));
			} else {
				Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(
						SoundEvents.BAMBOO_WOOD_BUTTON_CLICK_OFF,
						1.0f,
						1.0f
				));
			}
		}
	}

	public static Supplier<CreativeModeTab> registerTab(IEventBus bus, ResourceLocation location, Supplier<ItemStack> items) {
		DeferredRegister<CreativeModeTab> deferredRegister =
				DeferredRegister.create(Registries.CREATIVE_MODE_TAB, location.getNamespace());

		Supplier<CreativeModeTab> tab = deferredRegister.register(location.getPath(), () -> {
			return CreativeModeTab.builder()
					.icon(items)
					.title(Component.translatable("itemGroup." + location.getNamespace() + "." + location.getPath()))
					.displayItems((params, output) -> {
						output.accept(Items.BARRIER);
					})
					.build();
		});

		deferredRegister.register(bus);
		return tab;
	}
}
