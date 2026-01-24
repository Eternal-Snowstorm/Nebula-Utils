package top.nebula.libs.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Objects;

public class NebulaJeiInfo {
	private static IRecipeRegistration registration;

	static MutableComponent setTranKey(String key, String modid) {
		String tranKey = String.format("jei.info.%s.%s", modid, key);
		return Component.translatable(tranKey);
	}

	public static void init(IRecipeRegistration registration) {
		NebulaJeiInfo.registration = registration;
	}

	public static void addJeiInfo(ItemStack item, String key, String modid) {
		if (registration != null && !item.isEmpty()) {
			registration.addIngredientInfo(item, VanillaTypes.ITEM_STACK, setTranKey(key, modid));
		}
	}

	public static void addJeiInfo(TagKey<Item> tag, String key, String modid) {
		if (registration == null) {
			return;
		}

		List<ItemStack> stacks = Objects.requireNonNull(ForgeRegistries.ITEMS.tags())
				.getTag(tag)
				.stream()
				.map(ItemStack::new)
				.toList();

		if (stacks.isEmpty()) {
			return;
		}

		registration.addIngredientInfo(stacks, VanillaTypes.ITEM_STACK, setTranKey(key, modid));
	}
}