package dev.celestiacraft.libs.api.recipe.ingredient.fluid;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public abstract class NebulaFluidIngredient implements Predicate<FluidStack> {
	public static NebulaFluidIngredient of(Fluid fluid, int amount) {
		return new NebulaFluidStackIngredient(fluid, amount);
	}

	public static NebulaFluidIngredient ofTag(TagKey<Fluid> tag, int amount) {
		return new NebulaFluidTagIngredient(tag, amount);
	}

	public static NebulaFluidIngredient ofTagId(ResourceLocation tag, int amount) {
		return new NebulaFluidTagIngredient(FluidTags.create(tag), amount);
	}

	public abstract int getRequiredAmount();

	public abstract List<FluidStack> getMatchingFluidStacks();

	public static NebulaFluidIngredient fromJson(@Nullable JsonElement element) {
		if (element == null || element.isJsonNull() || !element.isJsonObject()) {
			throw new JsonSyntaxException("Invalid fluid ingredient: " + element);
		}

		JsonObject json = element.getAsJsonObject();
		int amount = GsonHelper.getAsInt(json, "amount");

		if (json.has("fluid")) {
			String fluidString = GsonHelper.getAsString(json, "fluid");
			ResourceLocation id = ResourceLocation.tryParse(fluidString);
			Fluid fluid = id == null ? null : ForgeRegistries.FLUIDS.getValue(id);

			if (fluid == null || fluid == Fluids.EMPTY) {
				throw new JsonSyntaxException("Unknown fluid: '" + fluidString + "'");
			}
			return new NebulaFluidStackIngredient(fluid, amount);
		}
		if (json.has("tag")) {
			ResourceLocation id = ResourceLocation.tryParse(GsonHelper.getAsString(json, "tag"));

			if (id == null) {
				String tagString = GsonHelper.getAsString(json, "tag");
				throw new JsonSyntaxException("Invalid fluid tag: '" + tagString + "'");
			}
			return new NebulaFluidTagIngredient(FluidTags.create(id), amount);
		}
		throw new JsonSyntaxException("Fluid ingredient needs either 'fluid' or 'tag'");
	}

	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.addProperty("amount", getRequiredAmount());
		writeJson(json);
		return json;
	}

	protected abstract void writeJson(JsonObject json);

	public static NebulaFluidIngredient fromNetwork(FriendlyByteBuf buf) {
		boolean isTag = buf.readBoolean();
		int amount = buf.readVarInt();

		if (isTag) {
			TagKey<Fluid> tag = FluidTags.create(buf.readResourceLocation());
			return new NebulaFluidTagIngredient(tag, amount);
		}
		return new NebulaFluidStackIngredient(buf.readRegistryId(), amount);
	}

	public void toNetwork(FriendlyByteBuf buf) {
		buf.writeBoolean(this instanceof NebulaFluidTagIngredient);
		buf.writeVarInt(getRequiredAmount());
		writeNetwork(buf);
	}

	protected abstract void writeNetwork(FriendlyByteBuf buf);
}