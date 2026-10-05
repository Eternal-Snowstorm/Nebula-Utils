package dev.celestiacraft.libs.api.tab.creativetab;

import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.api.tab.FTSInternal;
import dev.celestiacraft.libs.api.tab.FancyTabSections;
import dev.celestiacraft.libs.api.tab.section.Section;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * 创造模式物品栏界面左侧分区横幅(banner)的渲染器
 *
 * <p>
 * 按当前滚动行偏移绘制各分区的横幅与折叠按钮，并提供鼠标是否落在折叠按钮、横幅上的命中检测
 * </p>
 *
 * <p>
 * 本类属于内部 API，其中的方法不保证向后兼容，不建议在外部直接调用。
 * </p>
 */
public class BannerRenderer {
	public static int CURRENT_ROW = 0;

	public static ResourceLocation CURRENT_TAB = ResourceLocation.withDefaultNamespace("none");

	public static final int ROW_HEIGHT = 18;
	public static final int GRID_COLS = 9;
	public static final int GRID_X_OFFSET = 10;
	public static final int GRID_Y_OFFSET = 17;


	public static final int VISIBLE_ROWS = 5;
	public static final int BANNER_WIDTH = GRID_COLS * ROW_HEIGHT - 4;

	public static final ResourceLocation EXPANDED_BUTTON = NebulaLibs.loadResource("textures/gui/collapse_button/expanded_button.png");
	public static final ResourceLocation EXPANDED_BUTTON_HIGHLIGHT = NebulaLibs.loadResource("textures/gui/collapse_button/expanded_button_highlight.png");
	public static final ResourceLocation COLLAPSED_BUTTON = NebulaLibs.loadResource("textures/gui/collapse_button/collapsed_button.png");
	public static final ResourceLocation COLLAPSED_BUTTON_HIGHLIGHT = NebulaLibs.loadResource("textures/gui/collapse_button/collapsed_button_highlight.png");

	public static void render(CreativeModeInventoryScreen screen, GuiGraphics guiGraphics, List<Section<?>> sections, int mouseX, int mouseY) {
		int topLeftX = screen.getGuiLeft() + 8;
		int top = screen.getGuiTop() + 17;

		Font font = Minecraft.getInstance().font;

		for (Section<?> section : sections) {
			int sectionRow = FTSInternal.getRowForSection(section);
			if (sectionRow == -1) continue;

			int relativeRow = sectionRow - CURRENT_ROW;
			if (relativeRow < 0 || relativeRow >= 5) continue;

			int topLeftY = top + relativeRow * 18;

			section.render(guiGraphics, font, topLeftX, topLeftY);

			boolean isHoveringAny = sections.stream().anyMatch(o -> BannerRenderer.isInToggle(screen, o, mouseX, mouseY));
			section.renderToggle(screen, guiGraphics, section, topLeftX, topLeftY, BANNER_WIDTH, 18, mouseX, mouseY, isHoveringAny);
		}
	}

	/**
	 * @return 给定的屏幕坐标是否落在可折叠 {@link Section} 的(可见的)折叠控件内
	 */
	public static boolean isInToggle(CreativeModeInventoryScreen screen, Section section, double mouseX, double mouseY) {
		if (!section.collapsible()) return false;

		int sectionRow = FTSInternal.getRowForSection(section);
		if (sectionRow == -1) return false;

		int relativeRow = sectionRow - CURRENT_ROW;
		if (relativeRow < 0 || relativeRow >= VISIBLE_ROWS) return false;

		int left = screen.getGuiLeft() + GRID_X_OFFSET;
		int top = screen.getGuiTop() + GRID_Y_OFFSET;
		int y = top + relativeRow * ROW_HEIGHT;
		int h = ROW_HEIGHT - 1;

		int tx1 = left + BANNER_WIDTH + 1;
		int tx0 = tx1 - ROW_HEIGHT;

		return mouseX >= tx0 && mouseX < tx1 && mouseY >= y && mouseY < y + h;
	}

	/**
	 * @return 给定的屏幕坐标是否落在某个横幅内
	 *
	 * @since 5.0
	 */
	public static boolean isInBanner(CreativeModeInventoryScreen screen, double mouseX, double mouseY) {
		List<Section<?>> list = FancyTabSections.REGISTERED_TABS.getOrDefault(BannerRenderer.CURRENT_TAB, List.of());

		for (Section<?> section : list) {
			int sectionRow = FTSInternal.getRowForSection(section);
			if (sectionRow == -1) continue;

			int relativeRow = sectionRow - CURRENT_ROW;
			if (relativeRow < 0 || relativeRow >= VISIBLE_ROWS) continue;

			int left = screen.getGuiLeft() + GRID_X_OFFSET;
			int top = screen.getGuiTop() + GRID_Y_OFFSET;
			int y = top + relativeRow * ROW_HEIGHT;
			int h = ROW_HEIGHT - 1;

			int tx0 = left - 2;
			int tx1 = left + BANNER_WIDTH + 1;

			if (mouseX >= tx0 && mouseX < tx1 && mouseY >= y && mouseY < y + h) {
				return true;
			}
		}
		return false;
	}
}
