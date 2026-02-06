package top.nebula.libs.compat.jei.function;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.network.chat.Component;

import java.util.List;

@FunctionalInterface
public interface TooltipHandler<T> {
	List<Component> getTooltips(T recipe, IRecipeSlotsView view, double mouseX, double mouseY);
}