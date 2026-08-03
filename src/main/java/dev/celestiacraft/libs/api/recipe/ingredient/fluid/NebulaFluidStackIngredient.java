package dev.celestiacraft.libs.api.recipe.ingredient.fluid;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class NebulaFluidStackIngredient extends NebulaFluidIngredient{
	private final Fluid fluid;
	private final int amount;

	public NebulaFluidStackIngredient(Fluid fluid, int amount) {
		this.fluid = fluid;
		this.amount = amount;
	}

	@Override
	public boolean test(FluidStack stack) {
		return stack != null && stack.getFluid() == fluid;
	}

	@Override
	public int getRequiredAmount() {
		return amount;
	}

	@Override
	public List<FluidStack> getMatchingFluidStacks() {
		return List.of(new FluidStack(fluid, amount));
	}

	@Override
	protected void writeJson(JsonObject json) {
		json.addProperty("fluid", ForgeRegistries.FLUIDS.getKey(fluid).toString());
	}

	@Override
	protected void writeNetwork(FriendlyByteBuf buf) {
		buf.writeRegistryId(ForgeRegistries.FLUIDS, fluid);
	}
}