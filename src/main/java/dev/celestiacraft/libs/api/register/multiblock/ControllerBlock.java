package dev.celestiacraft.libs.api.register.multiblock;

import com.simibubi.create.AllTags;
import com.simibubi.create.foundation.block.IBE;
import dev.celestiacraft.libs.api.interaction.context.UseContext;
import dev.celestiacraft.libs.api.register.block.BasicBlock;
import dev.celestiacraft.libs.api.register.block.IBasicEntityBlock;
import dev.celestiacraft.libs.common.register.NebulaItem;
import dev.celestiacraft.libs.compat.patchouli.multiblock.IMultiblockProvider;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * 多方块控制器 Block 基类
 *
 * <p>
 * 作为多方块结构的核心控制方块, 负责交互入口与基础行为封装
 * </p>
 *
 * <p>
 * 提供功能:
 * </p>
 * <ul>
 *     <li>使用触发器(默认扳手)右键显示多方块结构预览</li>
 *     <li>自动获取并绑定 {@link BlockEntity}(基于 {@link IBE})</li>
 *     <li>内置方向系统(支持无方向 / 水平 / 六面)</li>
 *     <li>统一处理旋转与镜像逻辑</li>
 * </ul>
 *
 * <p>
 * 扩展点:
 * </p>
 * <ul>
 *     <li>{@link #useFacingType()}: 定义方向类型</li>
 *     <li>{@link #isTrigger(UseContext)}: 自定义触发工具</li>
 * </ul>
 *
 * <p>
 * 子类通常只需:
 * </p>
 * <ul>
 *     <li>实现对应的 {@link BlockEntity}</li>
 * </ul>
 *
 * <p>
 * 无需手动处理事件注册或基础交互逻辑
 * </p>
 */
public abstract class ControllerBlock<T extends BlockEntity & IMultiblockProvider> extends BasicBlock implements IBasicEntityBlock<T> {
	public ControllerBlock(Properties properties) {
		super(properties);
	}

	/**
	 * 右键交互
	 *
	 * <p>
	 * 默认行为:
	 * 使用触发器右键后显示多方块结构
	 * </p>
	 */
	@Override
	public InteractionResult useOn(UseContext context) {
		// 客户端: 渲染结构
		if (context.isClient()) {
			if (isTrigger(context)
					&& context.getHand() == InteractionHand.MAIN_HAND
					&& !context.getPlayer().isShiftKeyDown()) {

				T be = getBlockEntity(context.getLevel(), context.getPos());

				if (be != null) {
					context.getPlayer().swing(context.getHand());
					be.showMultiblock();
					return InteractionResult.SUCCESS;
				}
			}
		}

		// 服务端: 调试结构
		if (!context.isClient()) {
			if (context.getStack().is(NebulaItem.GEOLOGICAL_HAMMER.get())
					&& context.getHand() == InteractionHand.MAIN_HAND) {

				T be = getBlockEntity(context.getLevel(), context.getPos());

				if (be != null) {
					if (be.isStructureValid()) {
						context.getPlayer().displayClientMessage(
								Component.translatable("tip.structurally_valid"),
								true
						);
						return InteractionResult.SUCCESS;
					} else {
						context.getPlayer().displayClientMessage(
								Component.translatable("tip.structurally_invalid"),
								true
						);
					}
				}
			}
		}

		return InteractionResult.PASS;
	}

	/**
	 * 获取触发器
	 *
	 * @return
	 */
	protected TagKey<Item> getTriggerTag() {
		return AllTags.AllItemTags.WRENCH.tag;
	}

	/**
	 * 是否为触发器
	 *
	 * @param context
	 * @return
	 */
	protected boolean isTrigger(UseContext context) {
		return context.getStack().is(getTriggerTag());
	}

	/**
	 * 获取触发器名称
	 *
	 * @return
	 */
	protected Component getTriggerName() {
		TagKey<Item> tag = getTriggerTag();
		ResourceLocation id = tag.location();
		String path = id.getPath().replace('/', '.');
		String key = "tag.item.%s.%s".formatted(id.getNamespace(), path);

		return Component.translatable(key);
	}

	/**
	 * 获取用于多方块结构匹配的方向属性
	 *
	 * <p>
	 * 与 {@link #getFacingProperty()} 类似, 但语义上用于结构系统
	 * </p>
	 *
	 * <p>
	 * 主要用于:
	 * </p>
	 * <ul>
	 *     <li>多方块结构旋转</li>
	 *     <li>结构匹配方向对齐</li>
	 * </ul>
	 *
	 * @return 结构使用的方向属性
	 */
	public Property<Direction> getFacingPropertyForStructure() {
		return switch (useFacingType()) {
			case FACING -> BlockStateProperties.FACING;
			case HORIZONTAL -> BlockStateProperties.HORIZONTAL_FACING;
			default -> null;
		};
	}

	/**
	 * 从 Block 实例中提取方向属性(用于结构系统)
	 *
	 * <p>
	 * 若 Block 为 {@link ControllerBlock}, 则返回其方向属性
	 * </p>
	 *
	 * @param block 方块实例
	 * @return 对应方向属性, 否则为 {@code null}
	 */
	public static Property<Direction> getFacingProperty(Block block) {
		if (block instanceof ControllerBlock<?> multiblockControllerBlock) {
			return multiblockControllerBlock.getFacingPropertyForStructure();
		}
		return null;
	}
}