package top.nebula.libs.compat.jei.categoty;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.NotNull;
import top.nebula.libs.compat.jei.function.DrawHandler;
import top.nebula.libs.compat.jei.function.TooltipHandler;

import java.util.function.Supplier;

public class SimpleJeiCategory<T> implements IRecipeCategory<T> {
	public static <T> Builder<T> builder(RecipeType<T> type, IGuiHelper helper) {
		return new Builder<>(type, helper);
	}

	private final RecipeType<T> recipeType;
	private final Component title;
	private final IDrawable background;
	private final Supplier<IDrawable> iconSupplier;
	private final int width;
	private final int height;
	private final TriConsumer<IRecipeLayoutBuilder, T, IFocusGroup> recipeHandler;
	private final DrawHandler<T> drawHandler;

	public SimpleJeiCategory(Builder<T> builder) {

		this.recipeType = builder.recipeType;
		this.title = builder.title;
		this.background = builder.background;
		this.iconSupplier = builder.iconSupplier;
		this.width = builder.width;
		this.height = builder.height;
		this.recipeHandler = builder.recipeHandler;
		this.drawHandler = builder.drawHandler;
	}

	@Override
	public @NotNull RecipeType<T> getRecipeType() {
		return recipeType;
	}

	@Override
	public @NotNull Component getTitle() {
		return title;
	}

	@Override
	public @NotNull IDrawable getBackground() {
		return background;
	}

	@Override
	public @NotNull IDrawable getIcon() {
		return iconSupplier.get();
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull T recipe, @NotNull IFocusGroup focuses) {
		if (recipeHandler != null) {
			recipeHandler.accept(builder, recipe, focuses);
		}
	}

	@Override
	public void draw(@NotNull T recipe, @NotNull IRecipeSlotsView view, @NotNull GuiGraphics graphics, double mouseX, double mouseY) {
		if (drawHandler != null)
			drawHandler.draw(recipe, view, graphics, mouseX, mouseY);
	}

	public static class Builder<T> {
		private final RecipeType<T> recipeType;
		private Component title = Component.empty();
		private IDrawable background;
		private Supplier<IDrawable> iconSupplier;

		private int width = 0;
		private int height = 0;

		private TriConsumer<IRecipeLayoutBuilder, T, IFocusGroup> recipeHandler;
		private DrawHandler<T> drawHandler;

		private Builder(RecipeType<T> type, IGuiHelper helper) {
			this.recipeType = type;
			this.background = helper.createBlankDrawable(0, 0);
		}

		public Builder<T> setTitle(Component title) {
			this.title = title;
			return this;
		}

		public Builder<T> setSize(int width, int height) {
			this.width = width;
			this.height = height;
			return this;
		}

		public Builder<T> setBackground(IDrawable background) {
			this.background = background;
			return this;
		}

		public Builder<T> setIcon(Supplier<IDrawable> iconSupplier) {
			this.iconSupplier = iconSupplier;
			return this;
		}

		public Builder<T> setTooltips(TooltipHandler<T> handler) {
			return this;
		}

		public Builder<T> setRecipe(TriConsumer<IRecipeLayoutBuilder, T, IFocusGroup> handler) {
			this.recipeHandler = handler;
			return this;
		}

		public Builder<T> setDraw(DrawHandler<T> handler) {
			this.drawHandler = handler;
			return this;
		}

		public SimpleJeiCategory<T> build() {
			return new SimpleJeiCategory<>(this);
		}
	}
}