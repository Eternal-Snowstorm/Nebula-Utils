package dev.celestiacraft.libs.api.recipe.ingredient.fluid;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class NebulaFluidTagIngredient extends NebulaFluidIngredient {
	private final TagKey<Fluid> tag;
	private final int amount;

	public NebulaFluidTagIngredient(TagKey<Fluid> tag, int amount) {
		this.tag = tag;
		this.amount = amount;
	}

	@Override
	public boolean test(FluidStack stack) {
		return stack != null && stack.getFluid().is(tag);
	}

	@Override
	public int getRequiredAmount() {
		return amount;
	}

	@Override
	public List<FluidStack> getMatchingFluidStacks() {
		return ForgeRegistries.FLUIDS.tags()
				.getTag(tag)
				.stream()
				.map((fluid) -> {
					return new FluidStack(fluid, amount);
				})
				.toList();
	}

	@Override
	protected void writeJson(JsonObject json) {
		json.addProperty("fluid_tag", tag.location().toString());
	}

	@Override
	protected void writeNetwork(FriendlyByteBuf buf) {
		buf.writeResourceLocation(tag.location());
	}
}