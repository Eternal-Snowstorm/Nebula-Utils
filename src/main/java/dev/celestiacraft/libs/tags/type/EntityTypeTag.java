package dev.celestiacraft.libs.tags.type;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import dev.celestiacraft.libs.tags.AbstractTagBuilder;

public class EntityTypeTag extends AbstractTagBuilder<EntityType<?>> {
	public EntityTypeTag(String name) {
		super(name);
	}

	@Override
	protected TagKey<EntityType<?>> create(ResourceLocation id) {
		return TagKey.create(Registries.ENTITY_TYPE, id);
	}
}