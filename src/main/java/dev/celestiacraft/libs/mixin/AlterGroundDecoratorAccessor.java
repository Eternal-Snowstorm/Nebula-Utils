package dev.celestiacraft.libs.mixin;

import net.minecraft.world.level.levelgen.feature.treedecorators.AlterGroundDecorator;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AlterGroundDecorator.class)
public interface AlterGroundDecoratorAccessor {
	@Accessor("provider")
	BlockStateProvider getProvider();
}
