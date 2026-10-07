package dev.celestiacraft.libs.server.tick;

import dev.celestiacraft.libs.NebulaLibs;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 服务器 Tick 延迟任务调度器
 *
 * <p>
 * 该工具用于在指定 Tick 数之后执行任务, 常用于:
 * </p>
 *
 * <ul>
 *   <li>延迟执行逻辑</li>
 *   <li>等待方块状态更新</li>
 *   <li>等待实体生成</li>
 *   <li>模拟脚本中的 scheduleInTicks 行为</li>
 * </ul>
 *
 * <p>
 * Tick 时间参考:
 * </p>
 *
 * <ul>
 *   <li>20 ticks = 1 秒</li>
 *   <li>100 ticks = 5 秒</li>
 *   <li>1200 ticks = 1 分钟</li>
 * </ul>
 *
 * <p>
 * 调度器支持三种任务类型:
 * </p>
 *
 * <ul>
 *   <li>普通任务 Runnable</li>
 *   <li>服务器任务 MinecraftServer</li>
 *   <li>玩家任务 ServerPlayer</li>
 * </ul>
 *
 * <p>
 * 使用示例:
 * </p>
 *
 * <pre>{@code
 * // 5 秒后执行
 * TickScheduler.schedule(100, () -> {
 *     System.out.println("Hello after 5 seconds");
 * });
 *
 * // 2 秒后执行服务器任务
 * TickScheduler.scheduleServer(server, 40, (s) -> {
 *     s.getPlayerList().broadcastSystemMessage(
 *         Component.literal("Delayed message"),
 *         false
 *     );
 * });
 *
 * // 3 秒后执行玩家任务
 * TickScheduler.schedulePlayer(player, 60, (p) -> {
 *     p.sendSystemMessage(Component.literal("Delayed player message"));
 * });
 * }</pre>
 *
 * <p>
 * 注意:
 * </p>
 *
 * <ul>
 *   <li>任务在 ServerTickEvent 中执行</li>
 *   <li>任务只会执行一次</li>
 *   <li>Tick 倒计时每 Tick 减 1</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = NebulaLibs.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TickScheduler {
	/**
	 * 当前等待执行的 Tick 任务列表.
	 */
	private static final List<TickTask> TASKS = new LinkedList<>();

	/**
	 * 注册一个普通延迟任务.
	 *
	 * <p>
	 * 该任务将在指定 Tick 数之后执行.
	 * </p>
	 *
	 * <pre>{@code
	 * TickScheduler.schedule(100, () -> {
	 *     System.out.println("5 秒后执行");
	 * });
	 * }</pre>
	 *
	 * @param ticks 延迟 Tick 数
	 * @param task  要执行的任务
	 *
	 */
	public static void schedule(int ticks, Runnable task) {
		TASKS.add(new RunnableTask(ticks, task));
	}

	/**
	 * 注册一个服务器延迟任务.
	 *
	 * <p>
	 * 任务执行时会传入 MinecraftServer 实例.
	 * </p>
	 *
	 * <pre>{@code
	 * TickScheduler.scheduleServer(server, 40, s -> {
	 *     System.out.println("2 秒后执行服务器逻辑");
	 * });
	 * }</pre>
	 *
	 * @param server 服务器实例
	 * @param ticks  延迟 Tick 数
	 * @param task   任务逻辑
	 *
	 */
	public static void scheduleServer(MinecraftServer server, int ticks, Consumer<MinecraftServer> task) {
		TASKS.add(new ServerTask(server, ticks, task));
	}

	/**
	 * 注册一个玩家延迟任务.
	 *
	 * <p>
	 * 任务执行时会传入 ServerPlayer 实例.
	 * </p>
	 * <pre>{@code
	 * TickScheduler.schedulePlayer(player, 60, p -> {
	 *     p.sendSystemMessage(Component.literal("3 秒后执行"));
	 * });
	 * }</pre>
	 *
	 * @param player 玩家
	 * @param ticks  延迟 Tick 数
	 * @param task   玩家任务
	 *
	 */
	public static void schedulePlayer(ServerPlayer player, int ticks, Consumer<ServerPlayer> task) {
		TASKS.add(new PlayerTask(player, ticks, task));
	}

	/**
	 * 服务器 Tick 事件处理器.
	 *
	 * <p>
	 * 每个服务器 Tick 执行一次:
	 * </p>
	 *
	 * <ul>
	 *   <li>遍历所有任务</li>
	 *   <li>减少 Tick 计数</li>
	 *   <li>当 Tick 为 0 时执行任务</li>
	 *   <li>执行后移除任务</li>
	 * </ul>
	 *
	 * @param event Forge ServerTickEvent
	 */
	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.END) {
			return;
		}

		Iterator<TickTask> iterator = TASKS.iterator();

		while (iterator.hasNext()) {
			TickTask task = iterator.next();

			task.ticks--;

			if (task.ticks <= 0) {
				task.run();
				iterator.remove();
			}
		}
	}
}