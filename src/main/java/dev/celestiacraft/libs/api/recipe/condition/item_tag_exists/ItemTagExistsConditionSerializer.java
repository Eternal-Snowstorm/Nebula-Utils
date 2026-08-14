package dev.celestiacraft.libs.api.recipe.condition.item_tag_exists;

import com.google.gson.JsonObject;
import dev.celestiacraft.libs.api.recipe.condition.NebulaConditionIds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public class ItemTagExistsConditionSerializer implements IConditionSerializer<ItemTagExistsCondition> {
	@Override
	public void write(JsonObject json, ItemTagExistsCondition condition) {
		json.addProperty("tag", condition.getTag().location().toString());
	}

	@Override
	public ItemTagExistsCondition read(JsonObject json) {
		ResourceLocation id = ResourceLocation.parse(json.get("tag").getAsString());

		return new ItemTagExistsCondition(ItemTags.create(id));
	}

	@Override
	public ResourceLocation getID() {
		return NebulaConditionIds.ITEM_TAG_EXISTS;
	}
}