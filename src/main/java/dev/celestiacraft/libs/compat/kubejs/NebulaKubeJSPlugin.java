package dev.celestiacraft.libs.compat.kubejs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.GsonBuildConfig;
import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.api.register.tool.tier.TierBuilder;
import dev.celestiacraft.libs.client.NebulaLang;
import dev.celestiacraft.libs.common.material.*;
import dev.celestiacraft.libs.common.material.event.RegisterMaterialEvent;
import dev.celestiacraft.libs.compat.ICheckModLoaded;
import dev.celestiacraft.libs.compat.curios.ICuriosHelper;
import dev.celestiacraft.libs.compat.jade.util.CommonJadeTipProvider;
import dev.celestiacraft.libs.compat.kubejs.event.NebulaEventJS;
import dev.celestiacraft.libs.compat.kubejs.recipe.AnvilCraftSchema;
import dev.celestiacraft.libs.compat.patchouli.multiblock.*;
import dev.celestiacraft.libs.compat.tconstruct.util.SimpleTConUtils;
import dev.celestiacraft.libs.debug.DebugUserManager;
import dev.celestiacraft.libs.tags.TagsBuilder;
import dev.celestiacraft.libs.utils.FestivalUtils;
import dev.celestiacraft.libs.wrapper.IntWrapper;
import dev.celestiacraft.libs.wrapper.gson.JsonWrapper;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import top.theillusivec4.curios.api.CuriosApi;

import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.IntStream;

public class NebulaKubeJSPlugin extends KubeJSPlugin {
	@Override
	public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
		event.namespace(NebulaLibs.MODID)
				.register("anvil_craft", AnvilCraftSchema.SCHEMA);
	}

	@Override
	public void registerEvents() {
		NebulaEventJS.init();
	}

	public void registerBindings(BindingsEvent event) {
		event.add("NebulaLibs", NebulaLibs.class);
		event.add("NebulaLang", NebulaLang.class);
		event.add("NebulaLang$JeiLang", NebulaLang.JeiLang.class);

		bindCommon(event);
		bindJson(event);
		bindMaterial(event);
		bindTags(event);
		bindTools(event);
		bindTCon(event);
		bindRegister(event);
		bindCreate(event);
		bindPatchouli(event);
		bindCurios(event);
		bindJade(event);
	}

	private void bindCommon(BindingsEvent event) {
		event.add("ParticleTypes", ParticleTypes.class);
		event.add("Player", Player.class);
		event.add("InteractionHand", InteractionHand.class);
		event.add("Rarity", Rarity.class);
		event.add("ChatFormatting", ChatFormatting.class);
		event.add("TierBuilder", TierBuilder.class);
		event.add("Item$Properties", Item.Properties.class);
		event.add("BlockBehaviour$Properties", BlockBehaviour.Properties.class);
		event.add("BlockItem", BlockItem.class);
	}

	private void bindJson(BindingsEvent event) {
		event.add("JsonWrapper", JsonWrapper.class);
		event.add("Gson", Gson.class);
		event.add("GsonBuilder", GsonBuilder.class);
		event.add("GsonBuildConfig", GsonBuildConfig.class);
	}

	private void bindMaterial(BindingsEvent event) {
		event.add("NebulaMaterial", NebulaMaterial.class);
		event.add("IMaterialType", IMaterialType.class);
		event.add("MaterialTypes", MaterialTypes.class);
		event.add("IMaterialKind", IMaterialKind.class);
		event.add("MaterialKinds", MaterialKinds.class);
		event.add("IMiningLevel", IMiningLevel.class);
		event.add("MiningLevels", MiningLevels.class);
		event.add("MaterialManager", MaterialManager.class);
		event.add("RegisterMaterialEvent", RegisterMaterialEvent.class);
	}

	private void bindTools(BindingsEvent event) {
		event.add("LocalDateTime", LocalDateTime.class);
		event.add("IntWrapper", IntWrapper.class);
		event.add("IntStream", IntStream.class);
		event.add("JavaArray", Array.class);
		event.add("JavaArrays", Arrays.class);
		event.add("DebugUserManager", DebugUserManager.class);
		event.add("FestivalUtils", FestivalUtils.class);
	}

	private void bindTags(BindingsEvent event) {
		event.add("ItemTags", ItemTags.class);
		event.add("BlockTags", BlockTags.class);
		event.add("FluidTags", FluidTags.class);
		event.add("ForgeTags", Tags.class);
		event.add("ForgeTags$Items", Tags.Items.class);
		event.add("ForgeTags$Blocks", Tags.Blocks.class);
		event.add("ForgeTags$Fluids", Tags.Fluids.class);
		event.add("TagsBuidlder", TagsBuilder.class);
	}

	private void bindRegister(BindingsEvent event) {
		event.add("ForgeRegistries", ForgeRegistries.class);
		event.add("BuiltInRegistries", BuiltInRegistries.class);
		event.add("RegistryInfo", RegistryInfo.class);
	}

	private void bindCreate(BindingsEvent event) {
		if (ICheckModLoaded.hasCreate()) {
			event.add("AllSoundEvents", AllSoundEvents.class);
			event.add("AllParticleTypes", AllParticleTypes.class);
			event.add("TooltipHelper", TooltipHelper.class);
			event.add("FontHelper$Palette", FontHelper.Palette.class);
			event.add("CreateLang", CreateLang.class);
		}
	}

	private void bindTCon(BindingsEvent event) {
		if (ICheckModLoaded.hasTCon()) {
			event.add("SimpleTConUtils", SimpleTConUtils.class);
			event.add("ToolDefinition", ToolDefinition.class);
			event.add("ModifiableItem", ModifiableItem.class);
		}
	}

	private void bindPatchouli(BindingsEvent event) {
		if (ICheckModLoaded.hasPatchouli()) {
			event.add("StructureBuilder", StructureBuilder.class);
			event.add("DefineBlockBuilder", DefineBlockBuilder.class);
			event.add("PropertyImmutableMap", PropertyImmutableMap.class);
			event.add("MultiblockHandler", MultiblockHandler.class);
			event.add("MultiblockHandler$Builder", MultiblockHandler.Builder.class);
			event.add("IMultiblockProvider", IMultiblockProvider.class);
			event.add("BlockEntityContext", BlockEntityContext.class);
			event.add("MultiblockContext", IMultiblockContext.class);
			event.add("WorldContext", WorldContext.class);
		}
	}

	private void bindCurios(BindingsEvent event) {
		if (ICheckModLoaded.hasCurios()) {
			event.add("CuriosUtils", ICuriosHelper.class);
			event.add("CuriosApi", CuriosApi.class);
		}
	}

	private void bindJade(BindingsEvent event) {
		if (ICheckModLoaded.hasJade()) {
			event.add("CommonJadeTipProvider", CommonJadeTipProvider.class);
		}
	}
}