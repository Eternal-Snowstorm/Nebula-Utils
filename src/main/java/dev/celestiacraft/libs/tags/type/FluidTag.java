package dev.celestiacraft.libs.tags.type;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import dev.celestiacraft.libs.tags.AbstractTagBuilder;

public class FluidTag extends AbstractTagBuilder<Fluid> {
	public FluidTag(String name) {
		super(name);
	}

	@Override
	protected TagKey<Fluid> create(ResourceLocation id) {
		return FluidTags.create(id);
	}
}