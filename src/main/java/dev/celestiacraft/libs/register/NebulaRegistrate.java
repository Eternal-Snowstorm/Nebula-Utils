package dev.celestiacraft.libs.register;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.builders.FluidBuilder;
import dev.celestiacraft.libs.register.builder.block.NebulaBlockBuilder;
import dev.celestiacraft.libs.register.builder.effect.NebulaEffectBuilder;
import dev.celestiacraft.libs.register.builder.fluid.NebulaFluidBuilder;
import dev.celestiacraft.libs.register.builder.item.NebulaItemBuilder;
import dev.celestiacraft.libs.register.builder.recipe.NebulaRecipeBuilder;
import dev.celestiacraft.libs.register.builder.recipe.NebulaRecipeTypeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.Item;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class NebulaRegistrate extends AbstractRegistrate<NebulaRegistrate> {
	protected NebulaRegistrate(String modid) {
		super(modid);
	}

	public static NebulaRegistrate create(String modid) {
		NebulaRegistrate registrate = new NebulaRegistrate(modid);
		NebulaRegistrateCallback.provideRegistrate(registrate);

		return registrate;
	}

	@Override
	public @NotNull NebulaRegistrate registerEventListeners(@NotNull IEventBus bus) {
		return super.registerEventListeners(bus);
	}

	@Override
	public <T extends Item> @NotNull NebulaItemBuilder<T, NebulaRegistrate> item(@NotNull NonNullFunction<Item.Properties, T> factory) {
		return item(self(), factory);
	}

	@Override
	public <T extends Item> @NotNull NebulaItemBuilder<T, NebulaRegistrate> item(@NotNull String name, @NotNull NonNullFunction<Item.Properties, T> factory) {
		return item(self(), name, factory);
	}

	@Override
	public <T extends Item, P> @NotNull NebulaItemBuilder<T, P> item(@NotNull P parent, @NotNull NonNullFunction<Item.Properties, T> factory) {
		return item(parent, currentName(), factory);
	}

	@Override
	public <T extends Item, P> @NotNull NebulaItemBuilder<T, P> item(@NotNull P parent, @NotNull String name, @NotNull NonNullFunction<Item.Properties, T> factory) {
		return NebulaItemBuilder.create(this, parent, name, this::accept, factory);
	}

	@Override
	public <T extends Block> @NotNull NebulaBlockBuilder<T, NebulaRegistrate> block(@NotNull NonNullFunction<BlockBehaviour.Properties, T> factory) {
		return block(self(), factory);
	}

	@Override
	public <T extends Block> @NotNull NebulaBlockBuilder<T, NebulaRegistrate> block(@NotNull String name, @NotNull NonNullFunction<BlockBehaviour.Properties, T> factory) {
		return block(self(), name, factory);
	}

	@Override
	public <T extends Block, P> @NotNull NebulaBlockBuilder<T, P> block(@NotNull P parent, @NotNull NonNullFunction<BlockBehaviour.Properties, T> factory) {
		return block(parent, currentName(), factory);
	}

	@Override
	public <T extends Block, P> @NotNull NebulaBlockBuilder<T, P> block(@NotNull P parent, @NotNull String name, @NotNull NonNullFunction<BlockBehaviour.Properties, T> factory) {
		return NebulaBlockBuilder.create(this, parent, name, this::accept, factory);
	}

	@Override
	public <T extends ForgeFlowingFluid> @NotNull NebulaFluidBuilder<T, NebulaRegistrate> fluid(
			@NotNull String name, @NotNull ResourceLocation stillTexture, @NotNull ResourceLocation flowingTexture,
			@NotNull NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory
	) {
		return fluid(name, stillTexture, flowingTexture, (FluidBuilder.FluidTypeFactory) null, fluidFactory);
	}

	@Override
	public @NotNull NebulaFluidBuilder<ForgeFlowingFluid.Flowing, NebulaRegistrate> fluid(
			@NotNull String name, @NotNull ResourceLocation stillTexture, @NotNull ResourceLocation flowingTexture
	) {
		return fluid(name, stillTexture, flowingTexture, (FluidBuilder.FluidTypeFactory) null, ForgeFlowingFluid.Flowing::new);
	}

	@Override
	public @NotNull NebulaFluidBuilder<ForgeFlowingFluid.Flowing, NebulaRegistrate> fluid(
			@NotNull String name, @NotNull ResourceLocation stillTexture, @NotNull ResourceLocation flowingTexture,
			@NotNull FluidBuilder.FluidTypeFactory typeFactory
	) {
		return fluid(name, stillTexture, flowingTexture, typeFactory, ForgeFlowingFluid.Flowing::new);
	}

	@Override
	public <T extends ForgeFlowingFluid> @NotNull NebulaFluidBuilder<T, NebulaRegistrate> fluid(
			@NotNull String name, @NotNull ResourceLocation stillTexture, @NotNull ResourceLocation flowingTexture,
			@NotNull FluidBuilder.FluidTypeFactory typeFactory,
			@NotNull NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory
	) {
		return NebulaFluidBuilder.create(this, self(), name, this::accept, stillTexture, flowingTexture, typeFactory, fluidFactory);
	}

	public <T extends Recipe<?>> @NotNull NebulaRecipeBuilder<T> recipe(@NotNull String name, @NotNull NonNullSupplier<? extends RecipeSerializer<T>> serializer) {
		return new NebulaRecipeBuilder<>(this, name, serializer);
	}

	public <T extends Recipe<?>> @NotNull NebulaRecipeTypeBuilder<T> recipeType(@NotNull String name, @NotNull NonNullSupplier<RecipeType<T>> factory, @NotNull Supplier<RecipeSerializer<T>> serializer) {
		return new NebulaRecipeTypeBuilder<>(this, self(), name, this::accept, factory, serializer);
	}

	public <T extends MobEffect> @NotNull NebulaEffectBuilder<T, NebulaRegistrate> effect(@NotNull NonNullBiFunction<MobEffectCategory, Integer, T> factory) {
		return effect(currentName(), factory);
	}

	public <T extends MobEffect> @NotNull NebulaEffectBuilder<T, NebulaRegistrate> effect(@NotNull String name, @NotNull NonNullBiFunction<MobEffectCategory, Integer, T> factory) {
		return effect(self(), name, factory);
	}

	public <T extends MobEffect, P> @NotNull NebulaEffectBuilder<T, P> effect(@NotNull P parent, @NotNull String name, @NotNull NonNullBiFunction<MobEffectCategory, Integer, T> factory) {
		return entry(name, (callback) -> {
			return NebulaEffectBuilder.create(this, parent, name, callback, factory);
		});
	}
}