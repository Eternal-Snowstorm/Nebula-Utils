package dev.celestiacraft.libs.debug;

import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.common.register.NebulaItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

/**
 * 改编自 Jasons-impart 的 Create-Delight-Remake 项目.
 * <p>
 * 感谢 Jasons-impart 的授权
 *
 * @see <a href="https://github.com/Jasons-impart/Create-Delight-Remake/blob/main/kubejs/server_scripts/Debug/Or_debug_boss.js">Create-Delight-Remake</a>
 */
@Mod.EventBusSubscriber(modid = NebulaLibs.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BlockEntityTool {
	@SubscribeEvent
	public static void onGetBlockEntity(PlayerInteractEvent.RightClickBlock event) {
		Player player = event.getEntity();
		Level level = event.getLevel();
		BlockPos pos = event.getPos();
		ItemStack item = player.getMainHandItem();

		if (level.isClientSide()) {
			return;
		}
		if (event.getHand() != InteractionHand.MAIN_HAND) {
			return;
		}
		if (!item.is(NebulaItem.BLOCK_ENTITY_TOOL.get())) {
			return;
		}

		BlockEntity entity = level.getBlockEntity(pos);
		if (entity == null) {
			player.sendSystemMessage(Component.literal("No BlockEntity at " + pos)
					.withStyle(ChatFormatting.RED));
			player.swing(InteractionHand.MAIN_HAND);
			event.setCanceled(true);
			return;
		}

		CompoundTag blockEntityNBT = entity.saveWithFullMetadata();

		player.swing(InteractionHand.MAIN_HAND);
		player.sendSystemMessage(Component.literal("BlockEntityNBT:")
				.withStyle(ChatFormatting.YELLOW));

		for (String key : blockEntityNBT.getAllKeys()) {
			Tag valueTag = blockEntityNBT.get(key);
			String valueStr = String.valueOf(valueTag);
			String line = key + ": " + valueStr;

			Component message = Component.literal("§7- §e" + key + "§7: §a" + valueStr)
					.withStyle((style) -> {
						return style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, line))
								.withHoverEvent(new HoverEvent(
										HoverEvent.Action.SHOW_TEXT,
										Component.literal("NBT " + key + " (Click to Copy)")
								));
					});

			player.sendSystemMessage(message);
		}

		event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onGetStructure(PlayerInteractEvent.RightClickItem event) {
		Level level = event.getLevel();
		Player player = event.getEntity();
		ItemStack stack = event.getItemStack();

		if (level.isClientSide()) {
			return;
		}
		if (!stack.is(NebulaItem.BLOCK_ENTITY_TOOL.get())) {
			return;
		}

		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}

		if (!player.isCrouching()) {
			event.setCanceled(true);
			return;
		}

		player.swing(event.getHand());

		BlockPos playerPos = player.blockPosition();
		Set<Structure> structureArray = serverLevel.structureManager()
				.getAllStructuresAt(playerPos)
				.keySet();

		Registry<Structure> structureRegistry = level.registryAccess()
				.registryOrThrow(Registries.STRUCTURE);

		for (Structure structure : structureArray) {
			StructureStart structureStart = serverLevel.structureManager()
					.getStructureAt(playerPos, structure);

			if (!structureStart.isValid()) {
				continue;
			}

			ResourceLocation structureId = structureRegistry.getKey(structure);
			if (structureId == null) {
				continue;
			}

			String structureName = structureId.toString();
			Component message = Component.literal("§7- §a" + structureName)
					.withStyle((style) -> {
						return style.withClickEvent(new ClickEvent(
										ClickEvent.Action.COPY_TO_CLIPBOARD,
										structureName
								))
								.withHoverEvent(new HoverEvent(
										HoverEvent.Action.SHOW_TEXT,
										Component.literal("Structure ID(Click to Copy)")
								));
					});

			player.sendSystemMessage(Component.literal("Locate Structure:"));
			player.sendSystemMessage(message);
		}
	}
}