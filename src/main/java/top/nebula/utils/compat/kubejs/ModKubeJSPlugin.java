package top.nebula.utils.compat.kubejs;

import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.AllSoundEvents;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import top.nebula.utils.NebulaUtils;
import top.nebula.utils.compat.kubejs.event.NebulaEvents;
import top.nebula.utils.multiblock.DefineBlockBuilder;
import top.nebula.utils.multiblock.MultiblockStructureBuilder;
import top.nebula.utils.multiblock.PropertyImmutableMap;

import java.time.LocalDateTime;

public class ModKubeJSPlugin extends KubeJSPlugin {
	public void registerEvents() {
		super.registerEvents();

		NebulaEvents.GROUP.register();
	}

	public void registerBindings(BindingsEvent event) {
		super.registerBindings(event);

		event.add("NebulaUtils", NebulaUtils.class);
		event.add("MultiblockStructureBuilder", MultiblockStructureBuilder.class);
		event.add("DefineBlockBuilder", DefineBlockBuilder.class);
		event.add("PropertyImmutableMap", PropertyImmutableMap.class);
		event.add("RegistryInfo", RegistryInfo.class);
		event.add("LocalDateTime", LocalDateTime.class);
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
		event.add("AllSoundEvents", AllSoundEvents.class);
		event.add("ParticleTypes", ParticleTypes.class);
		event.add("AllParticleTypes", AllParticleTypes.class);
	}
}