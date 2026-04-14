package dev.celestiacraft.libs.common.recipe.machine;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MachineRecipe implements Recipe<Container> {
	private final ResourceLocation id;
	@Getter
	private final ResourceLocation recipeTypeId;
	@Getter
	private final List<ItemInput> inputItems;
	@Getter
	private final List<ItemOutput> outputItems;
	@Getter
	private final List<FluidInput> inputFluids;
	@Getter
	private final List<FluidOutput> outputFluids;
	@Getter
	private final int inputEnergy;
	@Getter
	private final int outputEnergy;
	@Getter
	private final int duration;

	public MachineRecipe(
			ResourceLocation id,
			ResourceLocation recipeTypeId,
			List<ItemInput> inputItems,
			List<ItemOutput> outputItems,
			List<FluidInput> inputFluids,
			List<FluidOutput> outputFluids,
			int inputEnergy,
			int outputEnergy,
			int duration
	) {
		this.id = id;
		this.recipeTypeId = recipeTypeId;
		this.inputItems = List.copyOf(inputItems);
		this.outputItems = List.copyOf(outputItems);
		this.inputFluids = List.copyOf(inputFluids);
		this.outputFluids = List.copyOf(outputFluids);
		this.inputEnergy = Math.max(0, inputEnergy);
		this.outputEnergy = Math.max(0, outputEnergy);
		this.duration = Math.max(1, duration);
	}

	public static MachineRecipe fromJson(ResourceLocation id, ResourceLocation recipeTypeId, JsonObject json) {
		List<ItemInput> inputItems = new ArrayList<>();
		List<ItemOutput> outputItems = new ArrayList<>();
		List<FluidInput> inputFluids = new ArrayList<>();
		List<FluidOutput> outputFluids = new ArrayList<>();

		if (json.has("inputItems")) {
			for (JsonElement element : GsonHelper.getAsJsonArray(json, "inputItems")) {
				inputItems.add(ItemInput.fromJson(GsonHelper.convertToJsonObject(element, "inputItems")));
			}
		}

		if (json.has("outputItems")) {
			for (JsonElement element : GsonHelper.getAsJsonArray(json, "outputItems")) {
				outputItems.add(ItemOutput.fromJson(GsonHelper.convertToJsonObject(element, "outputItems")));
			}
		}

		if (json.has("inputFluids")) {
			for (JsonElement element : GsonHelper.getAsJsonArray(json, "inputFluids")) {
				inputFluids.add(FluidInput.fromJson(GsonHelper.convertToJsonObject(element, "inputFluids")));
			}
		}

		if (json.has("outputFluids")) {
			for (JsonElement element : GsonHelper.getAsJsonArray(json, "outputFluids")) {
				outputFluids.add(FluidOutput.fromJson(GsonHelper.convertToJsonObject(element, "outputFluids")));
			}
		}

		return new MachineRecipe(
				id,
				recipeTypeId,
				inputItems,
				outputItems,
				inputFluids,
				outputFluids,
				GsonHelper.getAsInt(json, "inputEnergy", 0),
				GsonHelper.getAsInt(json, "outputEnergy", 0),
				GsonHelper.getAsInt(json, "duration", 20)
		);
	}

	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.addProperty("duration", duration);

		if (!inputItems.isEmpty()) {
			JsonArray array = new JsonArray();
			inputItems.forEach(entry -> array.add(entry.toJson()));
			json.add("inputItems", array);
		}

		if (!outputItems.isEmpty()) {
			JsonArray array = new JsonArray();
			outputItems.forEach(entry -> array.add(entry.toJson()));
			json.add("outputItems", array);
		}

		if (!inputFluids.isEmpty()) {
			JsonArray array = new JsonArray();
			inputFluids.forEach(entry -> array.add(entry.toJson()));
			json.add("inputFluids", array);
		}

		if (!outputFluids.isEmpty()) {
			JsonArray array = new JsonArray();
			outputFluids.forEach(entry -> array.add(entry.toJson()));
			json.add("outputFluids", array);
		}

		if (inputEnergy > 0) {
			json.addProperty("inputEnergy", inputEnergy);
		}

		if (outputEnergy > 0) {
			json.addProperty("outputEnergy", outputEnergy);
		}

		return json;
	}

	public static MachineRecipe fromNetwork(ResourceLocation id, ResourceLocation recipeTypeId, FriendlyByteBuf buf) {
		List<ItemInput> inputItems = buf.readList(ItemInput::fromNetwork);
		List<ItemOutput> outputItems = buf.readList(ItemOutput::fromNetwork);
		List<FluidInput> inputFluids = buf.readList(FluidInput::fromNetwork);
		List<FluidOutput> outputFluids = buf.readList(FluidOutput::fromNetwork);
		int inputEnergy = buf.readVarInt();
		int outputEnergy = buf.readVarInt();
		int duration = buf.readVarInt();

		return new MachineRecipe(id, recipeTypeId, inputItems, outputItems, inputFluids, outputFluids, inputEnergy, outputEnergy, duration);
	}

	public void toNetwork(FriendlyByteBuf buf) {
		buf.writeCollection(inputItems, (buffer, value) -> value.toNetwork(buffer));
		buf.writeCollection(outputItems, (buffer, value) -> value.toNetwork(buffer));
		buf.writeCollection(inputFluids, (buffer, value) -> value.toNetwork(buffer));
		buf.writeCollection(outputFluids, (buffer, value) -> value.toNetwork(buffer));
		buf.writeVarInt(inputEnergy);
		buf.writeVarInt(outputEnergy);
		buf.writeVarInt(duration);
	}

	public boolean matchesItemInputs(IItemHandler handler, int startSlot) {
		for (int i = 0; i < inputItems.size(); i++) {
			if (!inputItems.get(i).matches(handler.getStackInSlot(startSlot + i))) {
				return false;
			}
		}
		return true;
	}

	public boolean matchesFluidInputs(IFluidHandler handler, int startTank) {
		for (int i = 0; i < inputFluids.size(); i++) {
			if (!inputFluids.get(i).matches(handler.getFluidInTank(startTank + i))) {
				return false;
			}
		}
		return true;
	}

	public boolean canOutputItems(IItemHandler handler, int startSlot) {
		for (int i = 0; i < outputItems.size(); i++) {
			ItemStack remaining = handler.insertItem(startSlot + i, outputItems.get(i).stack.copy(), true);
			if (!remaining.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public boolean canOutputFluids(IFluidHandler handler) {
		for (FluidOutput output : outputFluids) {
			int filled = handler.fill(output.stack.copy(), IFluidHandler.FluidAction.SIMULATE);
			if (filled < output.stack.getAmount()) {
				return false;
			}
		}
		return true;
	}

	public void consumeItemInputs(IItemHandler handler, int startSlot) {
		for (int i = 0; i < inputItems.size(); i++) {
			handler.extractItem(startSlot + i, inputItems.get(i).count, false);
		}
	}

	public void produceItemOutputs(Level level, IItemHandler handler, int startSlot) {
		for (int i = 0; i < outputItems.size(); i++) {
			ItemOutput output = outputItems.get(i);
			if (output.roll(level)) {
				handler.insertItem(startSlot + i, output.stack.copy(), false);
			}
		}
	}

	public void produceFluidOutputs(Level level, IFluidHandler handler) {
		for (FluidOutput output : outputFluids) {
			if (output.roll(level)) {
				handler.fill(output.stack.copy(), IFluidHandler.FluidAction.EXECUTE);
			}
		}
	}

	@Override
	public boolean matches(@NotNull Container container, @NotNull Level level) {
		return false;
	}

	@Override
	public @NotNull ItemStack assemble(@NotNull Container container, @NotNull RegistryAccess access) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return false;
	}

	@Override
	public @NotNull ItemStack getResultItem(@NotNull RegistryAccess access) {
		return outputItems.isEmpty() ? ItemStack.EMPTY : outputItems.get(0).stack.copy();
	}

	@Override
	public @NotNull ResourceLocation getId() {
		return id;
	}

	@Override
	public @NotNull RecipeSerializer<?> getSerializer() {
		RecipeSerializer<?> serializer = ForgeRegistries.RECIPE_SERIALIZERS.getValue(recipeTypeId);
		return Objects.requireNonNull(serializer, "Missing recipe serializer: " + recipeTypeId);
	}

	@Override
	public @NotNull RecipeType<?> getType() {
		RecipeType<?> type = ForgeRegistries.RECIPE_TYPES.getValue(recipeTypeId);
		return Objects.requireNonNull(type, "Missing recipe type: " + recipeTypeId);
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	private static JsonObject writeStackJson(String key, ResourceLocation id, int amount, CompoundTag tag, double chance) {
		JsonObject json = new JsonObject();
		json.addProperty(key, id.toString());
		if (amount != 1) {
			json.addProperty("count", amount);
		}
		if (tag != null && !tag.isEmpty()) {
			json.addProperty("nbt", tag.toString());
		}
		if (chance != 1D) {
			json.addProperty("chance", chance);
		}
		return json;
	}

	private static void readTag(JsonObject json, ConsumerWithException<CompoundTag> consumer, String messagePrefix) {
		if (!json.has("nbt")) {
			return;
		}

		try {
			consumer.accept(TagParser.parseTag(GsonHelper.getAsString(json, "nbt")));
		} catch (Exception exception) {
			throw new IllegalStateException(messagePrefix + GsonHelper.getAsString(json, "nbt"), exception);
		}
	}

	private static ItemStack readItemStack(JsonObject json) {
		String itemId = GsonHelper.getAsString(json, "item");
		Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(itemId));
		if (item == null) {
			throw new IllegalStateException("Unknown item: " + itemId);
		}

		ItemStack stack = new ItemStack(item, GsonHelper.getAsInt(json, "count", 1));
		readTag(json, stack::setTag, "Invalid item nbt: ");
		return stack;
	}

	private static FluidStack readFluidStack(JsonObject json) {
		String fluidId = GsonHelper.getAsString(json, "fluid");
		var fluid = ForgeRegistries.FLUIDS.getValue(ResourceLocation.parse(fluidId));
		if (fluid == null) {
			throw new IllegalStateException("Unknown fluid: " + fluidId);
		}

		FluidStack stack = new FluidStack(fluid, GsonHelper.getAsInt(json, "amount"));
		readTag(json, stack::setTag, "Invalid fluid nbt: ");
		return stack;
	}

	@FunctionalInterface
	private interface ConsumerWithException<T> {
		void accept(T value) throws Exception;
	}

	public record ItemInput(Ingredient ingredient, int count) {
		public ItemInput {
			count = Math.max(1, count);
		}

		public boolean matches(ItemStack stack) {
			return !stack.isEmpty() && stack.getCount() >= count && ingredient.test(stack);
		}

		public JsonObject toJson() {
			JsonObject json = new JsonObject();
			json.add("ingredient", ingredient.toJson());
			json.addProperty("count", count);
			return json;
		}

		public static ItemInput fromJson(JsonObject json) {
			return new ItemInput(Ingredient.fromJson(json.get("ingredient")), GsonHelper.getAsInt(json, "count", 1));
		}

		public void toNetwork(FriendlyByteBuf buf) {
			ingredient.toNetwork(buf);
			buf.writeVarInt(count);
		}

		public static ItemInput fromNetwork(FriendlyByteBuf buf) {
			return new ItemInput(Ingredient.fromNetwork(buf), buf.readVarInt());
		}
	}

	public record ItemOutput(ItemStack stack, double chance) {
		public ItemOutput {
			stack = stack.copy();
		}

		public boolean roll(Level level) {
			return chance >= 1D || level.random.nextDouble() <= chance;
		}

		public JsonObject toJson() {
			ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
			if (itemId == null) {
				throw new IllegalStateException("Unregistered item: " + stack.getItem());
			}
			return writeStackJson("item", itemId, stack.getCount(), stack.getTag(), chance);
		}

		public static ItemOutput fromJson(JsonObject json) {
			return new ItemOutput(readItemStack(json), GsonHelper.getAsDouble(json, "chance", 1D));
		}

		public void toNetwork(FriendlyByteBuf buf) {
			buf.writeItem(stack);
			buf.writeDouble(chance);
		}

		public static ItemOutput fromNetwork(FriendlyByteBuf buf) {
			return new ItemOutput(buf.readItem(), buf.readDouble());
		}
	}

	public record FluidInput(FluidStack stack) {
		public FluidInput {
			stack = stack.copy();
		}

		public boolean matches(FluidStack other) {
			return !other.isEmpty()
					&& other.getAmount() >= stack.getAmount()
					&& other.isFluidEqual(stack)
					&& FluidStack.areFluidStackTagsEqual(stack, other);
		}

		public JsonObject toJson() {
			ResourceLocation fluidId = ForgeRegistries.FLUIDS.getKey(stack.getFluid());
			if (fluidId == null) {
				throw new IllegalStateException("Unregistered fluid: " + stack.getFluid());
			}
			return writeStackJson("fluid", fluidId, stack.getAmount(), stack.getTag(), 1D);
		}

		public static FluidInput fromJson(JsonObject json) {
			return new FluidInput(readFluidStack(json));
		}

		public void toNetwork(FriendlyByteBuf buf) {
			buf.writeFluidStack(stack);
		}

		public static FluidInput fromNetwork(FriendlyByteBuf buf) {
			return new FluidInput(buf.readFluidStack());
		}
	}

	public record FluidOutput(FluidStack stack, double chance) {
		public FluidOutput {
			stack = stack.copy();
		}

		public boolean roll(Level level) {
			return chance >= 1D || level.random.nextDouble() <= chance;
		}

		public JsonObject toJson() {
			ResourceLocation fluidId = ForgeRegistries.FLUIDS.getKey(stack.getFluid());
			if (fluidId == null) {
				throw new IllegalStateException("Unregistered fluid: " + stack.getFluid());
			}
			return writeStackJson("fluid", fluidId, stack.getAmount(), stack.getTag(), chance);
		}

		public static FluidOutput fromJson(JsonObject json) {
			return new FluidOutput(readFluidStack(json), GsonHelper.getAsDouble(json, "chance", 1D));
		}

		public void toNetwork(FriendlyByteBuf buf) {
			buf.writeFluidStack(stack);
			buf.writeDouble(chance);
		}

		public static FluidOutput fromNetwork(FriendlyByteBuf buf) {
			return new FluidOutput(buf.readFluidStack(), buf.readDouble());
		}
	}
}