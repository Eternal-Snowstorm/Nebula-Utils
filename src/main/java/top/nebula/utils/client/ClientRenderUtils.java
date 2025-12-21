package top.nebula.utils.client;

public class ClientRenderUtils {
	/**
	 * @param iconX 图标位置(X)
	 * @param iconY 图标位置(Y)
	 * @param iconWidth 图标宽度
	 * @param iconHeight 图标高度
	 * @param cursorX 光标
	 * @param cursorY 光标
	 * @return
	 */
	public static boolean isCursorInsideBounds(int iconX, int iconY, int iconWidth, int iconHeight, double cursorX, double cursorY) {
		return iconX <= cursorX && cursorX < iconX + iconWidth && iconY <= cursorY && cursorY < iconY + iconHeight;
	}
}