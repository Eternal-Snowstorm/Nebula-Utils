package dev.celestiacraft.libs.compat.patchouli.multiblock;

import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * 多方块结构能力提供接口.
 *
 * <p>
 * 用于标记一个 BlockEntity 拥有多方块结构能力.
 * 通过组合持有 {@link MultiblockHandler}，
 * 统一暴露结构验证和全息预览切换逻辑.
 * </p>
 *
 * <p>
 * 该接口提供默认实现：
 * <ul>
 *     <li>showMultiblock() - 切换多方块全息预览</li>
 *     <li>isStructureValid() - 判断结构是否完整(带缓存)</li>
 * </ul>
 * 实现类只需返回自身持有的 MultiblockHandler 实例即可.
 * </p>
 *
 * <h2>设计目标</h2>
 * <ul>
 *     <li>接口化能力 - 不强制继承任何基类</li>
 *     <li>逻辑集中 - 所有多方块逻辑统一委托给 MultiblockHandler</li>
 *     <li>最小实现 - 仅需实现 getMultiblockHandler()</li>
 * </ul>
 *
 * <h2>典型用法</h2>
 * <pre>{@code
 * public class MyMachineBlockEntity extends BlockEntity
 *         implements IMultiblockProvider {
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
 *
 *     @Override
 *     public BlockEntity getBlockEntity() {
 *         return this;
 *     }
 *
 *     @Override
 *     public void setRemoved() {
 *         cancelShowMultiblock();
 *         super.setRemoved();
 *     }
 * }
 * }</pre>
 */
public interface IMultiblockProvider {
	/**
	 * 获取当前 BlockEntity 持有的 MultiblockHandler.
	 *
	 * <p>
	 * 所有多方块相关逻辑均委托给该实例.
	 * </p>
	 *
	 * @return MultiblockHandler 实例
	 */
	@Info("Gets the MultiblockHandler owned by this BlockEntity\n\n获取当前 BlockEntity 持有的 MultiblockHandler")
	MultiblockHandler getMultiblockHandler();

	/**
	 * 获取当前 BlockEntity 实例.
	 *
	 * <p>
	 * 提供对当前 BlockEntity 的直接访问.
	 * 主要用于在多方块逻辑中需要操作当前 BlockEntity 时.
	 * </p>
	 *
	 * @return 当前 BlockEntity 实例
	 */
	@Info("Gets the BlockEntity instance\n\n获取当前 BlockEntity 实例")
	default BlockEntity getBlockEntity() {
		if (getMultiblockHandler().getContext() instanceof BlockEntityContext context) {
			return context.getBlockEntity();
		}
		return null;
	}

	/**
	 * 切换多方块全息预览的显示/隐藏.
	 *
	 * <p>
	 * 默认委托给 MultiblockHandler.toggleVisualization().
	 * </p>
	 */
	@Info("Toggles the multiblock holographic preview\n\n切换多方块全息预览的显示/隐藏")
	default void showMultiblock() {
		getMultiblockHandler().toggleVisualization();
	}

	/**
	 * 判断多方块结构是否完整.
	 *
	 * <p>
	 * 默认委托给 MultiblockHandler.isValid().
	 * 该方法带 tick 缓存，避免频繁验证.
	 * </p>
	 *
	 * @return true 如果结构完整
	 */
	@Info("Checks if the multiblock structure is valid\n\n判断多方块结构是否完整")
	default boolean isStructureValid() {
		return getMultiblockHandler().isValid();
	}

	/**
	 * 清理并切换多方块全息预览的显示/隐藏.
	 * 切换多方块全息预览的显示/隐藏.
	 *
	 * <p>
	 * 默认委托给 MultiblockHandler.toggleVisualization().
	 * </p>
	 */
	@Info("Toggles the multiblock holographic preview\n\n切换多方块全息预览的显示/隐藏")
	default void cancelShowMultiblock() {
		BlockEntity entity = getBlockEntity();

		if (entity.getLevel() != null && entity.getLevel().isClientSide()) {
			getMultiblockHandler().hideVisualization();
		}
	}
}