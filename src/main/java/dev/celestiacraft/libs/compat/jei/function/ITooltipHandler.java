package dev.celestiacraft.libs.compat.jei.function;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;

@FunctionalInterface
public interface ITooltipHandler<T> {
	void getTooltips(ITooltipBuilder tooltip, T recipe, IRecipeSlotsView view, double mouseX, double mouseY);
}