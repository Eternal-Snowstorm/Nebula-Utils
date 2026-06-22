package dev.celestiacraft.libs.common.register;

import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.common.recipe.anvil_craft.AnvilCraftRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class NebulaRecipe {
	public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES;

	public static final Supplier<RecipeType<AnvilCraftRecipe>> ANVIL_CRAFT;

	static {
		RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, NebulaLibs.MODID);

		ANVIL_CRAFT = register("anvil_craft");
	}

	private static <T extends Recipe<?>> Supplier<RecipeType<T>> register(String name) {
		return RECIPE_TYPES.register(name, () -> new RecipeType<>() {
			@Override
			public String toString() {
				return NebulaLibs.MODID + ":" + name;
			}
		});
	}

	public static void register(IEventBus bus) {
		RECIPE_TYPES.register(bus);
		NebulaLibs.LOGGER.info("Nebula Libs RecipeTypes Registered!");
	}
}