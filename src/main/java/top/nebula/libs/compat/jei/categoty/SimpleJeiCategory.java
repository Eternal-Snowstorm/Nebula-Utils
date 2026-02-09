package top.nebula.libs.compat.jei.categoty;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.NotNull;
import top.nebula.libs.compat.jei.function.IDrawHandler;
import top.nebula.libs.compat.jei.function.ITooltipHandler;

import java.util.List;
import java.util.function.Supplier;

public class SimpleJeiCategory<T> implements IRecipeCategory<T> {
	public static <T> Builder<T> builder(RecipeType<T> type) {
		return new Builder<>(type);
	}

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
	private final IDrawHandler<T> drawHandler;
	private final ITooltipHandler<T> tooltipHandler;

	private SimpleJeiCategory(Builder<T> builder) {
		this.recipeType = builder.recipeType;
		this.title = builder.title;
		this.background = builder.background;
		this.iconSupplier = builder.iconSupplier;
		this.width = builder.width;
		this.height = builder.height;
		this.recipeHandler = builder.recipeHandler;
		this.drawHandler = builder.drawHandler;
		this.tooltipHandler = builder.tooltipHandler;
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
	public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull T recipe, @NotNull IFocusGroup group) {
		if (recipeHandler != null) {
			recipeHandler.accept(builder, recipe, group);
		}
	}

	@Override
	public void draw(@NotNull T recipe, @NotNull IRecipeSlotsView view, @NotNull GuiGraphics graphics, double mouseX, double mouseY) {
		if (drawHandler != null) {
			drawHandler.draw(recipe, view, graphics, mouseX, mouseY);
		}
	}

	@Override
	public @NotNull List<Component> getTooltipStrings(@NotNull T recipe, @NotNull IRecipeSlotsView view, double mouseX, double mouseY) {
		if (tooltipHandler != null) {
			return tooltipHandler.getTooltips(recipe, view, mouseX, mouseY);
		}
		return List.of();
	}

	public static class Builder<T> {
		private final RecipeType<T> recipeType;
		private final IGuiHelper helper;

		private Component title = Component.empty();
		private IDrawable background;
		private Supplier<IDrawable> iconSupplier;

		private int width = 0;
		private int height = 0;

		private Integer backgroundWidth;
		private Integer backgroundHeight;

		private TriConsumer<IRecipeLayoutBuilder, T, IFocusGroup> recipeHandler;
		private IDrawHandler<T> drawHandler;
		private ITooltipHandler<T> tooltipHandler;

		private Builder(RecipeType<T> type) {
			this.recipeType = type;
			this.helper = null;
		}

		private Builder(RecipeType<T> type, IGuiHelper helper) {
			this.recipeType = type;
			this.helper = helper;
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

		public Builder<T> setBackground(int width, int height) {
			this.backgroundWidth = width;
			this.backgroundHeight = height;
			return this;
		}

		public Builder<T> setIcon(Supplier<IDrawable> iconSupplier) {
			this.iconSupplier = iconSupplier;
			return this;
		}

		public Builder<T> setIcon(ItemStack stack) {
			if (helper == null) {
				String exceptionMessage = "setIcon(ItemStack) requires builder(type, IGuiHelper)";
				throw new IllegalStateException(exceptionMessage);
			}
			this.iconSupplier = () -> {
				return helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, stack);
			};
			return this;
		}

		public Builder<T> setRecipe(TriConsumer<IRecipeLayoutBuilder, T, IFocusGroup> handler) {
			this.recipeHandler = handler;
			return this;
		}

		public Builder<T> setDraw(IDrawHandler<T> handler) {
			this.drawHandler = handler;
			return this;
		}

		public Builder<T> setTooltips(ITooltipHandler<T> handler) {
			this.tooltipHandler = handler;
			return this;
		}

		public SimpleJeiCategory<T> build() {
			if (width <= 0 || height <= 0) {
				String exceptionMessage = "JEI Category requires setSize(width, height) with values > 0";
				throw new IllegalStateException(exceptionMessage);
			}

			if (background == null && helper != null) {
				int bgW = backgroundWidth != null && backgroundWidth > 0
						? backgroundWidth
						: width;

				int bgH = backgroundHeight != null && backgroundHeight > 0
						? backgroundHeight
						: height;

				background = helper.createBlankDrawable(bgW, bgH);
			}

			if (iconSupplier == null && helper != null) {
				iconSupplier = () -> {
					return helper.createBlankDrawable(16, 16);
				};
			}

			return new SimpleJeiCategory<>(this);
		}
	}
}