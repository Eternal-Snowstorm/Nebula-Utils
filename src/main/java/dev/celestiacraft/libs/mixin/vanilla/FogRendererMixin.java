package dev.celestiacraft.libs.mixin.vanilla;

import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FogRenderer.class)
public class FogRendererMixin {
	@Redirect(
			method = "setupFog(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/FogRenderer$FogMode;FZF)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;isSpectator()Z",
					ordinal = 0
			)
	)
	private static boolean nebula$setupFog(Entity entity) {
		if (entity instanceof Player player) {
			if (player.isCreative() || player.fireImmune() || player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
				return true;
			}
		}
		return entity.isSpectator();
	}
}