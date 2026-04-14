package dev.celestiacraft.libs.api.register.multiblock;

import dev.celestiacraft.libs.api.register.multiblock.machine.IControllerRecipe;
import dev.celestiacraft.libs.api.register.multiblock.machine.MultiblockContext;
import dev.celestiacraft.libs.compat.patchouli.multiblock.IMultiblockProvider;
import dev.celestiacraft.libs.compat.patchouli.multiblock.MultiblockHandler;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import vazkii.patchouli.api.IMultiblock;

import java.util.function.Supplier;

/**
 * 多方块控制器 BlockEntity 基类
 *
 * <p>
 * 作为多方块系统的核心运行单元, 负责结构逻辑与客户端显示的统一管理
 * </p>
 *
 * <p>
 * 核心职责:
 * </p>
 * <ul>
 *     <li>封装 {@link MultiblockHandler} 的创建与生命周期</li>
 *     <li>提供结构匹配与缓存机制</li>
 *     <li>管理客户端多方块结构的显示与取消</li>
 *     <li>提供结构渲染偏移与性能参数配置</li>
 * </ul>
 *
 * <p>
 * 内部实现:
 * </p>
 * <ul>
 *     <li>通过 {@link MultiblockHandler#builder} 构建处理器</li>
 *     <li>自动应用翻译键, 渲染偏移与缓存策略</li>
 *     <li>在 {@link #setRemoved()} 时自动清理客户端显示状态</li>
 * </ul>
 *
 * <p>
 * 扩展点:
 * </p>
 * <ul>
 *     <li>{@link #getMultiblockKey()}: 定义结构翻译键(必须实现)</li>
 *     <li>{@link #getRenderOffsetX()} / Y / Z: 控制结构显示偏移</li>
 *     <li>{@link #getCacheTicks()}: 控制结构验证缓存周期</li>
 * </ul>
 *
 * <p>
 * 子类通常只需:
 * </p>
 * <ul>
 *     <li>提供 {@link IMultiblock} 结构</li>
 *     <li>实现 {@link #getModId()}</li>
 *     <li>实现 {@link #getMultiblockName()}</li>
 *     <li>根据需要调整渲染偏移</li>
 *     <li>若需要配方逻辑还需实现 {@link IControllerRecipe#recipe(MultiblockContext)} 和 {@link IControllerRecipe#tick(MultiblockContext)}</li>
 * </ul>
 *
 * <p>
 * 无需关心结构缓存, 显示管理或 Handler 初始化逻辑
 * </p>
 *
 * <pre>{@code
 * public class ExampleBlockEntity extends MultiblockControllerBlockEntity {
 *     public ExampleBlockEntity(...) {
 *         super(type, pos, state, NebulaMultiblock.EXAMPLE);
 *     }
 *
 *     @Override
 *     protected String getModId() {
 *         return NebulaLibs.MODID;
 *     }
 *
 *     @Override
 *     protected String getMultiblockName() {
 *         return "example_multiblock";
 *     }
 * }
 * }</pre>
 */
public abstract class ControllerBlockEntity extends BlockEntity implements IMultiblockProvider {
	@Getter
	@Setter
	protected int workTime;

	/**
	 * 多方块处理器
	 *
	 * <p>
	 * 多方块系统的核心执行单元, 封装所有结构相关逻辑
	 * </p>
	 *
	 * <p>
	 * 主要负责:
	 * </p>
	 * <ul>
	 *     <li>结构匹配与验证</li>
	 *     <li>验证结果缓存(降低性能开销)</li>
	 *     <li>客户端结构预览的显示与隐藏</li>
	 * </ul>
	 *
	 * <p>
	 * 该实例在构造时创建, 并在整个 BlockEntity 生命周期内复用
	 * </p>
	 */
	protected final MultiblockHandler multiblock;

	/**
	 * 构造方法
	 *
	 * <p>
	 * 自动初始化 {@link MultiblockHandler}, 并应用所有结构相关配置
	 * </p>
	 *
	 * <p>
	 * 初始化流程:
	 * </p>
	 * <ul>
	 *     <li>绑定当前 BlockEntity</li>
	 *     <li>设置结构提供器({@link IMultiblock})</li>
	 *     <li>应用翻译键({@link #getMultiblockKey()})</li>
	 *     <li>应用渲染偏移({@link #getRenderOffsetX()} / Y / Z)</li>
	 *     <li>应用缓存策略({@link #getCacheTicks()})</li>
	 * </ul>
	 *
	 * <p>
	 * 子类无需手动创建或管理 {@link MultiblockHandler}
	 * </p>
	 *
	 * @param type      BlockEntity 类型
	 * @param pos       方块位置
	 * @param state     方块状态
	 * @param structure 多方块结构提供器
	 */
	protected ControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Supplier<IMultiblock> structure) {
		super(type, pos, state);

		this.multiblock = MultiblockHandler.builder(this, structure)
				.translationKey(getMultiblockKey())
				.renderOffset(getRenderOffsetX(), getRenderOffsetY(), getRenderOffsetZ())
				.cacheTicks(getCacheTicks())
				.build();
	}

	/**
	 * 获取多方块处理器实例
	 *
	 * <p>
	 * 用于访问结构匹配, 缓存与显示相关功能
	 * </p>
	 *
	 * <p>
	 * 通常不建议重写该方法
	 * </p>
	 *
	 * @return 当前绑定的 {@link MultiblockHandler}
	 */
	@Override
	public MultiblockHandler getMultiblockHandler() {
		return multiblock;
	}


	/**
	 * BlockEntity 被移除时调用
	 *
	 * <p>
	 * 自动清理客户端多方块结构预览,
	 * 防止结构显示在方块移除后仍然残留
	 * </p>
	 *
	 * <p>
	 * 若子类重写该方法, 必须调用 {@code super.setRemoved()}
	 * </p>
	 */
	@Override
	public void setRemoved() {
		cancelShowMultiblock();
		super.setRemoved();
	}

	/**
	 * 获取该结构所属的 modid
	 *
	 * @return modid（如 "cmi"）
	 */
	protected abstract String getModId();

	/**
	 * 获取结构名称（用于拼接翻译键）
	 *
	 * <p>
	 * 通常使用简短标识名, 例如 "water_pump"
	 * </p>
	 *
	 * @return 结构名称
	 */
	protected abstract String getMultiblockName();

	/**
	 * 获取结构分类
	 *
	 * <p>
	 * 用于拼接翻译键中的分类字段, 默认值为 "building"
	 * </p>
	 *
	 * <p>
	 * 子类可按需重写, 例如 "machine", "structure"
	 * </p>
	 *
	 * @return 结构分类
	 */
	protected String getMultiblockCategory() {
		return "building";
	}

	/**
	 * 获取多方块结构的翻译键
	 *
	 * <p>
	 * 默认格式为：
	 * <pre>{@code
	 * multiblock.{category}.{modid}.{name}
	 * }</pre>
	 * </p>
	 *
	 * <p>
	 * 一般情况下无需重写, 只需实现 {@link #getModId()} 和 {@link #getMultiblockName()}
	 * </p>
	 *
	 * <p>
	 * 若需要自定义翻译键格式, 可在子类中重写该方法
	 * </p>
	 *
	 * @return 结构对应的翻译键
	 */
	protected String getMultiblockKey() {
		return String.format(
				"multiblock.%s.%s.%s",
				getMultiblockCategory(),
				getModId(),
				getMultiblockName()
		);
	}

	/**
	 * 获取结构渲染偏移(X 轴)
	 *
	 * <p>
	 * 用于调整结构预览在 X 方向的显示位置
	 * </p>
	 *
	 * <p>
	 * 默认返回 {@code 0}
	 * </p>
	 */
	protected int getRenderOffsetX() {
		return 0;
	}

	/**
	 * 获取结构渲染偏移(Y 轴)
	 *
	 * <p>
	 * 常用于:
	 * </p>
	 * <ul>
	 *     <li>控制器不在结构底部时的对齐修正</li>
	 *     <li>整体结构上移或下移</li>
	 * </ul>
	 *
	 * <p>
	 * 默认返回 {@code -1}
	 * </p>
	 *
	 * <pre>{@code
	 * @Override
	 * protected int getRenderOffsetY() {
	 *     return -1;
	 * }
	 * }</pre>
	 */
	protected int getRenderOffsetY() {
		return -1;
	}

	/**
	 * 获取结构渲染偏移(Z 轴)
	 *
	 * <p>
	 * 用于调整结构预览在 Z 方向的显示位置
	 * </p>
	 *
	 * <p>
	 * 默认返回 {@code 0}
	 * </p>
	 */
	protected int getRenderOffsetZ() {
		return 0;
	}

	/**
	 * 获取结构验证缓存周期(tick)
	 *
	 * <p>
	 * 控制多方块结构匹配结果的缓存时间
	 * </p>
	 *
	 * <p>
	 * 行为说明:
	 * </p>
	 * <ul>
	 *     <li>值越大: 性能越好, 但结构更新延迟更高</li>
	 *     <li>值越小: 响应更及时, 但可能增加性能开销</li>
	 * </ul>
	 *
	 * <p>
	 * 默认值: {@code 20}(1 秒)
	 * </p>
	 *
	 * <pre>{@code
	 * @Override
	 * protected int getCacheTicks() {
	 *     return 10;
	 * }
	 * }</pre>
	 *
	 * @return 缓存周期(tick)
	 */
	protected int getCacheTicks() {
		return 20;
	}
}
