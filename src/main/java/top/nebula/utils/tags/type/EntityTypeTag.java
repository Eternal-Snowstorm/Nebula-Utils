package top.nebula.utils.tags.type;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import top.nebula.utils.tags.AbstractTagBuilder;

public class EntityTypeTag extends AbstractTagBuilder<EntityType<?>> {
	public EntityTypeTag(String name) {
		super(name);
	}

	@Override
	public TagKey<EntityType<?>> build() {
		return TagKey.create(Registries.ENTITY_TYPE, id());
	}
}
