package dev.celestiacraft.libs.api.register.multiblock.machine;

import com.simibubi.create.foundation.block.IBE;
import dev.celestiacraft.libs.api.register.multiblock.ControllerBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public interface IControllerRecipe<T extends ControllerBlockEntity> {
	/**
	 * 返回该控制器对应的配方类型 ID
	 *
	 * <p>
	 * 这个 ID 主要用于:
	 * </p>
	 * <ul>
	 *     <li>显示或记录当前机器对应的配方类型</li>
	 *     <li>在已经注册 RecipeType 时解析对应的 {@link RecipeType}</li>
	 * </ul>
	 *
	 * <p>
	 * 即使当前配方类型尚未注册, 也应该返回稳定的 ID
	 * </p>
	 */
	ResourceLocation getRecipeTypeId();

	/**
	 * 解析当前控制器的配方类型
	 *
	 * <p>
	 * 若该 ID 尚未注册到 {@link ForgeRegistries#RECIPE_TYPES},
	 * 则返回 {@code null}
	 * </p>
	 */
	@Nullable
	default RecipeType<?> getRecipeType() {
		return ForgeRegistries.RECIPE_TYPES.getValue(getRecipeTypeId());
	}

	/**
	 * 执行多方块配方逻辑
	 *
	 * <p>
	 * 当多方块结构和IO验证通过后, 由系统在服务端周期性调用,
	 * 用于驱动机器的核心运行逻辑
	 * </p>
	 *
	 * <p>
	 * 调用特性:
	 * </p>
	 * <ul>
	 *     <li>通常每 tick 调用一次(取决于外部调度)</li>
	 *     <li>仅在结构有效时执行</li>
	 *     <li>客户端不应执行任何逻辑</li>
	 * </ul>
	 *
	 * <p>
	 * 上下文内容:
	 * </p>
	 * <ul>
	 *     <li>{@link MultiblockContext#getEntity()} 当前控制器实例</li>
	 *     <li>{@link MultiblockContext#getLevel()} 所在世界</li>
	 *     <li>{@link MultiblockContext#getStack()} 当前处理的物品(可能为空)</li>
	 *     <li>{@link MultiblockContext#getPos()} 当前处理位置(通常为控制器或核心位置)</li>
	 *     <li>{@link MultiblockContext#getState()} 对应方块状态</li>
	 * </ul>
	 *
	 * <p>
	 * 常见用途:
	 * </p>
	 * <ul>
	 *     <li>检测是否满足配方条件</li>
	 * </ul>
	 *
	 * <p>
	 * 实现建议:
	 * </p>
	 * <ul>
	 *     <li>始终首先判断 {@link MultiblockContext#isClient()}</li>
	 * </ul>
	 *
	 * <p>
	 * 注意事项:
	 * </p>
	 * <ul>
	 *     <li>在重写该方法后还需要在本类下重写 {@link #tick(MultiblockContext)} 方法, 详见 {@link #tick(MultiblockContext)}</p></li>
	 *     <li>在重写该方法后还需要在 Block 类下重写 {@link IBE#getTicker(Level, BlockState, BlockEntityType)} 方法调用本类下的 {@link #tick(MultiblockContext)} 方法</li>
	 * </ul>
	 *
	 * <pre>{@code
	 * @Override
	 * protected void recipe(MultiblockContext<T> context) {
	 * }
	 * }</pre>
	 *
	 * @param context 多方块运行上下文, 提供当前 tick 的所有执行信息
	 */
	void recipe(MultiblockContext<T> context);

	/**
	 *
	 * @param context 多方块运行上下文, 提供当前 tick 的所有执行信
	 * @return
	 */
	MultiblockContext<T> tick(MultiblockContext<T> context);
}