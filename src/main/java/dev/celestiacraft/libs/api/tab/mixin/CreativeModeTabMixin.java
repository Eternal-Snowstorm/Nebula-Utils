package dev.celestiacraft.libs.api.tab.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.celestiacraft.libs.api.tab.FTSInternal;
import dev.celestiacraft.libs.api.tab.FancyTabSections;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

/**
 * 包裹 {@link CreativeModeTab} 的内容构建流程：若该标签页注册过分节，则改用分节内容填充，否则执行原版逻辑。
 */
@Mixin(CreativeModeTab.class)
public class CreativeModeTabMixin {
	@WrapMethod(method = "buildContents")
	private void nebula$buildContents(CreativeModeTab.ItemDisplayParameters parameters, Operation<Void> original) {
		CreativeModeTab self = (CreativeModeTab) (Object) this;

		ResourceLocation location = BuiltInRegistries.CREATIVE_MODE_TAB.getKey(self);

		if (FancyTabSections.REGISTERED_TABS.containsKey(location)) {
			((CreativeModeTabAccessor) self).setDisplayItems(List.of(Items.DIAMOND.getDefaultInstance()));

			FTSInternal.applyItems(self);
		} else {
			original.call(parameters);
		}
	}
}