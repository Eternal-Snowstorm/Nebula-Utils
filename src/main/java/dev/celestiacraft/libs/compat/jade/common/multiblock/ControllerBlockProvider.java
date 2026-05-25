package dev.celestiacraft.libs.compat.jade.common.multiblock;

import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.api.register.multiblock.ControllerBlock;
import dev.celestiacraft.libs.compat.patchouli.multiblock.IMultiblockProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum ControllerBlockProvider implements IBlockComponentProvider {
	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		Block block = accessor.getBlock();

		if (!(block instanceof ControllerBlock<?> controller)) {
			return;
		}

		BlockEntity be = accessor.getBlockEntity();

		if (!(be instanceof IMultiblockProvider provider)) {
			return;
		}

		if (provider.isStructureValid()) {
			tooltip.add(Component.translatable("tip.structurally_valid").withStyle(ChatFormatting.GREEN));
		} else {
			tooltip.add(Component.translatable("tip.structurally_invalid").withStyle(ChatFormatting.RED));
		}
	}

	@Override
	public ResourceLocation getUid() {
		return NebulaLibs.loadResource("common");
	}
}