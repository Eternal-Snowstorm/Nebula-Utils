package dev.celestiacraft.libs.compat.patchouli.multiblock;

import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.kubejs.typings.Info;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.patchouli.api.IMultiblock;
import vazkii.patchouli.api.PatchouliAPI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 多方块结构的组合式处理器.
 *
 * <p>
 * 封装了 Patchouli 多方块的 <strong>验证缓存</strong> 和 <strong>渲染切换</strong> 逻辑，
 * 供 BlockEntity 以组合方式持有和使用.
 * </p>
 *
 * <p>
 * 一个轻量级委托类，将多方块结构验证(带 tick 缓存)和
 * 客户端全息预览的显示/隐藏逻辑统一封装.
 * </p>
 *
 * <ul>
 *     <li>不强制继承 - 通过组合持有，不限制 BlockEntity 的父类</li>
 *     <li>Tick 缓存 - 默认 20 tick(1秒)刷新一次，大幅减少冗余 validate 调用</li>
 *     <li>一行切换 - toggleVisualization() 自动处理状态判断和 Patchouli API 调用</li>
 *     <li>Builder 配置 - 翻译 key、渲染偏移、缓存间隔均可自定义</li>
 * </ul>
 *
 * <h2>典型用法</h2>
 *
 * <h3>1. BlockEntity 持有 Handler</h3>
 * <pre>{@code
 * public class MyMachineBlockEntity extends BlockEntity
 *         implements IMultiblockProvider {
 *
 *     private static final Lazy<IMultiblock> STRUCTURE = Lazy.of(() -> {
 *         return new MultiblockStructureBuilder(...)
 *             .define(...)
 *             .build();
 *     });
 *
 *     private final MultiblockHandler multiblock =
 *         MultiblockHandler.builder(this, STRUCTURE)
 *             .translationKey("multiblock.building.mymod.my_machine")
 *             .renderOffset(0, -1, 0)
 *             .cacheTicks(20)
 *             .build();
 *
 *     @Override
 *     public MultiblockHandler getMultiblockHandler() {
 *         return multiblock;
 *     }
 * }
 * }</pre>
 *
 * <h3>2. Block 触发全息预览示例</h3>
 * <pre>{@code
 * @Mod.EventBusSubscriber(modid = MyMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
 * public class MyMachineBlock extends Block {
 *     public MyMachineBlock(Properties properties) {
 *         super(properties);
 *     }
 *
 *     @SubscribeEvent
 *     public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
 *         Level level = event.getLevel();
 *
 *         if (!level.isClientSide()) {
 *             return;
 *         }
 *
 *         BlockPos pos = event.getPos();
 *         Player player = event.getEntity();
 *
 *         if (level.getBlockState(pos).getBlock() instanceof MyMachineBlock) {
 *             BlockEntity be = level.getBlockEntity(pos);
 *
 *             if (be instanceof IMultiblockProvider provider) {
 *                 provider.showMultiblock();
 *                 player.swing(event.getHand());
 *             }
 *         }
 *     }
 * }
 * }</pre>
 *
 * <h3>3. 结构内方块检测</h3>
 * <pre>{@code
 * // 检测结构中是否包含某个方块
 * boolean hasDiamond = multiblock.containsBlock(Blocks.DIAMOND_BLOCK);
 *
 * // 按标签检测(例如所有原木类方块)
 * boolean hasLogs = multiblock.containsBlock((state) -> state.is(BlockTags.LOGS));
 *
 * // 查找结构中所有铁块的位置
 * List<BlockPos> ironPositions = multiblock.findBlock(Blocks.IRON_BLOCK);
 *
 * // 统计结构中金块的数量
 * int goldCount = multiblock.countBlock(Blocks.GOLD_BLOCK);
 * }</pre>
 *
 * <h3>4. 破坏结构内方块</h3>
 * <pre>{@code
 * // 破坏结构中所有方块（掉落物品）
 * multiblock.destroyAll(true);
 *
 * // 仅破坏结构中指定类型的方块
 * multiblock.destroyBlock(Blocks.IRON_BLOCK, true);
 *
 * // 破坏所有属于 Tag 的方块（不掉落）
 * multiblock.destroyTag(BlockTags.LOGS, false);
 *
 * // 按谓词精确控制
 * multiblock.destroyFilter((state) -> {
 *     return state.is(Blocks.PISTON) && state.getValue(PistonBlock.FACING) == Direction.UP;
 * }, true);
 * }</pre>
 */
public class MultiblockHandler {
	@Getter
	private final MultiblockContext context;
	private final Supplier<IMultiblock> structure;
	private final String tranKey;
	private final BlockPos renderOffset;
	private final int cacheTicks;

	private boolean cachedValid = false;
	private long lastValidationTick = -1;

	private boolean isShowingVisualization = false;

	private MultiblockHandler(
			MultiblockContext context,
			Supplier<IMultiblock> structure,
			String tranKey,
			BlockPos renderOffset,
			int cacheTicks
	) {
		this.context = context;
		this.structure = structure;
		this.tranKey = tranKey;
		this.renderOffset = renderOffset;
		this.cacheTicks = cacheTicks;
	}

	private Level getLevel() {
		return context.getLevel();
	}

	private BlockPos getBlockPos() {
		return context.getBlockPos();
	}

	@Info("Checks if the multiblock structure is valid (with tick-based caching)\n\n判断多方块结构是否完整(带 tick 缓存)")
	public boolean isValid() {
		Level level = getLevel();

		if (level == null) {
			return false;
		}

		long currentTick = level.getGameTime();

		if (lastValidationTick >= 0 && (currentTick - lastValidationTick) < cacheTicks) {
			return cachedValid;
		}

		cachedValid = structure.get().validate(level, getBlockPos()) != null;
		lastValidationTick = currentTick;

		return cachedValid;
	}

	@Info("Forces immediate re-validation, ignoring cache\n\n强制立即重新验证, 忽略缓存")
	public boolean forceValidate() {
		Level level = getLevel();

		if (level == null) {
			return false;
		}

		cachedValid = structure.get().validate(level, getBlockPos()) != null;
		lastValidationTick = level.getGameTime();

		return cachedValid;
	}

	@Info("Invalidates the validation cache\n\n使验证缓存失效")
	public void invalidateCache() {
		lastValidationTick = -1;
	}

	@Info("Toggles the multiblock holographic preview on/off\n\n切换多方块全息预览的显示/隐藏")
	public void toggleVisualization() {
		Level level = getLevel();

		if (level == null || !level.isClientSide()) {
			return;
		}

		if (isValid()) {
			if (isShowingVisualization) {
				hideVisualization();
			}
			return;
		}

		if (isShowingVisualization) {
			hideVisualization();
		} else {
			showVisualization();
		}
	}

	@Info("Shows the multiblock holographic preview\n\n显示多方块全息预览")
	public void showVisualization() {
		Level level = getLevel();
		BlockState state = level.getBlockState(getBlockPos());
		Direction direction = getFacing(state);

		if (!level.isClientSide()) {
			return;
		}

		Rotation rotation = switch (direction.getOpposite()) {
			case SOUTH -> Rotation.CLOCKWISE_90;
			case WEST -> Rotation.CLOCKWISE_180;
			case EAST -> Rotation.NONE;
			default -> Rotation.COUNTERCLOCKWISE_90;
		};

		PatchouliAPI.get().showMultiblock(
				structure.get(),
				Component.translatable(tranKey),
				getBlockPos().offset(renderOffset),
				rotation
		);

		isShowingVisualization = true;
	}

	@Info("Hides the multiblock holographic preview\n\n隐藏多方块全息预览")
	public void hideVisualization() {
		Level level = getLevel();

		if (level == null || !level.isClientSide) {
			return;
		}

		PatchouliAPI.get().clearMultiblock();

		isShowingVisualization = false;
	}

	@Info("Whether the holographic preview is currently showing\n\n当前是否正在显示全息预览")
	public boolean isShowingVisualization() {
		return isShowingVisualization;
	}

	@Info("Gets the underlying IMultiblock structure definition\n\n获取底层的 IMultiblock 结构定义")
	public IMultiblock getStructure() {
		return structure.get();
	}

	@Info("Checks if the formed multiblock contains a specific block\n\n检测已成型的多方块结构中是否包含指定方块")
	public boolean containsBlock(Block block) {
		return containsFilterBlock((state) -> {
			return state.is(block);
		});
	}

	@Info("Checks if the formed multiblock contains a block matching the predicate\n\n检测已成型的多方块结构中是否包含满足条件的方块")
	public boolean containsFilterBlock(Predicate<BlockState> predicate) {
		return !findFilterBlock(predicate).isEmpty();
	}

	@Info("Finds all positions of a specific block within the formed multiblock\n\n查找已成型的多方块结构中所有指定方块的位置")
	public List<BlockPos> findBlock(Block block) {
		return findFilterBlock((state) -> {
			return state.is(block);
		});
	}

	@Info("Finds all positions of blocks matching a tag within the formed multiblock\n\n查找已成型的多方块结构中所有属于指定标签的方块位置")
	public List<BlockPos> findBlockTag(TagKey<Block> tag) {
		return findFilterBlock((state) -> {
			return state.is(tag);
		});
	}

	@Info("Finds all positions matching the predicate within the formed multiblock\n\n查找已成型的多方块结构中所有满足条件的方块位置")
	public List<BlockPos> findFilterBlock(Predicate<BlockState> predicate) {
		List<BlockPos> positions = new ArrayList<>();
		Level level = getLevel();

		if (level == null) {
			return positions;
		}

		IMultiblock mb = structure.get();
		Rotation rotation = mb.validate(level, getBlockPos());

		if (rotation == null) {
			return positions;
		}

		Pair<BlockPos, Collection<IMultiblock.SimulateResult>> result = mb.simulate(
				level,
				getBlockPos(),
				rotation,
				false
		);

		for (IMultiblock.SimulateResult sr : result.getSecond()) {
			BlockPos pos = sr.getWorldPosition();
			BlockState state = level.getBlockState(pos);

			if (predicate.test(state)) {
				positions.add(pos);
			}
		}

		return positions;
	}

	@Info("Counts occurrences of a specific block within the formed multiblock\n\n统计已成型的多方块结构中指定方块的数量")
	public int countBlock(Block block) {
		return findBlock(block).size();
	}

	@Info("Counts occurrences of blocks matching a tag within the formed multiblock\n\n统计已成型的多方块结构中属于指定标签的方块数量")
	public int countBlockTag(TagKey<Block> tag) {
		return findBlockTag(tag).size();
	}

	@Info("Destroys all non-air blocks in the formed multiblock structure\n\n破坏已成型的多方块结构中所有非空气方块")
	public int destroyAll(boolean dropItems) {
		return destroyMatching((state) -> {
			return !state.isAir();
		}, dropItems);
	}

	@Info("Destroys all blocks of the specified type in the formed multiblock\n\n破坏已成型的多方块结构中所有指定类型的方块")
	public int destroyBlock(Block block, boolean dropItems) {
		return destroyMatching((state) -> {
			return state.is(block);
		}, dropItems);
	}

	@Info("Destroys all blocks matching the specified tag in the formed multiblock\n\n破坏已成型的多方块结构中所有属于指定标签的方块")
	public int destroyTag(TagKey<Block> tag, boolean dropItems) {
		return destroyMatching((state) -> {
			return state.is(tag);
		}, dropItems);
	}

	@Info("Destroys all blocks matching the predicate in the formed multiblock\n\n破坏已成型的多方块结构中所有满足自定义条件的方块")
	public int destroyFilter(Predicate<BlockState> predicate, boolean dropItems) {
		return destroyMatching(predicate, dropItems);
	}

	private int destroyMatching(Predicate<BlockState> predicate, boolean dropItems) {
		Level level = getLevel();

		if (level == null || level.isClientSide()) {
			return 0;
		}

		IMultiblock mb = structure.get();
		Rotation rotation = mb.validate(level, getBlockPos());

		if (rotation == null) {
			return 0;
		}

		Pair<BlockPos, Collection<IMultiblock.SimulateResult>> result = mb.simulate(
				level,
				getBlockPos(),
				rotation,
				false
		);

		int count = 0;

		for (IMultiblock.SimulateResult sr : result.getSecond()) {

			BlockPos pos = sr.getWorldPosition();
			BlockState state = level.getBlockState(pos);

			if (!state.isAir() && predicate.test(state)) {
				level.destroyBlock(pos, dropItems);
				count++;
			}
		}

		if (count > 0) {
			invalidateCache();
		}

		return count;
	}

	@Info("Gets the facing direction of the formed multiblock\n\n获取多方块结构的朝向")
	public Direction getDirection() {
		Level level = getLevel();

		if (level == null) {
			return Direction.NORTH;
		}

		IMultiblock mb = structure.get();
		Rotation rotation = mb.validate(level, getBlockPos());

		if (rotation == null) {
			return Direction.NORTH;
		}

		return rotation.rotate(Direction.NORTH);
	}

	@Info("Creates a MultiblockHandler builder\n\n创建 MultiblockHandler 构建器")
	public static Builder builder(BlockEntity entity, Supplier<IMultiblock> structure) {
		return new Builder(new BlockEntityContext(entity), structure);
	}

	@Info("Creates a MultiblockHandler builder without BlockEntity\n\n创建无 BlockEntity 的 MultiblockHandler 构建器")
	public static Builder builder(Level level, BlockPos pos, Supplier<IMultiblock> structure) {
		return new Builder(new WorldContext(level, pos), structure);
	}

	public static class Builder {
		private final MultiblockContext context;
		private final Supplier<IMultiblock> structure;

		private String tranKey = null;
		private BlockPos renderOffset = BlockPos.ZERO;
		private int cacheTicks = 20;

		private Builder(MultiblockContext context, Supplier<IMultiblock> structure) {
			this.context = context;
			this.structure = structure;
		}

		@Info("Sets the translation key for the visualization display name\n\n设置全息预览显示的翻译 key")
		public Builder translationKey(String tranKey) {
			this.tranKey = tranKey;
			return this;
		}

		@Info("Sets the render offset for visualization\n\n设置渲染偏移量")
		public Builder renderOffset(int x, int y, int z) {
			this.renderOffset = new BlockPos(x, y, z);
			return this;
		}

		@Info("Sets the validation cache interval in ticks (default: 20)\n\n设置验证缓存的 tick 间隔(默认20)")
		public Builder cacheTicks(int ticks) {
			this.cacheTicks = Math.max(0, ticks);
			return this;
		}

		@Info("Builds the MultiblockHandler instance\n\n构建 MultiblockHandler 实例")
		public MultiblockHandler build() {
			String resolvedKey = tranKey;

			if (resolvedKey == null && context instanceof BlockEntityContext entityContext) {
				ResourceLocation blockKey = ForgeRegistries.BLOCKS.getKey(
						entityContext.getBlockEntity().getBlockState().getBlock()
				);

				if (blockKey != null) {
					resolvedKey = String.format(
							"multiblock.building.%s.%s",
							blockKey.getNamespace(),
							blockKey.getPath()
					);
				}
			}

			return new MultiblockHandler(context, structure, resolvedKey, renderOffset, cacheTicks);
		}
	}

	private Direction getFacing(BlockState state) {
		if (state.hasProperty(BlockStateProperties.FACING)) {
			return state.getValue(BlockStateProperties.FACING);
		}
		if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
			return state.getValue(BlockStateProperties.HORIZONTAL_FACING);
		}
		return Direction.NORTH;
	}
}