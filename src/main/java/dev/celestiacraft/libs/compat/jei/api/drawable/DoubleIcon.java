package dev.celestiacraft.libs.compat.jei.api.drawable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class DoubleIcon implements IDrawable {
	private final @Nullable Supplier<ItemStack> primaryItemSupplier;
	private final @Nullable Supplier<ItemStack> secondaryItemSupplier;
	private final @Nullable Supplier<ResourceLocation> primaryTextureSupplier;
	private final @Nullable Supplier<ResourceLocation> secondaryTextureSupplier;
	private final int primaryU;
	private final int primaryV;
	private final int secondaryU;
	private final int secondaryV;
	private final int width;
	private final int height;

	private ItemStack primaryItem;
	private ItemStack secondaryItem;
	private ResourceLocation primaryTexture;
	private ResourceLocation secondaryTexture;

	private DoubleIcon(@Nullable Supplier<ItemStack> primary, @Nullable Supplier<ItemStack> secondary) {
		primaryItemSupplier = primary;
		secondaryItemSupplier = secondary;
		primaryTextureSupplier = null;
		secondaryTextureSupplier = null;
		primaryU = primaryV = secondaryU = secondaryV = width = height = 0;
	}

	private DoubleIcon(
			@Nullable Supplier<ResourceLocation> primary,
			int primaryU,
			int primaryV,
			@Nullable Supplier<ResourceLocation> secondary,
			int secondaryU,
			int secondaryV,
			int width, int height
	) {
		primaryItemSupplier = null;
		secondaryItemSupplier = null;
		primaryTextureSupplier = primary;
		secondaryTextureSupplier = secondary;
		this.primaryU = primaryU;
		this.primaryV = primaryV;
		this.secondaryU = secondaryU;
		this.secondaryV = secondaryV;
		this.width = width;
		this.height = height;
	}

	public static DoubleIcon ofItem(Supplier<ItemStack> primary, Supplier<ItemStack> secondary) {
		return new DoubleIcon(primary, secondary);
	}

	public static DoubleIcon ofTextures(
			ResourceLocation primary,
			int primaryU, int primaryV,
			ResourceLocation secondary,
			int secondaryU,
			int secondaryV,
			int width,
			int height
	) {
		return new DoubleIcon(() -> primary, primaryU, primaryV, () -> secondary, secondaryU, secondaryV, width, height);
	}

	public static DoubleIcon ofTextures(
			Supplier<ResourceLocation> primary,
			int primaryU,
			int primaryV,
			Supplier<ResourceLocation> secondary,
			int secondaryU,
			int secondaryV,
			int width,
			int height
	) {
		return new DoubleIcon(primary, primaryU, primaryV, secondary, secondaryU, secondaryV, width, height);
	}

	@Override
	public int getWidth() {
		return 18;
	}

	@Override
	public int getHeight() {
		return 18;
	}

	@Override
	public void draw(@NotNull GuiGraphics graphics, int xOffset, int yOffset) {
		if (primaryTextureSupplier != null) {
			if (primaryTexture == null) {
				primaryTexture = primaryTextureSupplier.get();
			}
			if (secondaryTexture == null) {
				secondaryTexture = secondaryTextureSupplier.get();
			}
		} else {
			if (primaryItem == null) {
				primaryItem = primaryItemSupplier.get();
			}
			if (secondaryItem == null) {
				secondaryItem = secondaryItemSupplier.get();
			}
		}

		RenderSystem.enableDepthTest();
		PoseStack pose = graphics.pose();
		pose.pushPose();
		pose.translate(xOffset, yOffset, 0);

		pose.pushPose();
		pose.translate(1, 1, 0);
		renderPrimary(graphics);
		pose.popPose();

		pose.pushPose();
		pose.translate(10, 10, 100);
		pose.scale(0.5f, 0.5f, 0.5f);
		renderSecondary(graphics);
		pose.popPose();

		pose.popPose();
	}

	private void renderPrimary(GuiGraphics graphics) {
		if (primaryTextureSupplier != null) {
			graphics.blit(
					primaryTexture,
					0,
					0,
					primaryU,
					primaryV,
					width,
					height
			);
		} else {
			graphics.renderItem(primaryItem, 0, 0);
		}
	}

	private void renderSecondary(GuiGraphics graphics) {
		if (secondaryTextureSupplier != null) {
			graphics.blit(
					secondaryTexture,
					0,
					0,
					secondaryU,
					secondaryV,
					width,
					height
			);
		} else {
			graphics.renderItem(secondaryItem, 0, 0);
		}
	}
}