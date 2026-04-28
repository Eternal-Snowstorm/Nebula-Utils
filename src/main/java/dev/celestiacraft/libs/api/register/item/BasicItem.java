package dev.celestiacraft.libs.api.register.item;

import dev.celestiacraft.libs.api.client.tooltip.TooltipContext;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BasicItem extends Item {
	public BasicItem(Properties properties) {
		super(properties);
	}

	@NotNull
	public ResourceLocation getRegistryName() {
		ResourceLocation key = ForgeRegistries.ITEMS.getKey(this);
		return key != ForgeRegistries.ITEMS.getDefaultKey() ? key : null;
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
		Player player = Minecraft.getInstance().player;

		addTooltips(new TooltipContext(stack, level, tooltip, flag, player));
	}

	public void addTooltips(TooltipContext context) {
	}
}