package dev.celestiacraft.libs.api.register.multiblock.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * IO 方块 BlockEntity 基类
 *
 * <p>
 * 作为机器多方块结构中输入 / 输出接口方块的 BE 公共父类,
 * 通过 {@link #supportedControllers()} 的白名单声明该 IO 方块允许服务于哪些 {@link MachineControllerBlockEntity} 主控
 * </p>
 *
 * <p>
 * 核心职责:
 * </p>
 * <ul>
 *     <li>声明该 IO 方块允许接入的主控 BE 类型集合</li>
 *     <li>基于 {@link #supportedControllers()} 提供默认的白名单校验</li>
 *     <li>统一提供常用 capability 的便捷访问方法</li>
 * </ul>
 *
 * <p>
 * 子类通常只需:
 * </p>
 * <ul>
 *     <li>实现 {@link #supportedControllers()}, 以 {@link List#of(Object[])} 枚举可配合使用的主控 BE 类</li>
 *     <li>按需暴露物品 / 流体 / 能量 capability</li>
 * </ul>
 *
 * <pre>{@code
 * public class ItemIOBlockEntity extends IOBlockEntity {
 *     public ItemIOBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
 *         super(type, pos, state);
 *     }
 *
 *     @Override
 *     protected List<Class<? extends MachineControllerBlockEntity>> supportedControllers() {
 *         return List.of(
 *             CrusherControllerBlockEntity.class,
 *             SmelterControllerBlockEntity.class
 *         );
 *     }
 * }
 * }</pre>
 */
public abstract class IOBlockEntity extends BlockEntity {
	protected IOBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	/**
	 * 客户端同步所用的完整 NBT
	 *
	 * <p>
	 * 默认沿用 {@link BlockEntity#saveWithoutMetadata()}, 将全部持久化字段下发给客户端
	 * </p>
	 */
	@Override
	public @NotNull CompoundTag getUpdateTag() {
		return saveWithoutMetadata();
	}

	/**
	 * 客户端同步包
	 *
	 * <p>
	 * 默认使用 {@link ClientboundBlockEntityDataPacket#create(BlockEntity)} 构造,
	 * 与 {@link #getUpdateTag()} 配合完成客户端状态同步
	 * </p>
	 */
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	/**
	 * 获取当前 IO 方块暴露的物品能力
	 *
	 * @return 物品能力, 不存在时返回 {@code null}
	 */
	@Nullable
	public IItemHandler getItemHandler() {
		return getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
	}

	/**
	 * 获取当前 IO 方块暴露的流体能力
	 *
	 * @return 流体能力, 不存在时返回 {@code null}
	 */
	@Nullable
	public IFluidHandler getFluidHandler() {
		return getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);
	}

	/**
	 * 获取当前 IO 方块暴露的能量能力
	 *
	 * @return 能量能力, 不存在时返回 {@code null}
	 */
	@Nullable
	public IEnergyStorage getEnergyStorage() {
		return getCapability(ForgeCapabilities.ENERGY).orElse(null);
	}

	/**
	 * 当前 IO 方块是否暴露了物品能力
	 */
	public boolean hasItemHandler() {
		return getItemHandler() != null;
	}

	/**
	 * 当前 IO 方块是否暴露了流体能力
	 */
	public boolean hasFluidHandler() {
		return getFluidHandler() != null;
	}

	/**
	 * 当前 IO 方块是否暴露了能量能力
	 */
	public boolean hasEnergyStorage() {
		return getEnergyStorage() != null;
	}

	/**
	 * 声明当前 IO 方块允许接入的主控类型
	 *
	 * <p>
	 * 子类以 {@link List#of(Object[])} 枚举所有可配合使用的 {@link MachineControllerBlockEntity} 子类
	 * </p>
	 *
	 * <p>
	 * 约定:
	 * </p>
	 * <ul>
	 *     <li>空列表表示不允许接入任何主控</li>
	 *     <li>若需要允许所有机器主控, 可返回 {@code List.of(MachineControllerBlockEntity.class)}</li>
	 * </ul>
	 *
	 * @return 允许接入的主控 BE 类列表
	 */
	@NotNull
	protected abstract List<Class<? extends MachineControllerBlockEntity>> supportedControllers();

	/**
	 * 判断给定主控 BE 是否被当前 IO 方块接受
	 *
	 * <p>
	 * 默认基于 {@link #supportedControllers()} 的白名单做 {@link Class#isInstance(Object)} 校验,
	 * 命中其中任一类型即视为允许接入
	 * </p>
	 *
	 * @param entity 待校验的主控 BE, 允许为 {@code null}
	 * @return 命中白名单任一类型返回 {@code true}, 否则返回 {@code false}
	 */
	public boolean isControllerAllowed(@Nullable MachineControllerBlockEntity entity) {
		if (entity == null) {
			return false;
		}

		for (Class<? extends MachineControllerBlockEntity> type : supportedControllers()) {
			if (type.isInstance(entity)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断任意 {@link BlockEntity} 是否为当前 IO 方块可接受的主控
	 *
	 * <p>
	 * 便利重载: 先做 {@code instanceof} 类型检查, 命中后委托给
	 * {@link #isControllerAllowed(MachineControllerBlockEntity)}
	 * </p>
	 *
	 * @param entity 待校验的 BE, 允许为 {@code null} 或非主控类型
	 * @return 为允许的主控返回 {@code true}, 否则返回 {@code false}
	 */
	public boolean isControllerAllowed(@Nullable BlockEntity entity) {
		return entity instanceof MachineControllerBlockEntity controller && isControllerAllowed(controller);
	}
}
