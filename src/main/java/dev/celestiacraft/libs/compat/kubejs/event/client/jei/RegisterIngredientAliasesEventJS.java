package dev.celestiacraft.libs.compat.kubejs.event.client.jei;

import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import dev.latvian.mods.rhino.util.RemapForJS;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

public class RegisterIngredientAliasesEventJS extends EventJS {
	private final IIngredientAliasRegistration registration;

	private RegisterIngredientAliasesEventJS(IIngredientAliasRegistration registration) {
		this.registration = registration;
	}

	public static RegisterIngredientAliasesEventJS of(IIngredientAliasRegistration registration) {
		return new RegisterIngredientAliasesEventJS(registration);
	}

	public void addItemAlias(Item item, String alias) {
		if (item == null || !isValidAlias(alias)) {
			return;
		}

		registration.addAlias(item, alias);
	}

	@RemapForJS("addItemAliases")
	public void addItemAlias(Item item, String... aliases) {
		if (item == null || aliases == null) {
			return;
		}

		for (String alias : aliases) {
			addItemAlias(item, alias);
		}
	}

	public void addItemStackAlias(ItemStack stack, String alias) {
		if (stack == null || stack.isEmpty() || !isValidAlias(alias)) {
			return;
		}

		registration.addAlias(stack, alias);
	}

	@RemapForJS("addItemStackAliases")
	public void addItemStackAlias(ItemStack stack, String... aliases) {
		if (stack == null || stack.isEmpty() || aliases == null) {
			return;
		}

		for (String alias : aliases) {
			addItemStackAlias(stack, alias);
		}
	}

	public void addItemTagAlias(TagKey<Item> tag, String alias) {
		registerItemTag(tag, alias);
	}

	@RemapForJS("addItemTagAliases")
	public void addItemTagAlias(TagKey<Item> tag, String... aliases) {
		registerItemTag(tag, aliases);
	}

	public void addItemTagIdAlias(ResourceLocation tag, String alias) {
		if (tag == null) {
			return;
		}

		addItemTagAlias(TagKey.create(Registries.ITEM, tag), alias);
	}

	@RemapForJS("addItemTagIdAliases")
	public void addItemTagIdAlias(ResourceLocation tag, String... aliases) {
		if (tag == null) {
			return;
		}

		addItemTagAlias(TagKey.create(Registries.ITEM, tag), aliases);
	}

	public void addFluidAlias(Fluid fluid, String alias) {
		if (fluid == null || !isValidAlias(alias)) {
			return;
		}

		registration.addAlias(fluid, alias);
	}

	@RemapForJS("addFluidAliases")
	public void addFluidAlias(Fluid fluid, String... aliases) {
		if (fluid == null || aliases == null) {
			return;
		}

		for (String alias : aliases) {
			addFluidAlias(fluid, alias);
		}
	}

	public void addFluidTagAlias(TagKey<Fluid> tag, String alias) {
		registerFluidTag(tag, alias);
	}

	@RemapForJS("addFluidTagAliases")
	public void addFluidTagAlias(TagKey<Fluid> tag, String... aliases) {
		registerFluidTag(tag, aliases);
	}

	public void addFluidTagIdAlias(ResourceLocation tag, String alias) {
		if (tag == null) {
			return;
		}

		addFluidTagAlias(TagKey.create(Registries.FLUID, tag), alias);
	}

	@RemapForJS("addFluidTagIdAliases")
	public void addFluidTagIdAlias(ResourceLocation tag, String... aliases) {
		if (tag == null) {
			return;
		}

		addFluidTagAlias(TagKey.create(Registries.FLUID, tag), aliases);
	}

	public void addIngredientAlias(Ingredient ingredient, String alias) {
		registerIngredient(ingredient, alias);
	}

	@RemapForJS("addIngredientAliases")
	public void addIngredientAlias(Ingredient ingredient, String... aliases) {
		registerIngredient(ingredient, aliases);
	}

	private void registerIngredient(Ingredient ingredient, String... aliases) {
		if (ingredient == null || ingredient == Ingredient.EMPTY || aliases == null || aliases.length == 0) {
			return;
		}

		int matched = 0;

		for (Item item : ForgeRegistries.ITEMS) {
			if (item != Items.AIR && ingredient.test(item.getDefaultInstance())) {
				for (String alias : aliases) {
					if (isValidAlias(alias)) {
						registration.addAlias(item, alias);
					}
				}

				matched++;
			}
		}

		if (matched == 0) {
			ConsoleJS.CLIENT.warn("Nebula JEI alias: ingredient %s did not match any registered item".formatted(ingredient));
		}
	}

	private static boolean isValidAlias(String alias) {
		return alias != null && !alias.isBlank();
	}

	private void registerItemTag(TagKey<Item> tag, String... aliases) {
		if (tag == null || aliases == null) {
			return;
		}

		int matched = 0;

		for (Item item : ForgeRegistries.ITEMS) {
			if (item != Items.AIR && item.getDefaultInstance().is(tag)) {
				for (String alias : aliases) {
					if (isValidAlias(alias)) {
						registration.addAlias(item, alias);
					}
				}

				matched++;
			}
		}

		if (matched == 0) {
			ConsoleJS.CLIENT.warn("Nebula JEI alias: item tag '%s' did not match any registered item (tag not loaded yet?)".formatted(tag.location()));
		}
	}

	private void registerFluidTag(TagKey<Fluid> tag, String... aliases) {
		if (tag == null || aliases == null) {
			return;
		}

		ClientLevel level = Minecraft.getInstance().level;

		if (level == null) {
			ConsoleJS.CLIENT.warn("Nebula JEI alias: no client level to resolve fluid tag '%s'".formatted(tag.location()));
			return;
		}

		Registry<Fluid> registry = level.registryAccess().registryOrThrow(Registries.FLUID);
		Optional<HolderSet.Named<Fluid>> holders = registry.getTag(tag);
		int matched = 0;

		if (holders.isPresent()) {
			for (Holder<Fluid> holder : holders.get()) {
				for (String alias : aliases) {
					if (isValidAlias(alias)) {
						registration.addAlias(holder.value(), alias);
					}
				}

				matched++;
			}
		}

		if (matched == 0) {
			ConsoleJS.CLIENT.warn("Nebula JEI alias: fluid tag '%s' did not match any registered fluid (tag not loaded yet?)".formatted(tag.location()));
		}
	}
}