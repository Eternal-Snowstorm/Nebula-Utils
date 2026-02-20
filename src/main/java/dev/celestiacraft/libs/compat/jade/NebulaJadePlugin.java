package dev.celestiacraft.libs.compat.jade;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class NebulaJadePlugin implements IWailaPlugin {
	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.addTooltipCollectedCallback(10001, CommonJadeTipProvider::onTooltipCollected);
	}
}
