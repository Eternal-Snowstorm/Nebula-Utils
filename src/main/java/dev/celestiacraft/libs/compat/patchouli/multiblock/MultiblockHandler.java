package dev.celestiacraft.libs.compat.patchouli.multiblock;

import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
 * boolean hasLogs = multiblock.containsBlock(state -> state.is(BlockTags.LOGS));
 *
 * // 查找结构中所有铁块的位置
 * List<BlockPos> ironPositions = multiblock.findBlock(Blocks.IRON_BLOCK);
 *
 * // 统计结构中金块的数量
 * int goldCount = multiblock.countBlock(Blocks.GOLD_BLOCK);
 * }</pre>
 */
public class MultiblockHandler {
	private final BlockEntity entity;
	private final Supplier<IMultiblock> structure;
	private final String tranKey;
	private final BlockPos renderOffset;
	private final int cacheTicks;

	private boolean cachedValid = false;
	private long lastValidationTick = -1;

	private boolean isShowingVisualization = false;

	private MultiblockHandler(BlockEntity owner, Supplier<IMultiblock> structure, String tranKey, BlockPos renderOffset, int cacheTicks) {
		this.entity = owner;
		this.structure = structure;
		this.tranKey = tranKey;
		this.renderOffset = renderOffset;
		this.cacheTicks = cacheTicks;
	}

	/**
	 * 判断多方块结构是否完整(带 tick 缓存).
	 *
	 * <p>
	 * 在缓存有效期内直接返回缓存结果，避免每 tick 都执行完整的 validate 遍历.
	 * 缓存过期后自动重新验证.
	 * </p>
	 *
	 * @return true 如果结构完整
	 */
	@Info("Checks if the multiblock structure is valid (with tick-based caching)\n\n判断多方块结构是否完整(带 tick 缓存)")
	public boolean isValid() {
		Level level = entity.getLevel();

		if (level == null) {
			return false;
		}
		long currentTick = level.getGameTime();

		if (lastValidationTick >= 0 && (currentTick - lastValidationTick) < cacheTicks) {
			return cachedValid;
		}
		cachedValid = structure.get().validate(level, entity.getBlockPos()) != null;
		lastValidationTick = currentTick;
		return cachedValid;
	}

	/**
	 * 强制立即重新验证，忽略缓存.
	 *
	 * <p>
	 * 适用于需要立即获取最新状态的场景(如玩家刚放置/破坏方块后).
	 * 验证结果会更新缓存.
	 * </p>
	 *
	 * @return true 如果结构完整
	 */
	@Info("Forces immediate re-validation, ignoring cache\n\n强制立即重新验证，忽略缓存")
	public boolean forceValidate() {
		Level level = entity.getLevel();
		if (level == null) {
			return false;
		}
		cachedValid = structure.get().validate(level, entity.getBlockPos()) != null;
		lastValidationTick = level.getGameTime();
		return cachedValid;
	}

	/**
	 * 使缓存失效，下次调用 isValid() 时会重新验证.
	 */
	@Info("Invalidates the validation cache\n\n使验证缓存失效")
	public void invalidateCache() {
		lastValidationTick = -1;
	}


	/**
	 * 切换多方块全息预览的显示/隐藏.
	 *
	 * <p>
	 * 仅在客户端生效. 逻辑如下：
	 * <ul>
	 *     <li>如果结构已完整 → 强制关闭预览(不需要引导了)</li>
	 *     <li>如果当前正在显示 → 关闭预览</li>
	 *     <li>如果当前未显示 → 打开预览</li>
	 * </ul>
	 * </p>
	 */
	@Info("Toggles the multiblock holographic preview on/off\n\n切换多方块全息预览的显示/隐藏")
	public void toggleVisualization() {
		Level level = entity.getLevel();

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

	/**
	 * 显示多方块全息预览. 仅客户端生效.
	 */
	@Info("Shows the multiblock holographic preview\n\n显示多方块全息预览")
	public void showVisualization() {
		Level level = entity.getLevel();

		if (level == null || !level.isClientSide) {
			return;
		}

		PatchouliAPI.get().showMultiblock(
				structure.get(),
				Component.translatable(tranKey),
				entity.getBlockPos().offset(renderOffset),
				Rotation.NONE
		);
		isShowingVisualization = true;
	}

	/**
	 * 隐藏多方块全息预览. 仅客户端生效.
	 */
	@Info("Hides the multiblock holographic preview\n\n隐藏多方块全息预览")
	public void hideVisualization() {
		Level level = entity.getLevel();

		if (level == null || !level.isClientSide) {
			return;
		}

		PatchouliAPI.get().clearMultiblock();
		isShowingVisualization = false;
	}

	/**
	 * 当前是否正在显示全息预览.
	 */
	@Info("Whether the holographic preview is currently showing\n\n当前是否正在显示全息预览")
	public boolean isShowingVisualization() {
		return isShowingVisualization;
	}

	/**
	 * 获取底层的 IMultiblock 结构定义.
	 */
	@Info("Gets the underlying IMultiblock structure definition\n\n获取底层的 IMultiblock 结构定义")
	public IMultiblock getStructure() {
		return structure.get();
	}

	/**
	 * 获取所属的 BlockEntity.
	 */
	public BlockEntity getBlockEntity() {
		return entity;
	}

	/**
	 * 检测已成型的多方块结构中是否包含指定方块.
	 *
	 * <p>
	 * 先通过 validate 获取成型旋转方向，再通过 simulate 遍历所有位置，
	 * 检查世界中对应坐标的方块是否与目标匹配.
	 * </p>
	 *
	 * @param block 要检测的方块
	 * @return true 如果结构中存在该方块
	 */
	@Info("Checks if the formed multiblock contains a specific block\n\n检测已成型的多方块结构中是否包含指定方块")
	public boolean containsBlock(Block block) {
		return containsBlock(state -> state.is(block));
	}

	/**
	 * 检测已成型的多方块结构中是否包含满足条件的方块.
	 *
	 * <p>
	 * 使用自定义谓词检查结构中每个位置的 BlockState.
	 * 适用于按标签、属性等复杂条件匹配的场景.
	 * </p>
	 *
	 * @param predicate BlockState 匹配条件
	 * @return true 如果结构中存在满足条件的方块
	 */
	@Info("Checks if the formed multiblock contains a block matching the predicate\n\n检测已成型的多方块结构中是否包含满足条件的方块")
	public boolean containsBlock(Predicate<BlockState> predicate) {
		Level level = entity.getLevel();
		if (level == null) {
			return false;
		}

		IMultiblock mb = structure.get();
		Rotation rotation = mb.validate(level, entity.getBlockPos());
		if (rotation == null) {
			return false;
		}

		Pair<BlockPos, Collection<IMultiblock.SimulateResult>> result =
				mb.simulate(level, entity.getBlockPos(), rotation, false);

		for (IMultiblock.SimulateResult sr : result.getSecond()) {
			BlockState worldState = level.getBlockState(sr.getWorldPosition());
			if (predicate.test(worldState)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 查找已成型的多方块结构中所有指定方块的位置.
	 *
	 * @param block 要查找的方块
	 * @return 匹配位置列表，结构未成型时返回空列表
	 */
	@Info("Finds all positions of a specific block within the formed multiblock\n\n查找已成型的多方块结构中所有指定方块的位置")
	public List<BlockPos> findBlock(Block block) {
		return findBlock(state -> state.is(block));
	}

	/**
	 * 查找已成型的多方块结构中所有属于指定标签的方块位置.
	 *
	 * @param tag 要查找的方块标签
	 * @return 匹配位置列表，结构未成型时返回空列表
	 */
	@Info("Finds all positions of blocks matching a tag within the formed multiblock\n\n查找已成型的多方块结构中所有属于指定标签的方块位置")
	public List<BlockPos> findBlock(TagKey<Block> tag) {
		return findBlock(state -> state.is(tag));
	}

	/**
	 * 查找已成型的多方块结构中所有满足条件的方块位置.
	 *
	 * @param predicate BlockState 匹配条件
	 * @return 匹配位置列表，结构未成型时返回空列表
	 */
	@Info("Finds all positions matching the predicate within the formed multiblock\n\n查找已成型的多方块结构中所有满足条件的方块位置")
	public List<BlockPos> findBlock(Predicate<BlockState> predicate) {
		List<BlockPos> positions = new ArrayList<>();
		Level level = entity.getLevel();
		if (level == null) {
			return positions;
		}

		IMultiblock mb = structure.get();
		Rotation rotation = mb.validate(level, entity.getBlockPos());
		if (rotation == null) {
			return positions;
		}

		Pair<BlockPos, Collection<IMultiblock.SimulateResult>> result =
				mb.simulate(level, entity.getBlockPos(), rotation, false);

		for (IMultiblock.SimulateResult sr : result.getSecond()) {
			BlockState worldState = level.getBlockState(sr.getWorldPosition());
			if (predicate.test(worldState)) {
				positions.add(sr.getWorldPosition());
			}
		}
		return positions;
	}

	/**
	 * 统计已成型的多方块结构中指定方块的数量.
	 *
	 * @param block 要统计的方块
	 * @return 匹配数量，结构未成型时返回 0
	 */
	@Info("Counts occurrences of a specific block within the formed multiblock\n\n统计已成型的多方块结构中指定方块的数量")
	public int countBlock(Block block) {
		return findBlock(block).size();
	}

	/**
	 * 统计已成型的多方块结构中属于指定标签的方块数量.
	 *
	 * @param tag 要统计的方块标签
	 * @return 匹配数量，结构未成型时返回 0
	 */
	@Info("Counts occurrences of blocks matching a tag within the formed multiblock\n\n统计已成型的多方块结构中属于指定标签的方块数量")
	public int countBlock(TagKey<Block> tag) {
		return findBlock(tag).size();
	}

	/**
	 * 创建 MultiblockHandler 构建器.
	 *
	 * @param entity    持有此 Handler 的 BlockEntity
	 * @param structure 多方块结构的懒加载供应器
	 * @return Builder 实例
	 */
	@Info("Creates a MultiblockHandler builder\n\n创建 MultiblockHandler 构建器")
	public static Builder builder(BlockEntity entity, Supplier<IMultiblock> structure) {
		return new Builder(entity, structure);
	}

	/**
	 * MultiblockHandler 的构建器.
	 *
	 * <p>
	 * 通过链式调用配置各项参数，最终调用 build() 生成 Handler 实例.
	 * 所有配置项均有合理默认值，最简用法只需要 owner 和 structure.
	 * </p>
	 */
	public static class Builder {
		private final BlockEntity block;
		private final Supplier<IMultiblock> structure;
		private String tranKey = null;
		private BlockPos renderOffset = BlockPos.ZERO;
		private int cacheTicks = 20;

		private Builder(BlockEntity entity, Supplier<IMultiblock> structure) {
			this.block = entity;
			this.structure = structure;
		}

		/**
		 * 设置全息预览显示的翻译 key.
		 *
		 * <p>
		 * 如果不设置，将自动从 BlockEntity 所属方块的 registry name 推导：
		 * {@code multiblock.building.<namespace>.<path>}
		 * </p>
		 *
		 * @param tranKey 翻译 key
		 * @return Builder 自身
		 */
		@Info("Sets the translation key for the visualization display name\n\n设置全息预览显示的翻译 key")
		public Builder translationKey(String tranKey) {
			this.tranKey = tranKey;
			return this;
		}

		/**
		 * 设置渲染偏移量.
		 *
		 * <p>
		 * Patchouli 渲染多方块时可能需要位置修正(例如 Y 轴下沉一格).
		 * 此偏移量会叠加到 BlockEntity 的 worldPosition 上.
		 * </p>
		 *
		 * @param x X 偏移
		 * @param y Y 偏移
		 * @param z Z 偏移
		 * @return Builder 自身
		 */
		@Info("Sets the render offset for visualization\n\n设置渲染偏移量")
		public Builder renderOffset(int x, int y, int z) {
			this.renderOffset = new BlockPos(x, y, z);
			return this;
		}

		/**
		 * 设置渲染偏移量.
		 *
		 * @param offset 偏移 BlockPos
		 * @return Builder 自身
		 */
		@Info("Sets the render offset for visualization\n\n设置渲染偏移量")
		public Builder renderOffset(BlockPos offset) {
			this.renderOffset = offset;
			return this;
		}

		/**
		 * 设置验证缓存的 tick 间隔.
		 *
		 * <p>
		 * 默认 20 tick(1秒). 值越大性能越好，但验证响应越慢.
		 * 设置为 0 表示禁用缓存(每次调用都验证).
		 * </p>
		 *
		 * @param ticks 缓存间隔(tick数)
		 * @return Builder 自身
		 */
		@Info("Sets the validation cache interval in ticks (default: 20)\n\n设置验证缓存的 tick 间隔(默认20)")
		public Builder cacheTicks(int ticks) {
			this.cacheTicks = Math.max(0, ticks);
			return this;
		}

		/**
		 * 构建 MultiblockHandler 实例.
		 *
		 * @return 配置完成的 MultiblockHandler
		 */
		@Info("Builds the MultiblockHandler instance\n\n构建 MultiblockHandler 实例")
		public MultiblockHandler build() {
			String resolvedKey = this.tranKey;

			if (resolvedKey == null) {
				ResourceLocation blockKey = ForgeRegistries.BLOCKS.getKey(block.getBlockState().getBlock());
				if (blockKey != null) {
					resolvedKey = String.format("multiblock.building.%s.%s", blockKey.getNamespace(), blockKey.getPath());
				}
			}
			return new MultiblockHandler(block, structure, resolvedKey, renderOffset, cacheTicks);
		}
	}
}
