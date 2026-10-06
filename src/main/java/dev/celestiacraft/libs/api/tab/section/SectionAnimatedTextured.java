package dev.celestiacraft.libs.api.tab.section;

import dev.celestiacraft.libs.api.tab.creativetab.ConglomerateOfItems;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * 使用序列帧纹理横幅渲染的分区，按固定帧间隔循环播放动画。
 *
 * @since 3.0
 */
public class SectionAnimatedTextured extends SectionTextured {
	int frames = 0;
	float frameTimeInMS = 200;

	public SectionAnimatedTextured(ResourceLocation id) {
		super(id);
	}

	@Override
	public void render(GuiGraphics guiGraphics, Font font, int topLeftX, int topLeftY) {
		long currentTimeMs = System.currentTimeMillis();
		int currentFrame = (int) ((currentTimeMs / (long) frameTimeInMS) % frames);

		guiGraphics.blit(
				texture, topLeftX, topLeftY,
				horizontalSize, verticalSize,
				0, currentFrame * verticalSize,
				horizontalSize, verticalSize,
				horizontalSize, verticalSize * frames
		);

		renderTitle(guiGraphics, font, topLeftX, topLeftY);
	}

	/**
	 * 使用该构建器时，纹理必须放置在 [namespace]:textures/gui/fancy_tab_section/[path].png
	 *
	 * @since 3.0
	 */
	public static SectionAnimatedTextured of(ResourceLocation id, Component title, int frames, float frameTimeInMS, int textColor, boolean textShadow, boolean collapsible, ConglomerateOfItems items) {
		SectionAnimatedTextured sat = new SectionAnimatedTextured(id);

		sat.setTitle(title);
		sat.setTextColor(textColor);
		sat.setTextShadow(textShadow);
		sat.setCollapsible(collapsible);
		sat.setItems(items);

		sat.setFrames(frames);
		sat.setFrameTimeInMS(frameTimeInMS);

		return sat;
	}

	public SectionAnimatedTextured setFrames(int frames) {
		this.frames = frames;
		return this;
	}

	public SectionAnimatedTextured setFrameTimeInMS(float frameTimeInMS) {
		this.frameTimeInMS = frameTimeInMS;
		return this;
	}
}