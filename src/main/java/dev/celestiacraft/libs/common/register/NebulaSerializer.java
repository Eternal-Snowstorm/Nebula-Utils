package dev.celestiacraft.libs.common.register;

import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.common.recipe.anvil_craft.AnvilCraftRecipe;
import dev.celestiacraft.libs.common.recipe.anvil_craft.AnvilCraftSerializer;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class NebulaSerializer {
	public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS;

	public static final Supplier<RecipeSerializer<AnvilCraftRecipe>> ANVIL_CRAFT;

	static {
		SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, NebulaLibs.MODID);

		ANVIL_CRAFT = SERIALIZERS.register("anvil_craft", AnvilCraftSerializer::new);
	}

	public static void register(IEventBus bus) {
		SERIALIZERS.register(bus);
		NebulaLibs.LOGGER.info("Nebula Libs RecipeSerializers Registered!");
	}
}