package dev.celestiacraft.libs.compat.jei.categoty;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
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
import dev.celestiacraft.libs.compat.jei.function.IDrawHandler;
import dev.celestiacraft.libs.compat.jei.function.ITooltipHandler;

import java.util.function.Supplier;

/**
 * <p>
 * SimpleJeiCategory 是一个通用的 JEI IRecipeCategory 实现,
 * 通过 Builder 模式封装分类的完整构建流程,
 * 用于统一模组内所有 JEI 分类的注册结构.
 * </p>
 *
 * <p>
 * 在原生 JEI 使用方式中,
 * 每个分类都需要手动实现 IRecipeCategory,
 * 并重复编写标题, 背景, 图标, setRecipe, draw 等方法.
 * 当分类数量增加时,
 * 代码会变得分散且重复度较高.
 * </p>
 *
 * <p>
 * 本类将分类构建逻辑抽象为链式 API,
 * 通过多种重载写法提供灵活配置方式,
 * 使分类定义更加声明式,
 * 同时保持注册结构统一.
 * </p>
 *
 * <h2>完整使用流程</h2>
 *
 * <h3>1. 在 @JeiPlugin 主类中注册分类</h3>
 *
 * <pre>{@code
 * @JeiPlugin
 * public class ModJeiPlugin implements IModPlugin {
 *     @Override
 *     public ResourceLocation getPluginUid() {
 *         return ResourceLocation.fromNamespaceAndPath("modid", "jei_plugin");
 *     }
 *
 *     @Override
 *     public void registerCategories(IRecipeCategoryRegistration registration) {
 *         IGuiHelper helper = registration.getJeiHelpers().getGuiHelper();
 *
 *         registration.addRecipeCategories(ExamplesCategory.builder(helper));
 *     }
 * }
 * }</pre>
 *
 * <h3>2. 在具体分类类中构建</h3>
 *
 * <pre>{@code
 * public static SimpleJeiCategory<ExamplesRecipe> builder(IGuiHelper helper) {
 *     return SimpleJeiCategory.builder(RECIPE_TYPE, helper)
 *         .setTitle(Component.literal("Examples"))
 *         .setSize(178, 72)
 *         .setIcon(() -> iconSupplier)
 *         .setRecipe((builder, recipe, group) -> {
 *             // 添加输入输出槽位
 *         })
 *         .setDraw((recipe, view, graphics, mouseX, mouseY) -> {
 *             // 额外渲染逻辑
 *         })
 *         .build();
 * }
 * }</pre>
 *
 * <h2>Builder 创建方式重载</h2>
 *
 * <ul>
 *     <li>
 *         builder(RecipeType<T> type)
 *         <br>
 *         不依赖 IGuiHelper.
 *         需要手动提供背景与图标.
 *     </li>
 *
 *     <li>
 *         builder(RecipeType<T> type, IGuiHelper helper)
 *         <br>
 *         推荐使用方式.
 *         支持自动创建空白背景与默认图标,
 *         并允许使用 setIcon(ItemStack) 简化写法.
 *     </li>
 * </ul>
 *
 * <h2>方法重载说明</h2>
 *
 * <ul>
 *     <li>
 *         setTitle(Component title)
 *         <br>
 *         适用于可翻译文本或复杂样式.
 *     </li>
 *
 *     <li>
 *         setTitle(String literal)
 *         <br>
 *         简化写法, 自动包装为 Component.literal().
 *     </li>
 *
 *     <li>
 *     *   setTranTitle(String tranKey)
 *         <br>
 *         简化写法, 自动包装为 Component.translatable().
 *     </li>
 *
 *     <li>
 *         setBackground(IDrawable background)
 *         <br>
 *         提供自定义背景对象, 在 draw() 中自动绘制.
 *     </li>
 *
 *     <li>
 *         setBackground(int width, int height)
 *         <br>
 *         通过 IGuiHelper 创建空白背景, 在 draw() 中自动绘制.
 *         若未调用且存在 helper,
 *         默认使用 setSize 尺寸生成背景.
 *     </li>
 *
 *     <li>
 *         setIcon(Supplier<IDrawable> supplier)
 *         <br>
 *         延迟生成图标.
 *         适用于复杂或动态 Drawable.
 *     </li>
 *
 *     <li>
 *         setIcon(ItemStack stack)
 *         <br>
 *         简化写法,
 *         通过 IGuiHelper 自动生成物品图标.
 *         仅在存在 helper 时可用.
 *     </li>
 * </ul>
 *
 * <h2>行为扩展接口</h2>
 *
 * <ul>
 *     <li>
 *         setRecipe(TriConsumer<IRecipeLayoutBuilder, T, IFocusGroup>)
 *         <br>
 *         定义配方槽位布局逻辑.
 *     </li>
 *
 *     <li>
 *         setDraw(IDrawHandler<T>)
 *         <br>
 *         自定义额外渲染逻辑.
 *     </li>
 *
 *     <li>
 *         setTooltips(ITooltipHandler<T>)
 *         <br>
 *         自定义 Tooltip 内容生成逻辑.
 *     </li>
 * </ul>
 *
 * <h2>自动行为说明</h2>
 *
 * <ul>
 *     <li>
 *         若存在 helper 且未设置背景,
 *         build() 时自动创建空白背景.
 *     </li>
 *
 *     <li>
 *         若存在 helper 且未设置图标,
 *         build() 时自动创建 16x16 空白图标.
 *     </li>
 * </ul>
 *
 * <p>
 * 本类不改变 JEI 生命周期与调用顺序,
 * 仅对 IRecipeCategory 进行结构性封装,
 * 提供多种重载写法以增强灵活性与可读性.
 * </p>
 *
 * @param <T> 配方类型
 */
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
		if (background != null) {
			background.draw(graphics);
		}
		if (drawHandler != null) {
			drawHandler.draw(recipe, view, graphics, mouseX, mouseY);
		}
	}

	@Override
	public void getTooltip(@NotNull ITooltipBuilder tooltip, @NotNull T recipe, @NotNull IRecipeSlotsView view, double mouseX, double mouseY) {
		if (tooltipHandler != null) {
			tooltipHandler.getTooltips(tooltip, recipe, view, mouseX, mouseY);
		}
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

		public Builder<T> setTitle(String title) {
			this.title = Component.literal(title);
			return this;
		}

		public Builder<T> setTranTitle(String tranKey) {
			this.title = Component.translatable(tranKey);
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