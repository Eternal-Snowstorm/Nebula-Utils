package dev.celestiacraft.libs.common.material;

import dev.celestiacraft.libs.NebulaLibs;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Consumer;

/**
 * <h2>MaterialPackSource</h2>
 *
 * <p>
 * 向游戏注入 {@link MaterialPack} 的资源包来源.
 * </p>
 */
public class MaterialPackSource implements RepositorySource {
	private final PackType type;

	public MaterialPackSource(PackType type) {
		this.type = type;
	}

	@Override
	public void loadPacks(@NotNull Consumer<Pack> onLoad) {
		Map<ResourceLocation, byte[]> files = MaterialAssets.files(type);

		if (files.isEmpty()) {
			return;
		}

		String id = NebulaLibs.MODID + "_materials_" + type.getDirectory();

		Pack pack = Pack.readMetaAndCreate(
				id,
				Component.translatable("resourcePack.nebula_libs.material"),
				true,
				(name) -> new MaterialPack(name, type, files),
				type,
				Pack.Position.BOTTOM,
				PackSource.BUILT_IN
		);

		if (pack != null) {
			onLoad.accept(pack);
		} else {
			NebulaLibs.LOGGER.error("无法创建材料资源包 {}", id);
		}
	}
}