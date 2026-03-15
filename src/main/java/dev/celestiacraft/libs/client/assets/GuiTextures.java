package dev.celestiacraft.libs.client.assets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.element.ScreenElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public record GuiTextures(
		ResourceLocation location,
		int startX,
		int startY,
		int width,
		int height
) implements ScreenElement {
	// namespace 构造
	public GuiTextures(String namespace, String path, int startX, int startY, int width, int height) {
		this(getResource(namespace, path), startX, startY, width, height);
	}

	// 简化构造
	public GuiTextures(String namespace, String path, int width, int height) {
		this(namespace, path, 0, 0, width, height);
	}

	private static ResourceLocation getResource(String namespace, String path) {
		return ResourceLocation.fromNamespaceAndPath(
				namespace,
				String.format("textures/gui/%s.png", path)
		);
	}

	@OnlyIn(Dist.CLIENT)
	public void bind() {
		RenderSystem.setShaderTexture(0, location);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void render(GuiGraphics graphics, int x, int y) {
		graphics.blit(location, x, y, startX, startY, width, height);
	}
}