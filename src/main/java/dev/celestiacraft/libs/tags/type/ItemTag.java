package dev.celestiacraft.libs.tags.type;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import dev.celestiacraft.libs.tags.AbstractTagBuilder;

public class ItemTag extends AbstractTagBuilder<Item> {
	public ItemTag(String name) {
		super(name);
	}

	@Override
	protected TagKey<Item> create(ResourceLocation id) {
		return ItemTags.create(id);
	}
}