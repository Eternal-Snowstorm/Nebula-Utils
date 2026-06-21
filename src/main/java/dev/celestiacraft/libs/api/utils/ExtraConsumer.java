package dev.celestiacraft.libs.api.utils;

import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.IFocusGroup;

@FunctionalInterface
public interface ExtraConsumer<T> {
	void accept(IRecipeExtrasBuilder builder, T recipe, IFocusGroup focuses);
}