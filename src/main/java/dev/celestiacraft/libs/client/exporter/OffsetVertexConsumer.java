package dev.celestiacraft.libs.client.exporter;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.NotNull;

public class OffsetVertexConsumer implements VertexConsumer {
	private final VertexConsumer delegate;
	private final double offsetX;
	private final double offsetY;
	private final double offsetZ;

	public OffsetVertexConsumer(VertexConsumer delegate, double offsetX, double offsetY, double offsetZ) {
		this.delegate = delegate;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.offsetZ = offsetZ;
	}

	@Override
	public @NotNull VertexConsumer vertex(double x, double y, double z) {
		return delegate.vertex(x + offsetX, y + offsetY, z + offsetZ);
	}

	@Override
	public @NotNull VertexConsumer color(int red, int green, int blue, int alpha) {
		return delegate.color(red, green, blue, alpha);
	}

	@Override
	public @NotNull VertexConsumer uv(float u, float v) {
		return delegate.uv(u, v);
	}

	@Override
	public @NotNull VertexConsumer overlayCoords(int u, int v) {
		return delegate.overlayCoords(u, v);
	}

	@Override
	public @NotNull VertexConsumer uv2(int u, int v) {
		return delegate.uv2(u, v);
	}

	@Override
	public @NotNull VertexConsumer normal(float x, float y, float z) {
		return delegate.normal(x, y, z);
	}

	@Override
	public void endVertex() {
		delegate.endVertex();
	}

	@Override
	public void defaultColor(int red, int green, int blue, int alpha) {
		delegate.defaultColor(red, green, blue, alpha);
	}

	@Override
	public void unsetDefaultColor() {
		delegate.unsetDefaultColor();
	}
}
