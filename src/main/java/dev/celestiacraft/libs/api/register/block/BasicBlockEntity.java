package dev.celestiacraft.libs.api.register.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * 基础 BlockEntity 实现.
 *
 * <p>
 * 该类统一封装常见生命周期, NBT 读写, 客户端同步和 capability 生命周期入口.
 * 子类通常只需要重写 {@link #write(CompoundTag)}, {@link #read(CompoundTag)}
 * 以及按需重写 {@link #writeSync(CompoundTag)} / {@link #readSync(CompoundTag)}.
 * </p>
 *
 * <p>
 * 如果子类需要处理 Forge capability, 推荐重写 {@link #onCapsRevived()} 和
 * {@link #onCapsInvalidated()}, 而不是直接重写 {@link #reviveCaps()} 或
 * {@link #invalidateCaps()}.
 * </p>
 */
public abstract class BasicBlockEntity extends BlockEntity {
	/**
	 * 创建基础 BlockEntity.
	 *
	 * @param type  BlockEntity 类型
	 * @param pos   方块位置
	 * @param state 方块状态
	 */
	public BasicBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		onLoaded();
		onCapsRevived();
	}

	@Override
	public void reviveCaps() {
		super.reviveCaps();
		onCapsRevived();
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();
		onCapsInvalidated();
	}

	@Override
	protected void saveAdditional(@NotNull CompoundTag tag) {
		super.saveAdditional(tag);
		write(tag);
	}

	@Override
	public void load(@NotNull CompoundTag tag) {
		super.load(tag);
		read(tag);
	}

	@Override
	public @NotNull CompoundTag getUpdateTag() {
		CompoundTag tag = new CompoundTag();
		super.saveAdditional(tag);
		writeSync(tag);
		return tag;
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void handleUpdateTag(CompoundTag tag) {
		readSync(tag);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
		readSync(packet.getTag());
	}

	/**
	 * 判断当前 BlockEntity 是否已经绑定 Level.
	 *
	 * @return 已绑定 Level 时返回 true
	 */
	protected boolean isLevelNotNull() {
		return level != null;
	}

	/**
	 * 判断当前逻辑是否运行在客户端.
	 *
	 * @return 已绑定 Level 且位于客户端时返回 true
	 */
	protected boolean isClient() {
		return isLevelNotNull() && level.isClientSide();
	}

	/**
	 * 判断当前逻辑是否运行在服务端.
	 *
	 * @return 已绑定 Level 且位于服务端时返回 true
	 */
	protected boolean isServer() {
		return isLevelNotNull() && !level.isClientSide();
	}

	/**
	 * 获取当前方块状态.
	 *
	 * @return 当前方块状态
	 */
	protected BlockState getState() {
		return getBlockState();
	}

	/**
	 * 标记当前 BlockEntity 数据已经改变.
	 *
	 * <p>
	 * 该方法只负责保存标记, 不会主动向客户端同步数据.
	 * </p>
	 */
	protected void markDirty() {
		setChanged();
	}

	/**
	 * 标记数据改变并通知客户端刷新方块.
	 *
	 * <p>
	 * 通常用于服务端状态改变后需要立即同步到客户端显示的场景.
	 * </p>
	 */
	protected void markDirtyAndUpdate() {
		setChanged();

		if (isLevelNotNull()) {
			BlockState state = getBlockState();
			level.sendBlockUpdated(worldPosition, state, state, 3);
		}
	}

	/**
	 * 通知客户端刷新当前方块.
	 *
	 * <p>
	 * 该方法不会调用 {@link #setChanged()}, 只负责发送方块更新.
	 * </p>
	 */
	protected void updateBlock() {
		if (isLevelNotNull() && level.isClientSide()) {
			BlockState state = getBlockState();
			level.sendBlockUpdated(worldPosition, state, state, 3);
		}
	}

	/**
	 * 向客户端同步当前 BlockEntity 数据.
	 *
	 * <p>
	 * 在 {@link #updateBlock()} 的基础上增加了{@link #setChanged()} .
	 * </p>
	 */
	protected void sync() {
		updateBlock();
		setChanged();
	}

	/**
	 * 写入服务端持久化 NBT.
	 *
	 * <p>
	 * 由 {@link #saveAdditional(CompoundTag)} 自动调用. 子类需要保存数据时重写该方法.
	 * </p>
	 *
	 * @param tag 目标 NBT
	 */
	protected void write(CompoundTag tag) {
	}

	/**
	 * 读取服务端持久化 NBT.
	 *
	 * <p>
	 * 由 {@link #load(CompoundTag)} 自动调用. 子类需要读取数据时重写该方法.
	 * </p>
	 *
	 * @param tag 来源 NBT
	 */
	protected void read(CompoundTag tag) {
	}

	/**
	 * 写入发送到客户端的同步 NBT.
	 *
	 * <p>
	 * 默认复用 {@link #write(CompoundTag)}. 如果存在只应保存在服务端的数据,
	 * 可以重写该方法并只写入客户端需要的数据.
	 * </p>
	 *
	 * @param tag 目标 NBT
	 */
	protected void writeSync(CompoundTag tag) {
		write(tag);
	}

	/**
	 * 读取客户端收到的同步 NBT.
	 *
	 * <p>
	 * 默认复用 {@link #read(CompoundTag)}. 如果 {@link #writeSync(CompoundTag)}
	 * 和 {@link #write(CompoundTag)} 写入的数据不同, 应同步重写该方法.
	 * </p>
	 *
	 * @param tag 来源 NBT
	 */
	protected void readSync(CompoundTag tag) {
		read(tag);
	}

	/**
	 * BlockEntity 加载完成后的扩展入口.
	 *
	 * <p>
	 * 由 {@link #onLoad()} 自动调用.
	 * </p>
	 */
	protected void onLoaded() {
	}

	/**
	 * capability 被创建或恢复后的扩展入口.
	 *
	 * <p>
	 * 由 {@link #onLoad()} 和 {@link #reviveCaps()} 自动调用.
	 * </p>
	 *
	 * <p>
	 * 示例:
	 * <pre>{@code
	 * @Override
	 * protected void onCapsRevived() {
	 *     super.onCapsRevived();
	 *     itemCapability = LazyOptional.of(() -> itemHandler);
	 *     fluidCapability = LazyOptional.empty();
	 * }
	 * }</pre>
	 */
	protected void onCapsRevived() {
	}

	/**
	 * capability 失效时的扩展入口.
	 *
	 * <p>
	 * 由 {@link #invalidateCaps()} 自动调用.
	 * </p>
	 *
	 * <p>
	 * 示例:
	 * <pre>{@code
	 * @Override
	 * protected void onCapsInvalidated() {
	 *     super.onCapsInvalidated();
	 *     itemCapability.invalidate();
	 *     fluidCapability.invalidate();
	 * }
	 * }</pre>
	 */
	protected void onCapsInvalidated() {
	}
}