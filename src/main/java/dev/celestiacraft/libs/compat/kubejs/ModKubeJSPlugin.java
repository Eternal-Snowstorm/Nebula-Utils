package dev.celestiacraft.libs.compat.kubejs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.GsonBuildConfig;
import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.Lang;
import dev.celestiacraft.libs.compat.jade.CommonJadeTipProvider;
import dev.celestiacraft.libs.compat.tconstruct.util.SimpleTConUtils;
import dev.celestiacraft.libs.wrapper.IntWrapper;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.compat.ICheckModLoaded;
import dev.celestiacraft.libs.compat.curios.CuriosUtils;
import dev.celestiacraft.libs.compat.patchouli.multiblock.DefineBlockBuilder;
import dev.celestiacraft.libs.compat.patchouli.multiblock.MultiblockStructureBuilder;
import dev.celestiacraft.libs.compat.patchouli.multiblock.PropertyImmutableMap;
import dev.celestiacraft.libs.tags.TagsBuilder;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import top.theillusivec4.curios.api.CuriosApi;

import java.time.LocalDateTime;

public class ModKubeJSPlugin extends KubeJSPlugin {
	public void registerEvents() {
		super.registerEvents();
	}

	public void registerBindings(BindingsEvent event) {
		super.registerBindings(event);

		event.add("NebulaLibs", NebulaLibs.class);
		event.add("RegistryInfo", RegistryInfo.class);
		event.add("LocalDateTime", LocalDateTime.class);
		event.add("Player", Player.class);
		event.add("InteractionHand", InteractionHand.class);
		event.add("Rarity", Rarity.class);
		event.add("ItemTags", ItemTags.class);
		event.add("BlockTags", BlockTags.class);
		event.add("FluidTags", FluidTags.class);
		event.add("ForgeTags", Tags.class);
		event.add("ForgeTags$Items", Tags.Items.class);
		event.add("ForgeTags$Blocks", Tags.Blocks.class);
		event.add("ForgeTags$Fluids", Tags.Fluids.class);
		event.add("ChatFormatting", ChatFormatting.class);
		event.add("ForgeRegistries", ForgeRegistries.class);
		event.add("BuiltInRegistries", BuiltInRegistries.class);
		event.add("ParticleTypes", ParticleTypes.class);
		event.add("TagsBuidlder", TagsBuilder.class);
		event.add("ItemClass", Item.class);
		event.add("BlockClass", Block.class);
		event.add("FluidClass", Fluid.class);
		event.add("Item$Properties", Item.Properties.class);
		event.add("BlockBehaviour$Properties", BlockBehaviour.Properties.class);
		event.add("IntWrapper", IntWrapper.class);
		event.add("Gson", Gson.class);
		event.add("GsonBuilder", GsonBuilder.class);
		event.add("GsonBuildConfig", GsonBuildConfig.class);

		if (ICheckModLoaded.hasCreate()) {
			event.add("AllSoundEvents", AllSoundEvents.class);
			event.add("AllParticleTypes", AllParticleTypes.class);
			event.add("TooltipHelper", TooltipHelper.class);
			event.add("CreateLang", Lang.class);
		}

		if (ICheckModLoaded.hasJade()) {
			event.add("CommonJadeTipProvider", CommonJadeTipProvider.class);
		}

		if (ICheckModLoaded.hasCurios()) {
			event.add("CuriosUtils", CuriosUtils.class);
			event.add("CuriosApi", CuriosApi.class);
		}

		if (ICheckModLoaded.hasTCon()) {
			event.add("SimpleTConUtils", SimpleTConUtils.class);
			event.add("ToolDefinition", ToolDefinition.class);
			event.add("ModifiableItem", ModifiableItem.class);
		}

		if (ICheckModLoaded.hasPatchouli()) {
			event.add("MultiblockStructureBuilder", MultiblockStructureBuilder.class);
			event.add("DefineBlockBuilder", DefineBlockBuilder.class);
			event.add("PropertyImmutableMap", PropertyImmutableMap.class);
		}
	}
}