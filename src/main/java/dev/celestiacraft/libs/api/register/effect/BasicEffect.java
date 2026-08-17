package dev.celestiacraft.libs.api.register.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

/**
 * 基础药水效果基类, 配合 {@code NebulaEffectBuilder} 使用.
 * <p>
 * 通过 {@code NebulaEffectBuilder#effectTick} / {@code #durationEffectTick} 注册的回调,
 * 分别接入原版的 {@link #applyEffectTick} 与 {@link #isDurationEffectTick}.
 * <p>
 * 未设置 durationEffectTick 时 {@link #isDurationEffectTick} 默认返回 true (每 tick 生效).
 */
public class BasicEffect extends MobEffect {
	private final List<BiPredicate<Integer, Integer>> durationEffectTickCallbacks = new ArrayList<>();
	private final List<BiConsumer<LivingEntity, Integer>> effectTickCallbacks = new ArrayList<>();

	public BasicEffect(MobEffectCategory category, int color) {
		super(category, color);
	}

	public void addDurationEffectTick(BiPredicate<Integer, Integer> predicate) {
		durationEffectTickCallbacks.add(predicate);
	}

	public void addEffectTick(BiConsumer<LivingEntity, Integer> consumer) {
		effectTickCallbacks.add(consumer);
	}

	@Override
	public boolean isDurationEffectTick(int duration, int amplifier) {
		if (!durationEffectTickCallbacks.isEmpty()) {
			return durationEffectTickCallbacks.stream()
					.allMatch((callback) -> {
						return callback.test(duration, amplifier);
					});
		}
		return true;
	}

	@Override
	public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
		effectTickCallbacks.forEach((callback) -> {
			callback.accept(entity, amplifier);
		});
	}
}