package dev.celestiacraft.libs.api.tab.creativetab;

import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.api.tab.FTSInternal;
import dev.celestiacraft.libs.api.tab.section.ISection;
import dev.celestiacraft.libs.config.common.FTSConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 创造模式物品栏界面左边缘的可折叠跳转列表,
 * 为当前选中标签页的每个已注册 {@link ISection} 显示一个图标.点击
 * 图标会平滑滚动网格, 使该分区的横幅停靠在顶部.
 *
 * @since 6.0
 */
public final class IndexPanel {
	private IndexPanel() {
	}

	private static final int ICON = 18;
	private static final int GAP = 2;
	private static final int PANEL_W = 24;

	private static final ResourceLocation TOGGLE_COLLAPSED = NebulaLibs.loadResource("textures/gui/index_panel/toggle_collapsed.png");
	private static final ResourceLocation TOGGLE_EXPANDED = NebulaLibs.loadResource("textures/gui/index_panel/toggle_expanded.png");

	private static final ResourceLocation INDEX_SLOT = NebulaLibs.loadResource("textures/gui/index_panel/slot_border.png");
	private static final ResourceLocation INDEX_SLOT_BOTTOM = NebulaLibs.loadResource("textures/gui/index_panel/slot_border_bottom.png");
	private static final ResourceLocation INDEX_SLOT_TOP = NebulaLibs.loadResource("textures/gui/index_panel/slot_border_top.png");

	private static final int TOGGLE_LEFT = 6;
	private static final int TOGGLE_SIZE = 8;
	private static final int TOGGLE_TEXT_GAP = 3;
	private static final int TOGGLE_HIT_PAD = 3;

	private static final int HOVER = 0x60FFFFFF;

	private static final long ANIM_MS = 250;

	private static float panelScroll = 0f;

	private static long animStart = -1;
	private static float animFrom;
	private static float animTo;

	public static boolean active(List<ISection<?>> sections) {
		return sections != null && !sections.isEmpty();
	}

	private static int toggleX(CreativeModeInventoryScreen screen) {
		return screen.getGuiLeft() + TOGGLE_LEFT;
	}

	private static int toggleY(CreativeModeInventoryScreen screen) {
		return screen.getGuiTop() + 6;
	}

	/**
	 * @return 当本面板的折叠开关显示时, 标签页标题为了避免与它重叠而需要偏移的 X 量
	 * (相对于 guiLeft).通过挂钩 renderLabels 的 mixin 生效.
	 */
	public static int titleX() {
		return TOGGLE_LEFT + TOGGLE_SIZE + TOGGLE_TEXT_GAP;
	}

	private static int panelX(CreativeModeInventoryScreen screen) {
		return screen.getGuiLeft() - 2 - PANEL_W;
	}

	private static int panelY(CreativeModeInventoryScreen screen) {
		return screen.getGuiTop() + 4;
	}

	private static int visibleRows(CreativeModeInventoryScreen screen, int count) {
		int maxH = screen.getYSize() - 8;
		return Math.min(count, Math.max(0, (maxH - 6 + GAP) / (ICON + GAP)));
	}

	private static int panelHeight(CreativeModeInventoryScreen screen, int count) {
		int rows = visibleRows(screen, count);
		return rows <= 0 ? 0 : rows * (ICON + GAP) - GAP + 6;
	}

	private static float maxScroll(CreativeModeInventoryScreen screen, int count) {
		int contentH = count * (ICON + GAP) - GAP + 6;
		return Math.max(0, contentH - panelHeight(screen, count));
	}

	/**
	 * 推进进行中的跳转至分区动画.
	 *
	 * @return 本帧需要应用的滚动比例(0..1), 若无内容需要应用则返回 -1
	 */
	public static float tick() {
		if (animStart < 0) return -1f;

		long elapsed = System.currentTimeMillis() - animStart;
		float t = Mth.clamp(elapsed / (float) ANIM_MS, 0f, 1f);
		float inv = 1f - t;
		float eased = 1f - inv * inv * inv;
		float value = Mth.lerp(eased, animFrom, animTo);

		if (t >= 1f) animStart = -1;
		return value;
	}

	private static void cancelAnim() {
		animStart = -1;
	}

	private static void jumpTo(CreativeModeInventoryScreen screen, ISection<?> section, float currentScrollOffs) {
		int row = FTSInternal.getRowForSection(section);
		if (row == -1) return;

		int itemCount = screen.getMenu().items.size();
		int rows = Mth.positiveCeilDiv(itemCount, BannerRenderer.GRID_COLS) - BannerRenderer.VISIBLE_ROWS;
		if (rows <= 0) return;

		animFrom = currentScrollOffs;
		animTo = Mth.clamp(row / (float) rows, 0f, 1f);
		animStart = System.currentTimeMillis();
	}

	public static void render(CreativeModeInventoryScreen screen, GuiGraphics g, List<ISection<?>> sections, int mouseX, int mouseY) {
		drawToggle(screen, g, mouseX, mouseY);

		if (!FTSConfig.INDEX_EXPANDED.get() || sections.isEmpty()) return;

		int count = sections.size();
		panelScroll = Mth.clamp(panelScroll, 0f, maxScroll(screen, count));

		int px = panelX(screen);
		int py = panelY(screen);
		int h = panelHeight(screen, count);
		if (h <= 0) return;
		int pBottom = py + h;

		Component hoveredTitle = null;

		int slotHeight = ICON + GAP;
		int start = Math.max(0, (int) panelScroll / slotHeight);
		int visible = Math.min(6, count - start);

		for (int i = 0; i < visible; i++) {
			int sectionIndex = start + i;

			int ix = px + (PANEL_W - ICON) / 2;

			// 让 6 个槽位固定在原位
			int iy = py + 3 + i * slotHeight;

			boolean hovered = mouseX >= ix && mouseX < ix + ICON && mouseY >= iy && mouseY < iy + ICON && mouseY >= py && mouseY < pBottom;

			ResourceLocation rl;

			if (i == 0) {
				rl = INDEX_SLOT_TOP;
			} else if (i == visible - 1) {
				rl = INDEX_SLOT_BOTTOM;
			} else {
				rl = INDEX_SLOT;
			}

			g.blit(rl, ix - 3, iy - 3, 24, 24, 24, 24, 24, 24, 24, 24);

			if (hovered) {
				g.fill(
						ix + 1,
						iy + 1,
						ix + ICON - 1,
						iy + ICON - 1,
						HOVER
				);
			}

			ISection<?> section = sections.get(sectionIndex);
			ItemStack icon = section.icon();

			g.renderItem(icon, ix + 1, iy + 1);
			g.renderItemDecorations(
					Minecraft.getInstance().font,
					icon,
					ix + 1,
					iy + 1
			);

			Component title = section.getTitle();

			if (hovered) {
				hoveredTitle = title == null
						? Component.literal(section.id().toString())
						: title;
			}
		}

		if (hoveredTitle != null) {
			g.renderTooltip(Minecraft.getInstance().font, hoveredTitle, mouseX, mouseY);
		}
	}

	public static boolean mouseClicked(CreativeModeInventoryScreen screen, List<ISection<?>> sections, float scrollOffs, double mouseX, double mouseY, int button) {
		if (button != 0) return false;

		int tx = toggleX(screen);
		int ty = toggleY(screen);
		if (mouseX >= tx - TOGGLE_HIT_PAD && mouseX < tx + TOGGLE_SIZE + TOGGLE_HIT_PAD
				&& mouseY >= ty - TOGGLE_HIT_PAD && mouseY < ty + TOGGLE_SIZE + TOGGLE_HIT_PAD) {
			FTSConfig.INDEX_EXPANDED.set(!FTSConfig.INDEX_EXPANDED.get());
			FTSConfig.INDEX_EXPANDED.save();
			panelScroll = 0f;
			return true;
		}

		if (!FTSConfig.INDEX_EXPANDED.get() || sections.isEmpty()) {
			cancelAnim();
			return false;
		}

		int count = sections.size();
		int px = panelX(screen);
		int py = panelY(screen);
		int h = panelHeight(screen, count);
		if (h <= 0 || mouseX < px || mouseX >= px + PANEL_W || mouseY < py || mouseY >= py + h) {
			cancelAnim();
			return false;
		}

		int i = (int) ((mouseY - (py + 3) + panelScroll) / (ICON + GAP));
		if (i < 0 || i >= count) return true;

		int ix = px + (PANEL_W - ICON) / 2;
		int iy = py + 3 + i * (ICON + GAP) - (int) panelScroll;
		if (mouseX < ix || mouseX >= ix + ICON || mouseY < iy || mouseY >= iy + ICON) return true;

		jumpTo(screen, sections.get(i), scrollOffs);
		return true;
	}

	public static boolean mouseScrolled(CreativeModeInventoryScreen screen, List<ISection<?>> sections, double mouseX, double mouseY, double scrollY) {
		if (!FTSConfig.INDEX_EXPANDED.get() || sections.isEmpty()) {
			cancelAnim();
			return false;
		}

		int count = sections.size();
		int px = panelX(screen);
		int py = panelY(screen);
		int h = panelHeight(screen, count);
		if (h <= 0 || mouseX < px || mouseX >= px + PANEL_W || mouseY < py || mouseY >= py + h) {
			cancelAnim();
			return false;
		}

		panelScroll = Mth.clamp((float) (panelScroll - scrollY * 20f), 0f, maxScroll(screen, count));
		return true;
	}

	private static void drawToggle(CreativeModeInventoryScreen screen, GuiGraphics g, int mouseX, int mouseY) {
		int tx = toggleX(screen);
		int ty = toggleY(screen);
		boolean hovered = mouseX >= tx && mouseX < tx + TOGGLE_SIZE && mouseY >= ty && mouseY < ty + TOGGLE_SIZE;

		ResourceLocation texture = FTSConfig.INDEX_EXPANDED.get() ? TOGGLE_EXPANDED : TOGGLE_COLLAPSED;
		g.blit(texture, tx, ty, 0, 0, TOGGLE_SIZE, TOGGLE_SIZE, TOGGLE_SIZE, TOGGLE_SIZE);
		if (hovered) {
			g.fill(tx, ty, tx + TOGGLE_SIZE, ty + TOGGLE_SIZE, HOVER);
		}
	}
}
