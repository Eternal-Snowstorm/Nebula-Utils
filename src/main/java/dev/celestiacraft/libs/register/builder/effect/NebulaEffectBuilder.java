package dev.celestiacraft.libs.register.builder.effect;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import dev.celestiacraft.libs.api.register.effect.BasicEffect;
import dev.celestiacraft.libs.register.NebulaRegistrate;
import dev.celestiacraft.libs.register.builder.item.NebulaItemBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.PotionItem;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

/**
 * 通过 {@link NebulaRegistrate} 注册 {@link MobEffect} 的 Registrate builder.
 * <p>
 * 直接继承 {@link AbstractBuilder}, 因此 {@code onRegister}/{@code lang}/{@code tag} 等
 * 链式方法都会返回 {@link NebulaEffectBuilder}, 无需逐个覆写.
 * <p>
 * 用法:
 * <pre>{@code
 * REGISTRATE.effect("my_effect", MyEffect::new)
 *         .category(MobEffectCategory.BENEFICIAL)
 *         .color(0xFF00FF)
 *         .effectTick((entity, amplifier) -> {
 *             ...
 *         })
 *         .durationEffectTick((duration, amplifier) -> {
 *             return duration % 20 == 0;
 *         })
 *         .register();
 * }</pre>
 * 其中 {@code MyEffect::new} 对应构造器 {@code (MobEffectCategory, int)}, 由
 * {@link #category(MobEffectCategory)} 和 {@link #color(int)} 提供参数;
 * {@code effectTick}/{@code durationEffectTick} 需要 effect 是 {@link BasicEffect} (或其子类),
 * 分别接入原版 {@code applyEffectTick} / {@code isDurationEffectTick}.
 */
public class NebulaEffectBuilder<T extends MobEffect, P> extends AbstractBuilder<MobEffect, T, P, NebulaEffectBuilder<T, P>> {
	private final NonNullBiFunction<MobEffectCategory, Integer, T> factory;
	private MobEffectCategory category = MobEffectCategory.NEUTRAL;
	private int color = 0;

	protected NebulaEffectBuilder(
			AbstractRegistrate<?> owner,
			P parent,
			String name,
			BuilderCallback callback,
			NonNullBiFunction<MobEffectCategory, Integer, T> factory
	) {
		super(owner, parent, name, callback, Registries.MOB_EFFECT);
		this.factory = factory;
	}

	public static <T extends MobEffect, P> NebulaEffectBuilder<T, P> create(
			AbstractRegistrate<?> owner,
			P parent,
			String name,
			BuilderCallback callback,
			NonNullBiFunction<MobEffectCategory, Integer, T> factory
	) {
		return new NebulaEffectBuilder<>(owner, parent, name, callback, factory);
	}

	public NebulaEffectBuilder<T, P> category(MobEffectCategory category) {
		this.category = category;
		return this;
	}

	public NebulaEffectBuilder<T, P> color(int color) {
		this.color = color;
		return this;
	}

	public NebulaEffectBuilder<T, P> durationEffectTick(BiPredicate<Integer, Integer> predicate) {
		return onRegister((effect) -> {
			if (effect instanceof BasicEffect basicEffect) {
				basicEffect.addDurationEffectTick(predicate);
			}
		});
	}

	public NebulaEffectBuilder<T, P> effectTick(BiConsumer<LivingEntity, Integer> consumer) {
		return onRegister((effect) -> {
			if (effect instanceof BasicEffect basicEffect) {
				basicEffect.addEffectTick(consumer);
			}
		});
	}

	public NebulaItemBuilder<PotionItem, NebulaEffectBuilder<T, P>> bottleItem() {
		return bottleItem(getName());
	}

	public NebulaItemBuilder<PotionItem, NebulaEffectBuilder<T, P>> bottleItem(@NotNull String name) {
		return ((NebulaRegistrate) getOwner())
				.item(this, name, PotionItem::new)
				.properties((properties) -> {
					return properties.stacksTo(1);
				});
	}

	@Override
	protected @NotNull T createEntry() {
		return factory.apply(category, color);
	}
}