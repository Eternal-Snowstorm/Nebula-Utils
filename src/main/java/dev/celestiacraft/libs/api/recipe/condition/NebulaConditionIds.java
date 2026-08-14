package dev.celestiacraft.libs.api.recipe.condition;

import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.api.recipe.condition.fluid_tag_exists.FluidTagExistsConditionSerializer;
import dev.celestiacraft.libs.api.recipe.condition.item_tag_exists.ItemTagExistsConditionSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;

public class NebulaConditionIds {
	public static final ResourceLocation
			ITEM_TAG_EXISTS,
			FLUID_TAG_EXISTS;

	static {
		ITEM_TAG_EXISTS = addCondition("item_tag_exists");
		FLUID_TAG_EXISTS = addCondition("fluid_tag_exists");
	}

	private static ResourceLocation addCondition(String name) {
		return NebulaLibs.loadResource(name);
	}

	public static void register() {
		CraftingHelper.register(new ItemTagExistsConditionSerializer());
		CraftingHelper.register(new FluidTagExistsConditionSerializer());
	}
}