package dev.celestiacraft.libs.api.recipe.condition.item_tag_exists;

import dev.celestiacraft.libs.api.recipe.condition.NebulaConditionIds;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.crafting.conditions.ICondition;

public class ItemTagExistsCondition implements ICondition {
	@Getter
	private final TagKey<Item> tag;

	public ItemTagExistsCondition(TagKey<Item> tag) {
		this.tag = tag;
	}

	public ItemTagExistsCondition(String tag) {
		this.tag = ItemTags.create(ResourceLocation.parse(tag));
	}

	@Override
	public ResourceLocation getID() {
		return NebulaConditionIds.ITEM_TAG_EXISTS;
	}

	@Override
	public boolean test(IContext context) {
		return !context.getTag(tag).isEmpty();
	}
}