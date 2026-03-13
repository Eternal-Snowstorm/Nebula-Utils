package dev.celestiacraft.libs.compat.curios;

import dev.celestiacraft.libs.NebulaLibs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NebulaLibs.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CuriosTicksHandler {
	@SubscribeEvent
	public static void onPlayerTick(LivingEvent.LivingTickEvent event) {
		LivingEntity entity = event.getEntity();
		Level level = entity.level();

		if (level.isClientSide()) {
			return;
		}
		if (!(entity instanceof Player player)) {
			return;
		}

		// 获取所有Curios物品
		for (ItemStack stack : CuriosUtils.getAllItems(player)) {
			Item item = stack.getItem();

			if (!(item instanceof CuriosUtils tickable)) {
				continue;
			}

			if (player.tickCount % tickable.tickCheck() != 0) {
				continue;
			}

			tickable.curiosTick(new CuriosContext(player, level, stack));
		}
	}
}