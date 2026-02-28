package dev.celestiacraft.libs.client.exporter;

import com.simibubi.create.foundation.utility.worldWrappers.WrappedWorld;
import dev.celestiacraft.libs.compat.ICheckModLoaded;
import dev.celestiacraft.libs.compat.mekanism.MekanismTransmitterHelper;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VirtualBlockLevel extends WrappedWorld {
	private final Map<BlockPos, BlockState> blocks;
	private final Map<BlockPos, CompoundTag> blockEntityNbt;
	private final Map<BlockPos, BlockEntity> blockEntities = new HashMap<>();

	@Getter
	private final List<BlockEntity> renderedBlockEntities = new ArrayList<>();

	@Getter
	private final List<Entity> renderedEntities = new ArrayList<>();

	public VirtualBlockLevel(Level level, Map<BlockPos, BlockState> blocks, Map<BlockPos, CompoundTag> tag) {
		super(level);
		this.blocks = blocks;
		this.blockEntityNbt = tag;
	}

	@Override
	public @NotNull BlockState getBlockState(BlockPos pos) {
		return blocks.getOrDefault(pos, Blocks.AIR.defaultBlockState());
	}

	@Override
	public @NotNull FluidState getFluidState(@NotNull BlockPos pos) {
		return getBlockState(pos).getFluidState();
	}

	@Override
	public int getBrightness(@NotNull LightLayer lightType, @NotNull BlockPos pos) {
		return 15;
	}

	@Nullable
	@Override
	public BlockEntity getBlockEntity(@NotNull BlockPos pos) {
		if (blockEntities.containsKey(pos)) {
			return blockEntities.get(pos);
		}

		BlockState state = getBlockState(pos);

		if (!(state.getBlock() instanceof EntityBlock entityBlock)) {
			return null;
		}

		try {
			BlockEntity blockEntity = entityBlock.newBlockEntity(pos, state);
			if (blockEntity != null) {
				blockEntity.setLevel(this);
				blockEntities.put(pos, blockEntity);
				renderedBlockEntities.add(blockEntity);

				if (blockEntityNbt.containsKey(pos)) {
					try {
						blockEntity.load(blockEntityNbt.get(pos));
					} catch (Exception exception) {
						throw new RuntimeException(exception);
					}
				}
			}
			return blockEntity;
		} catch (Exception exception) {
			blockEntities.put(pos, null);
			return null;
		}
	}

	public void initAllBlockEntities() {
		for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
			if (entry.getValue().getBlock() instanceof EntityBlock) {
				getBlockEntity(entry.getKey());
			}
		}
	}

	public void initEntities(List<StructureScene.EntityInfo> infos) {
		for (StructureScene.EntityInfo info : infos) {
			try {
				Entity entity = EntityType.loadEntityRecursive(
						info.nbt(),
						this,
						(loadedEntity) -> {
							Vec3 pos = info.pos();
							loadedEntity.moveTo(
									pos.x,
									pos.y,
									pos.z,
									loadedEntity.getYRot(),
									loadedEntity.getXRot()
							);
							loadedEntity.setDeltaMovement(Vec3.ZERO);
							return loadedEntity;
						}
				);
				if (entity != null) {
					renderedEntities.add(entity);
				}
			} catch (Exception ignored) {
			}
		}
	}

	public void refreshTransmitterConnections() {
		if (!ICheckModLoaded.hasMekanism()) {
			return;
		}

		MekanismTransmitterHelper.refreshConnections(blockEntities);
	}

	public void updateNeighborStates() {
		Map<BlockPos, BlockState> updates = new HashMap<>();
		for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
			BlockPos pos = entry.getKey();
			BlockState state = entry.getValue();
			BlockState updated = state;

			for (Direction direction : Direction.values()) {
				BlockPos neighborPos = pos.relative(direction);
				BlockState neighborState = getBlockState(neighborPos);
				updated = updated.updateShape(
						direction,
						neighborState,
						this,
						pos,
						neighborPos
				);
			}

			if (updated != state) {
				updates.put(pos, updated);
			}
		}

		blocks.putAll(updates);
	}
}