package dev.celestiacraft.libs.api.register.block;

import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * 一个简单的封装方块类
 * <p>
 * 该类同时继承着 {@link BasicBlock}, 因此不需要顾忌
 * <p>
 * 主要是封装实现 {@link IEntityBlock}
 */
public abstract class BasicEntityBlock<T extends BlockEntity> extends BasicBlock implements IEntityBlock<T> {
	public BasicEntityBlock(Properties properties) {
		super(properties);
	}
}