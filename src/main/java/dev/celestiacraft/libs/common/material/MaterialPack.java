package dev.celestiacraft.libs.common.material;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * <h2>MaterialPack</h2>
 *
 * <p>
 * 材料系统使用的运行时资源包.
 * </p>
 *
 * <p>
 * 该资源包完全驻留于内存, 内容由 {@link MaterialAssets} 生成, 并且是
 * <b>隐藏 + 强制启用</b> 的, 不会出现在资源包选择界面中.
 * </p>
 */
public class MaterialPack extends AbstractPackResources {
	private final PackType type;
	private final Map<ResourceLocation, byte[]> files;

	public MaterialPack(String name, PackType type, Map<ResourceLocation, byte[]> files) {
		super(name, false);
		this.type = type;
		this.files = files;
	}

	@Override
	public boolean isHidden() {
		return true;
	}

	@Nullable
	@Override
	public IoSupplier<InputStream> getRootResource(String... elements) {
		if (elements.length == 1 && PACK_META.equals(elements[0])) {
			byte[] meta = MaterialAssets.PACK_META.getBytes(StandardCharsets.UTF_8);
			return () -> new ByteArrayInputStream(meta);
		}

		return null;
	}

	@Nullable
	@Override
	public IoSupplier<InputStream> getResource(@NotNull PackType type, @NotNull ResourceLocation location) {
		if (type != this.type) {
			return null;
		}

		byte[] bytes = files.get(location);
		return bytes == null ? null : () -> {
			return new ByteArrayInputStream(bytes);
		};
	}

	@Override
	public void listResources(@NotNull PackType packType, @NotNull String namespace, @NotNull String path, @NotNull ResourceOutput output) {
		if (packType != type) {
			return;
		}

		for (Map.Entry<ResourceLocation, byte[]> entry : files.entrySet()) {
			ResourceLocation location = entry.getKey();

			if (!location.getNamespace().equals(namespace)) {
				continue;
			}

			String file = location.getPath();

			if (!path.isEmpty() && !file.startsWith(path + "/")) {
				continue;
			}

			output.accept(location, () -> {
				return new ByteArrayInputStream(entry.getValue());
			});
		}
	}

	@Override
	public @NotNull Set<String> getNamespaces(@NotNull PackType packType) {
		if (packType != type) {
			return Set.of();
		}

		Set<String> namespaces = new LinkedHashSet<>();

		for (ResourceLocation location : files.keySet()) {
			namespaces.add(location.getNamespace());
		}

		return namespaces;
	}

	@Override
	public void close() {
	}
}