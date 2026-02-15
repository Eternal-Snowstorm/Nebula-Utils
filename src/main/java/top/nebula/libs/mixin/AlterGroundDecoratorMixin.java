package top.nebula.libs.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.AlterGroundDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.nebula.libs.config.CommonConfig;

@Mixin(AlterGroundDecorator.class)
public abstract class AlterGroundDecoratorMixin {
	@Final
	@Shadow
	private BlockStateProvider provider;

	@Inject(
			method = "placeBlockAt",
			at = @At("HEAD"),
			cancellable = true
	)
	private void onPlaceBlockAt(TreeDecorator.Context context, BlockPos pos, CallbackInfo info) {
		if (!CommonConfig.ENABLE_LARGE_SPRUCE_PODZOL_CONVERSION.get()) {
			BlockState state = this.provider.getState(context.random(), pos);
			if (state.is(Blocks.PODZOL)) {
				info.cancel();
			}
		}
	}
}