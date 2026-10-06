package dev.celestiacraft.libs.api.tab.mixin;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Collection;
import java.util.Set;

/**
 * 为 {@link CreativeModeTab} 中存放展示物品的字段提供访问器，供分节功能改写标签页内容。
 */
@Mixin(CreativeModeTab.class)
public interface CreativeModeTabAccessor {
	@Accessor("displayItems")
	void setDisplayItems(Collection<ItemStack> items);

	@Accessor("displayItemsSearchTab")
	void setDisplayItemsSearchTab(Set<ItemStack> items);
}