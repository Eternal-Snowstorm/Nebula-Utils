package dev.celestiacraft.libs.compat.jade;

import dev.celestiacraft.libs.api.register.multiblock.ControllerBlock;
import dev.celestiacraft.libs.compat.jade.common.multiblock.ControllerBlockProvider;
import dev.celestiacraft.libs.compat.jade.util.CommonJadeTipProvider;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class NebulaJadePlugin implements IWailaPlugin {
	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.addTooltipCollectedCallback(10001, CommonJadeTipProvider::onTooltipCollected);
		registration.registerBlockComponent(ControllerBlockProvider.INSTANCE, ControllerBlock.class);
	}
}