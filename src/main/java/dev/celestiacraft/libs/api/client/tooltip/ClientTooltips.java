package dev.celestiacraft.libs.api.client.tooltip;

import dev.celestiacraft.libs.api.client.context.TooltipContext;
import dev.celestiacraft.libs.api.register.block.BasicBlockItem;
import dev.celestiacraft.libs.api.register.item.BasicItem;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.function.Consumer;

/**
 * {@link BasicItem} / {@link BasicBlockItem} 的客户端 Tooltip 入口.
 *
 * <p>
 * 该类整体标记为 {@link OnlyIn}({@link Dist#CLIENT}), 在专用服务器上不存在.
 * 因此 <b>禁止</b> 在通用代码中直接引用它, 必须通过
 * {@link net.minecraftforge.fml.DistExecutor#unsafeRunWhenOn(Dist, java.util.function.Supplier)}
 * 包一层后再调用, 例如:
 * </p>
 *
 * <pre>{@code
 * DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientTooltips.render(this, stack, level, tooltip, flag));
 * }</pre>
 *
 * <p>
 * 这样专用服务器既不会加载 {@link Minecraft}, 也不会构造 {@link TooltipContext},
 * 从而避免 {@code NoClassDefFoundError} 导致的崩溃.
 * </p>
 */
@OnlyIn(Dist.CLIENT)
public final class ClientTooltips {
	private ClientTooltips() {
	}

	/**
	 * {@link BasicItem} 的客户端 Tooltip 渲染.
	 *
	 * @param item    触发渲染的物品
	 * @param stack   当前物品堆
	 * @param level   当前世界(可能为 {@code null})
	 * @param tooltip 待写入的 Tooltip 行
	 * @param flag    Tooltip 标记
	 */
	public static void render(BasicItem item, ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
		render(stack, level, tooltip, flag, item::addTooltips);
	}

	/**
	 * {@link BasicBlockItem} 的客户端 Tooltip 渲染.
	 *
	 * @param item    触发渲染的物品
	 * @param stack   当前物品堆
	 * @param level   当前世界(可能为 {@code null})
	 * @param tooltip 待写入的 Tooltip 行
	 * @param flag    Tooltip 标记
	 */
	public static void render(BasicBlockItem item, ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
		render(stack, level, tooltip, flag, item::addTooltips);
	}

	/**
	 * 客户端 Tooltip 渲染的公共实现.
	 *
	 * @param stack   当前物品堆
	 * @param level   当前世界(可能为 {@code null})
	 * @param tooltip 待写入的 Tooltip 行
	 * @param flag    Tooltip 标记
	 * @param handler 实际写入 Tooltip 的处理器
	 */
	private static void render(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag, Consumer<TooltipContext> handler) {
		Player player = Minecraft.getInstance().player;

		handler.accept(new TooltipContext(stack, level, tooltip, flag, player));
	}
}
