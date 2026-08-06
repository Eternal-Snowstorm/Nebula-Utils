package dev.celestiacraft.libs.compat.jei.api.ingredient;

import dev.celestiacraft.libs.api.recipe.ingredient.item.IngredientWithCount;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class IngredientWithCountRenderer implements IIngredientRenderer<IngredientWithCount> {
	@Override
	public void render(@NotNull GuiGraphics graphics, @NotNull IngredientWithCount ingredient) {
		ItemStack stack = JeiIngredientTypes.firstStack(ingredient);
		if (stack.isEmpty()) {
			return;
		}
		Minecraft minecraft = Minecraft.getInstance();
		graphics.renderFakeItem(stack, 0, 0);
		graphics.renderItemDecorations(minecraft.font, stack, 0, 0);
	}

	@SuppressWarnings("removal")
	@Override
	public @NotNull List<Component> getTooltip(@NotNull IngredientWithCount ingredient, @NotNull TooltipFlag flag) {
		ItemStack stack = JeiIngredientTypes.firstStack(ingredient);
		if (stack.isEmpty()) {
			return List.of();
		}
		Player player = Minecraft.getInstance().player;
		return stack.getTooltipLines(player, flag);
	}
}