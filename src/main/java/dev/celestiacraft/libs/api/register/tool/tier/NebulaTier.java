package dev.celestiacraft.libs.api.register.tool.tier;

import lombok.Getter;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

@Getter
public class NebulaTier implements Tier {
	private final int level;
	private final int uses;
	private final float speed;
	private final float attackDamageBonus;
	private final int enchantmentValue;

	private final int attackDamageModifier;
	private final float attackSpeedModifier;

	private final TagKey<Block> mineableTag;
	private final Supplier<Ingredient> repairIngredient;

	public NebulaTier(
			int level,
			int uses,
			float speed,
			float attackDamageBonus,
			int enchantmentValue,
			int attackDamageModifier,
			float attackSpeedModifier,
			TagKey<Block> mineableTag,
			Supplier<Ingredient> repairIngredient
	) {
		this.level = level;
		this.uses = uses;
		this.speed = speed;
		this.attackDamageBonus = attackDamageBonus;
		this.enchantmentValue = enchantmentValue;

		this.attackDamageModifier = attackDamageModifier;
		this.attackSpeedModifier = attackSpeedModifier;

		this.mineableTag = mineableTag;
		this.repairIngredient = repairIngredient;
	}

	@Override
	public int getUses() {
		return uses;
	}

	@Override
	public float getSpeed() {
		return speed;
	}

	@Override
	public float getAttackDamageBonus() {
		return attackDamageBonus;
	}

	@Override
	public int getLevel() {
		return level;
	}

	@Override
	public int getEnchantmentValue() {
		return enchantmentValue;
	}

	@Override
	public @NotNull Ingredient getRepairIngredient() {
		return repairIngredient.get();
	}
}