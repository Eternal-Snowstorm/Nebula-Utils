package dev.celestiacraft.libs.api.register.tool.tier;

import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TierBuilder {
	@Getter
	private ResourceLocation name;
	@Getter
	private int level;
	@Getter
	private int uses;
	@Getter
	private float speed;
	@Getter
	private float attackSpeedModifier;
	@Getter
	private float attackDamageBonus;
	@Getter
	private int atackDamageModifier;
	@Getter
	private int enchantmentValue;
	@Getter
	private TagKey<Block> mineableTag;
	@Getter
	private Supplier<Ingredient> repairIngredient;
	private final List<Tier> after = new ArrayList<>();
	private final List<Tier> before = new ArrayList<>();

	public static Tier create(Consumer<TierBuilder> consumer) {
		TierBuilder builder = new TierBuilder();
		Objects.requireNonNull(consumer, "consumer").accept(builder);
		return builder.build();
	}

	public static Tier create(ResourceLocation name, Consumer<TierBuilder> consumer) {
		return create((builder) -> {
			builder.setName(name);
			consumer.accept(builder);
		});
	}

	public TierBuilder setName(ResourceLocation name) {
		this.name = name;
		return this;
	}

	public TierBuilder setLevel(int level) {
		this.level = level;
		return this;
	}

	public TierBuilder setUses(int uses) {
		this.uses = uses;
		return this;
	}

	public TierBuilder setSpeed(float speed) {
		this.speed = speed;
		return this;
	}

	public TierBuilder setSpeedModifier(float modifier) {
		this.attackSpeedModifier = modifier;
		return this;
	}

	public TierBuilder setDamage(float attackDamageBonus) {
		return setAttackDamageBonus(attackDamageBonus);
	}

	public TierBuilder setAttackDamageBonus(float attackDamageBonus) {
		this.attackDamageBonus = attackDamageBonus;
		return this;
	}

	public TierBuilder setDamageModifier(int modifier) {
		this.atackDamageModifier = modifier;
		return this;
	}

	public TierBuilder setEnchantmentValue(int enchantmentValue) {
		this.enchantmentValue = enchantmentValue;
		return this;
	}

	public TierBuilder setMineableTag(TagKey<Block> mineableTag) {
		this.mineableTag = mineableTag;
		return this;
	}

	public TierBuilder setRepairTag(TagKey<Item> tag) {
		return setRepair(() -> Ingredient.of(tag));
	}

	public TierBuilder setRepairIngredient(Ingredient ingredient) {
		return setRepair(() -> ingredient);
	}

	public TierBuilder setRepair(Supplier<Ingredient> supplier) {
		this.repairIngredient = supplier;
		return this;
	}

	public TierBuilder setAfter(Tier... tiers) {
		return setAfter(Arrays.asList(tiers));
	}

	public TierBuilder setAfter(Collection<Tier> tiers) {
		after.clear();
		after.addAll(tiers);
		return this;
	}

	public TierBuilder setBefore(Tier... tiers) {
		return setBefore(Arrays.asList(tiers));
	}

	public TierBuilder setBefore(Collection<Tier> tiers) {
		before.clear();
		before.addAll(tiers);
		return this;
	}

	private Tier build() {
		return TierSortingRegistry.registerTier(
				new ForgeTier(
						level,
						uses,
						speed,
						attackDamageBonus,
						enchantmentValue,
						required(mineableTag, "mineableTag"),
						required(repairIngredient, "repairIngredient")
				),
				required(name, "name"),
				List.copyOf(after),
				List.copyOf(before)
		);
	}

	private static <T> T required(T value, String fieldName) {
		return Objects.requireNonNull(value, "Tool tier " + fieldName + " must be set.");
	}
}