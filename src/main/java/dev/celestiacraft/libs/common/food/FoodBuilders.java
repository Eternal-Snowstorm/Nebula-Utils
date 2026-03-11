package dev.celestiacraft.libs.common.food;

import lombok.Getter;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 一个用于构建 {@link FoodProperties} 的链式构建器。
 *
 * <p>该类提供类似 KubeJS 的 FoodBuilder 风格 API，用于简化食物属性的定义。
 * 通过链式调用可以设置饥饿值、饱和度、食物效果以及食用后的回调逻辑。</p>
 *
 * <p>该类实现了 {@link Supplier} 接口，因此可以直接作为 Supplier 传入
 * {@code Item.Properties.food(...)}，也可以手动调用 {@link #build()} 获取
 * {@link FoodProperties} 实例。</p>
 *
 * <h2>基本用法</h2>
 *
 * <pre>{@code
 * public static final FoodBuilders PIG_IRON = FoodBuilders.food((builder) -> {
 *     builder.hunger(8)
 *            .saturation(1.0f)
 *            .alwaysEdible();
 * });
 * }</pre>
 *
 * <h2>在 Item 中使用</h2>
 *
 * <pre>{@code
 * new Item(new Item.Properties().food(PIG_IRON.get()));
 * }</pre>
 * <p>
 * 或者
 *
 * <pre>{@code
 * new Item(new Item.Properties().food(PIG_IRON.build()));
 * }</pre>
 *
 * <h2>添加效果</h2>
 *
 * <pre>{@code
 * FoodBuilders.food((builder) -> {
 *     builder.hunger(6)
 *            .saturation(0.8f)
 *            .effect(MobEffects.REGENERATION, 200, 1, 1.0f);
 * });
 * }</pre>
 *
 * <h2>食用回调</h2>
 *
 * <p>可以通过 {@link #eaten(Consumer)} 定义食物被食用时的回调逻辑。
 * 该回调不会自动触发，需要在 Item 中调用对应逻辑，例如在
 * {@code finishUsingItem()} 中手动触发。</p>
 *
 * <pre>{@code
 * FoodBuilders.food((builder) -> {
 *     builder.hunger(8)
 *            .saturation(1)
 *            .eaten((event) -> {
 *                Player player = event.getPlayer();
 *                ItemStack stack = event.getStack();
 *
 *                if (!player.isCreative()) {
 *                    stack.grow(1);
 *                }
 *            });
 * });
 * }</pre>
 *
 * <h2>设计目标</h2>
 *
 * <ul>
 * <li>提供类似脚本语言的食物定义体验</li>
 * <li>保持 Forge / NeoForge 原生 {@link FoodProperties} 兼容</li>
 * <li>支持链式 API 和可读性较高的定义方式</li>
 * </ul>
 * <h2>在 finishUsingItem 中触发 eaten 回调</h2>
 *
 * <p>由于 {@link FoodProperties} 本身不支持食用回调逻辑，因此需要在
 * {@code Item#finishUsingItem} 中手动触发 {@link #eaten(Consumer)}。</p>
 *
 * <pre>{@code
 * @Override
 * public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
 *     ItemStack result = super.finishUsingItem(stack, level, entity);
 *
 *     if (!level.isClientSide() && entity instanceof Player player) {
 *         FoodBuilders food = CmiFoodBuilder.PIG_IRON;
 *
 *         if (food.getEaten() != null) {
 *             food.getEaten().accept(new FoodEatenEvent(player, stack, level));
 *         }
 *     }
 *
 *     return result;
 * }
 * }</pre>
 *
 * <p>这样即可实现类似 KubeJS 中 {@code eaten()} 的行为。</p>
 */
public class FoodBuilders implements Supplier<FoodProperties> {
	private final FoodProperties.Builder builder = new FoodProperties.Builder();
	private final List<EffectEntry> effects = new ArrayList<>();
	@Getter
	private Consumer<FoodEatenEvent> eaten;

	public static FoodBuilders food(Consumer<FoodBuilders> consumer) {
		FoodBuilders builder = new FoodBuilders();
		consumer.accept(builder);
		return builder;
	}

	public FoodBuilders hunger(int hunger) {
		builder.nutrition(hunger);
		return this;
	}

	public FoodBuilders saturation(float saturation) {
		builder.saturationMod(saturation);
		return this;
	}

	public FoodBuilders alwaysEdible() {
		builder.alwaysEat();
		return this;
	}

	public FoodBuilders alwaysEdible(boolean flag) {
		if (flag) builder.alwaysEat();
		return this;
	}

	public FoodBuilders meat() {
		builder.meat();
		return this;
	}

	public FoodBuilders meat(boolean flag) {
		if (flag) {
			builder.meat();
		}
		return this;
	}

	public FoodBuilders fastToEat() {
		builder.fast();
		return this;
	}

	public FoodBuilders fastToEat(boolean flag) {
		if (flag) {
			builder.fast();
		}
		return this;
	}

	public FoodBuilders effect(MobEffect effect, int duration, int amplifier, float probability) {
		effects.add(new EffectEntry(effect, duration, amplifier, probability));
		return this;
	}

	public FoodBuilders removeEffect(MobEffect effect) {
		effects.removeIf((entry) -> {
			return entry.effect == effect;
		});
		return this;
	}

	public FoodBuilders eaten(Consumer<FoodEatenEvent> callback) {
		this.eaten = callback;
		return this;
	}

	public FoodProperties build() {
		for (EffectEntry effect : effects) {
			builder.effect(() -> {
				return new MobEffectInstance(
						effect.effect,
						effect.duration,
						effect.amplifier
				);
			}, effect.probability);
		}
		return builder.build();
	}

	@Override
	public FoodProperties get() {
		return build();
	}

	private record EffectEntry(
			MobEffect effect,
			int duration,
			int amplifier,
			float probability
	) {
	}
}