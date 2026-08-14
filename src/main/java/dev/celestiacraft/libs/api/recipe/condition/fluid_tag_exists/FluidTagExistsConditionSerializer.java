package dev.celestiacraft.libs.api.recipe.condition.fluid_tag_exists;

import com.google.gson.JsonObject;
import dev.celestiacraft.libs.api.recipe.condition.NebulaConditionIds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public class FluidTagExistsConditionSerializer implements IConditionSerializer<FluidTagExistsCondition> {
	@Override
	public void write(JsonObject json, FluidTagExistsCondition condition) {
		json.addProperty("fluid_tag", condition.getTag().location().toString());
	}

	@Override
	public FluidTagExistsCondition read(JsonObject json) {
		ResourceLocation id = ResourceLocation.parse(json.get("fluid_tag").getAsString());

		return new FluidTagExistsCondition(FluidTags.create(id));
	}

	@Override
	public ResourceLocation getID() {
		return NebulaConditionIds.FLUID_TAG_EXISTS;
	}
}