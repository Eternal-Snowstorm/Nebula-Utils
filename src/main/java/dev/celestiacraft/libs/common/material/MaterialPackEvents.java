package dev.celestiacraft.libs.common.material;

import dev.celestiacraft.libs.NebulaLibs;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * <h2>MaterialPackEvents</h2>
 *
 * <p>
 * 在客户端资源 / 服务端数据资源包创建时注入材料生成的资源包.
 * </p>
 *
 * @see MaterialPackSource
 */
@Mod.EventBusSubscriber(modid = NebulaLibs.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MaterialPackEvents {
	@SubscribeEvent
	public static void onAddPackFinders(AddPackFindersEvent event) {
		PackType type = event.getPackType();

		if (!type.equals(PackType.CLIENT_RESOURCES) && !type.equals(PackType.SERVER_DATA)) {
			return;
		}

		if (MaterialAssets.isEmpty(type)) {
			return;
		}

		event.addRepositorySource(new MaterialPackSource(type));
	}
}