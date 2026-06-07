package dev.celestiacraft.libs.client.exporter;

import dev.celestiacraft.libs.mixin.EntityAccessor;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 包裹一个真实 {@link Level}，把只读查询转发给宿主世界，并屏蔽一切会影响真实存档/客户端状态的副作用
 * （声音、方块更新事件、爆破进度等）。子类可覆写读取方法以提供虚拟内容，用于离屏渲染。
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class WrappedLevel extends Level {

	protected final Level host;
	private final LevelEntityGetter<Entity> emptyEntityGetter = new EmptyEntityGetter<>();

	public WrappedLevel(Level host) {
		super((WritableLevelData) host.getLevelData(), host.dimension(), host.registryAccess(),
				host.dimensionTypeRegistration(), host::getProfiler, host.isClientSide, host.isDebug(), 0, 0);
		this.host = host;
	}

	/* ---------- 转发给宿主世界的只读查询 ---------- */

	@Override
	public BlockState getBlockState(BlockPos pos) {
		return host.getBlockState(pos);
	}

	@Override
	public boolean isStateAtPosition(BlockPos pos, Predicate<BlockState> predicate) {
		return host.isStateAtPosition(pos, predicate);
	}

	@Nullable
	@Override
	public BlockEntity getBlockEntity(BlockPos pos) {
		return host.getBlockEntity(pos);
	}

	@Override
	public LevelLightEngine getLightEngine() {
		return host.getLightEngine();
	}

	@Override
	public LevelTickAccess<Block> getBlockTicks() {
		return host.getBlockTicks();
	}

	@Override
	public LevelTickAccess<Fluid> getFluidTicks() {
		return host.getFluidTicks();
	}

	@Override
	public ChunkSource getChunkSource() {
		return host.getChunkSource();
	}

	@Override
	public RecipeManager getRecipeManager() {
		return host.getRecipeManager();
	}

	@Override
	public Scoreboard getScoreboard() {
		return host.getScoreboard();
	}

	@Override
	public RegistryAccess registryAccess() {
		return host.registryAccess();
	}

	@Override
	public FeatureFlagSet enabledFeatures() {
		return host.enabledFeatures();
	}

	@Override
	public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z) {
		return host.getUncachedNoiseBiome(x, y, z);
	}

	@Override
	public float getShade(Direction direction, boolean shaded) {
		return host.getShade(direction, shaded);
	}

	@Override
	public int getFreeMapId() {
		return host.getFreeMapId();
	}

	@Override
	public String gatherChunkSourceStats() {
		return host.gatherChunkSourceStats();
	}

	/* ---------- 渲染相关：恒定满亮，避免离屏世界出现暗块 ---------- */

	@Override
	public int getMaxLocalRawBrightness(BlockPos pos) {
		return 15;
	}

	/* ---------- 写入：方块写入仍落到宿主，其余副作用一律屏蔽 ---------- */

	@Override
	public boolean setBlock(BlockPos pos, BlockState state, int flags) {
		return host.setBlock(pos, state, flags);
	}

	@Override
	public boolean addFreshEntity(Entity entity) {
		((EntityAccessor) entity).nebula$callSetLevel(host);
		return host.addFreshEntity(entity);
	}

	@Override
	public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
		host.sendBlockUpdated(pos, oldState, newState, flags);
	}

	@Override
	public void updateNeighbourForOutputSignal(BlockPos pos, Block block) {}

	@Override
	public void levelEvent(@Nullable Player player, int type, BlockPos pos, int data) {}

	@Override
	public void destroyBlockProgress(int breakerId, BlockPos pos, int progress) {}

	@Override
	public void gameEvent(@Nullable Entity entity, GameEvent event, BlockPos pos) {}

	@Override
	public void gameEvent(GameEvent event, Vec3 position, GameEvent.Context context) {}

	@Override
	public void playSeededSound(@Nullable Player player, double x, double y, double z, Holder<SoundEvent> sound,
								SoundSource source, float volume, float pitch, long seed) {}

	@Override
	public void playSeededSound(@Nullable Player player, Entity entity, Holder<SoundEvent> sound,
								SoundSource source, float volume, float pitch, long seed) {}

	@Override
	public void playSound(@Nullable Player player, double x, double y, double z, SoundEvent sound,
						  SoundSource source, float volume, float pitch) {}

	@Override
	public void playSound(@Nullable Player player, Entity entity, SoundEvent sound,
						  SoundSource source, float volume, float pitch) {}

	@Override
	public void setMapData(String mapId, MapItemSavedData data) {}

	/* ---------- 离屏世界不持有实体/地图，统一返回空 ---------- */

	@Nullable
	@Override
	public Entity getEntity(int id) {
		return null;
	}

	@Nullable
	@Override
	public MapItemSavedData getMapData(String mapId) {
		return null;
	}

	@Override
	public List<? extends Player> players() {
		return Collections.emptyList();
	}

	@Override
	protected LevelEntityGetter<Entity> getEntities() {
		return emptyEntityGetter;
	}

	/*
	 * 高度/分段方法全部锚定宿主真实高度并走原版公式，避免被 Lithium 等性能 mod 对 Level 的 Mixin
	 * 干扰而返回错误的分段索引（否则可能导致离屏渲染时光照/分段数组越界）。
	 */

	@Override
	public int getMinBuildHeight() {
		return host.getMinBuildHeight();
	}

	@Override
	public int getHeight() {
		return host.getHeight();
	}

	@Override
	public int getMaxBuildHeight() {
		return getMinBuildHeight() + getHeight();
	}

	@Override
	public int getMinSection() {
		return SectionPos.blockToSectionCoord(getMinBuildHeight());
	}

	@Override
	public int getMaxSection() {
		return SectionPos.blockToSectionCoord(getMaxBuildHeight() - 1) + 1;
	}

	@Override
	public int getSectionsCount() {
		return getMaxSection() - getMinSection();
	}

	@Override
	public int getSectionIndex(int y) {
		return getSectionIndexFromSectionY(SectionPos.blockToSectionCoord(y));
	}

	@Override
	public int getSectionIndexFromSectionY(int sectionY) {
		return sectionY - getMinSection();
	}

	@Override
	public int getSectionYFromSectionIndex(int sectionIndex) {
		return sectionIndex + getMinSection();
	}

	@Override
	public boolean isOutsideBuildHeight(BlockPos pos) {
		return isOutsideBuildHeight(pos.getY());
	}

	@Override
	public boolean isOutsideBuildHeight(int y) {
		return y < getMinBuildHeight() || y >= getMaxBuildHeight();
	}

	private static final class EmptyEntityGetter<T extends EntityAccess> implements LevelEntityGetter<T> {

		@Nullable
		@Override
		public T get(int id) {
			return null;
		}

		@Nullable
		@Override
		public T get(UUID uuid) {
			return null;
		}

		@Override
		public Iterable<T> getAll() {
			return Collections.emptyList();
		}

		@Override
		public <U extends T> void get(EntityTypeTest<T, U> test, AbortableIterationConsumer<U> consumer) {}

		@Override
		public void get(AABB box, Consumer<T> consumer) {}

		@Override
		public <U extends T> void get(EntityTypeTest<T, U> test, AABB box, AbortableIterationConsumer<U> consumer) {}
	}
}
