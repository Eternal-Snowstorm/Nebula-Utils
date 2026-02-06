package top.nebula.libs.compat.jei.function;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.client.gui.GuiGraphics;

@FunctionalInterface
public interface DrawHandler<T> {
	void draw(T recipe, IRecipeSlotsView view, GuiGraphics graphics, double mouseX, double mouseY);

	static <T> DrawHandler<T> empty() {
		return (r, view, graphics, x, y) -> {
		};
	}
}