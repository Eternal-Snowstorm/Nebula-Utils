package dev.celestiacraft.libs.api.recipe.condition.fluid_tag_exists;

import dev.celestiacraft.libs.api.recipe.condition.NebulaConditionIds;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.crafting.conditions.ICondition;

public class FluidTagExistsCondition implements ICondition {
	@Getter
	private final TagKey<Fluid> tag;

	public FluidTagExistsCondition(TagKey<Fluid> tag) {
		this.tag = tag;
	}

	public FluidTagExistsCondition(String tag) {
		this.tag = FluidTags.create(ResourceLocation.parse(tag));
	}

	@Override
	public ResourceLocation getID() {
		return NebulaConditionIds.FLUID_TAG_EXISTS;
	}

	@Override
	public boolean test(IContext context) {
		return !context.getTag(tag).isEmpty();
	}
}