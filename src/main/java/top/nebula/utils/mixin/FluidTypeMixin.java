package top.nebula.utils.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.nebula.utils.config.CommonConfig;

@Mixin(value = FluidType.class, remap = false)
public abstract class FluidTypeMixin {
	/**
	 * 判断当前 FluidState 是否在 burningFluids 配置中
	 * 支持：
	 * <p>
	 * - "modid:fluid"
	 * <p>
	 * - "#namespace:tag"
	 */
	@Unique
	private static boolean nebula$isBurningFluid(FluidState state) {
		Fluid fluid = state.getType();
		ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);

		for (String entry : CommonConfig.BURNING_FLUIDS.get()) {
			if (entry.startsWith("#")) {
				ResourceLocation tagId = ResourceLocation.tryParse(entry.substring(1));
				if (tagId != null) {
					TagKey<Fluid> tag = TagKey.create(
							BuiltInRegistries.FLUID.key(),
							tagId
					);
					if (fluid.is(tag)) {
						return true;
					}
				}
			} else {
				if (id.toString().equals(entry)) {
					return true;
				}
			}
		}
		return false;
	}

	@Inject(method = "move", at = @At("HEAD"), remap = false)
	private void nebula$move(
			FluidState state,
			LivingEntity entity,
			Vec3 movementVector,
			double gravity,
			CallbackInfoReturnable<Boolean> cir
	) {
		if (nebula$isBurningFluid(state)) {
			entity.lavaHurt();
		}
	}

	@Inject(method = "setItemMovement", at = @At("HEAD"), remap = false)
	private void nebula$setItemMovement(ItemEntity entity, CallbackInfo ci) {
		FluidState state = entity.level().getFluidState(entity.blockPosition());

		if (nebula$isBurningFluid(state)) {
			entity.lavaHurt();
		}
	}
}