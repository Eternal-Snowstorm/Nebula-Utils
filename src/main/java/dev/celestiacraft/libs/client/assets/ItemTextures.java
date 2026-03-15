package dev.celestiacraft.libs.client.assets;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ItemTextures {
	public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelProvider> setTexture(ResourceLocation texture) {
		return (context, provider) -> {
			provider.generated(context::getEntry, texture);
		};
	}

	public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelProvider> setTexture(String texture) {
		return (context, provider) -> {
			provider.generated(context::getEntry, provider.modLoc(texture));
		};
	}
}