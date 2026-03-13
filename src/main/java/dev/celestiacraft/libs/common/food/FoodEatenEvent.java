package dev.celestiacraft.libs.common.food;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record FoodEatenEvent(Player player, ItemStack item, Level level) {
}