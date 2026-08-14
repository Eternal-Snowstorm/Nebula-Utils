package dev.celestiacraft.libs.api.recipe.condition.fluid_tag_exists;

import dev.celestiacraft.libs.api.recipe.condition.NebulaConditionIds;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.crafting.conditions.ICondition;

@AllArgsConstructor
public class FluidTagExistsCondition implements ICondition {
	@Getter
	private final TagKey<Fluid> tag;

	@Override
	public ResourceLocation getID() {
		return NebulaConditionIds.FLUID_TAG_EXISTS;
	}

	@Override
	public boolean test(IContext context) {
		return !context.getTag(tag).isEmpty();
	}
}