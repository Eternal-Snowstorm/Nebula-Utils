package dev.celestiacraft.libs.client.exporter;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class StructureScene {
	public record EntityInfo(Vec3 pos, CompoundTag nbt) {
	}

	private final Map<BlockPos, BlockState> blocks = new HashMap<>();
	private final Map<BlockPos, CompoundTag> blockEntityNbt = new HashMap<>();
	private final List<EntityInfo> entities = new ArrayList<>();
	private final List<String> missingBlocks = new ArrayList<>();

	private int sizeX;
	private int sizeY;
	private int sizeZ;

	private float centerX;
	private float centerY;
	private float centerZ;

	private float maxDimension;

	private StructureScene() {
	}

	public static StructureScene loadFromFile(Path path) throws IOException {
		CompoundTag root = NbtIo.readCompressed(path.toFile());

		StructureScene scene = new StructureScene();
		scene.parse(root);

		return scene;
	}

	@SuppressWarnings("deprecation")
	private void parse(CompoundTag root) {
		ListTag sizeTag = root.getList("size", Tag.TAG_INT);
		sizeX = sizeTag.getInt(0);
		sizeY = sizeTag.getInt(1);
		sizeZ = sizeTag.getInt(2);

		ListTag paletteTag = root.getList("palette", Tag.TAG_COMPOUND);
		BlockState[] palette = new BlockState[paletteTag.size()];

		for (int index = 0; index < paletteTag.size(); index++) {
			CompoundTag paletteEntry = paletteTag.getCompound(index);
			String blockName = paletteEntry.getString("Name");
			ResourceLocation blockId = ResourceLocation.parse(blockName);

			if (!BuiltInRegistries.BLOCK.containsKey(blockId)) {
				palette[index] = Blocks.AIR.defaultBlockState();

				if (!missingBlocks.contains(blockName)) {
					missingBlocks.add(blockName);
				}
			} else {
				palette[index] = NbtUtils.readBlockState(
						BuiltInRegistries.BLOCK.asLookup(),
						paletteEntry
				);
			}
		}

		ListTag blocksList = root.getList("blocks", Tag.TAG_COMPOUND);

		for (int index = 0; index < blocksList.size(); index++) {

			CompoundTag entry = blocksList.getCompound(index);
			ListTag posTag = entry.getList("pos", Tag.TAG_INT);

			BlockPos pos = new BlockPos(
					posTag.getInt(0),
					posTag.getInt(1),
					posTag.getInt(2)
			);

			int stateIndex = entry.getInt("state");

			BlockState state;
			if (stateIndex >= 0 && stateIndex < palette.length) {
				state = palette[stateIndex];
			} else {
				state = Blocks.AIR.defaultBlockState();
			}

			if (!state.isAir()) {
				blocks.put(pos, state);
			}

			if (entry.contains("nbt", Tag.TAG_COMPOUND)) {
				blockEntityNbt.put(pos, entry.getCompound("nbt"));
			}
		}

		centerX = sizeX / 2f;
		centerY = sizeY / 2f;
		centerZ = sizeZ / 2f;

		maxDimension = Math.max(sizeX, Math.max(sizeY, sizeZ));

		if (root.contains("entities", Tag.TAG_LIST)) {
			ListTag entitiesList = root.getList("entities", Tag.TAG_COMPOUND);

			for (int index = 0; index < entitiesList.size(); index++) {
				CompoundTag entityEntry = entitiesList.getCompound(index);

				if (!entityEntry.contains("nbt", Tag.TAG_COMPOUND)) {
					continue;
				}

				ListTag posTag = entityEntry.getList("pos", Tag.TAG_DOUBLE);

				Vec3 pos = new Vec3(
						posTag.getDouble(0),
						posTag.getDouble(1),
						posTag.getDouble(2)
				);

				CompoundTag nbt = entityEntry.getCompound("nbt");

				entities.add(new EntityInfo(pos, nbt));
			}
		}
	}
}