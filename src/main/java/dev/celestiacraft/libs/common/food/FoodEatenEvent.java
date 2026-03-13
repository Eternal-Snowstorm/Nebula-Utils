package dev.celestiacraft.libs.common.food;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.logging.Level;

public record FoodEatenEvent(Player player, ItemStack stack, Level level) {
}