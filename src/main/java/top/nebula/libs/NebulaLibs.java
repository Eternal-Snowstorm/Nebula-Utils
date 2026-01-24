package top.nebula.libs;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.nebula.libs.config.CommonConfig;

@Mod(NebulaLibs.MODID)
@Mod.EventBusSubscriber(modid = NebulaLibs.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class NebulaLibs {
	public static final String MODID = "nebula_libs";
	public static final String NAME = "Nebula Libs";
	public static final Logger LOGGER = LogManager.getLogger("Nebula");

	public NebulaLibs(FMLJavaModLoadingContext context) {
		context.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC, "nebula/utils/common.toml");
	}

	/**
	 * 加载ResourceLocation资源
	 *
	 * @param path
	 * @return
	 */
	public static ResourceLocation loadResource(String path) {
		return ResourceLocation.fromNamespaceAndPath(MODID, path);
	}

	/**
	 * 使用图腾动画
	 * <p>
	 * Use Totem Animation
	 *
	 * @param item 传入物品
	 */
	public static void useTotemAnimation(ItemStack item) {
		Minecraft.getInstance().gameRenderer.displayItemActivation(item);
	}

	/**
	 * 摄氏度转到华氏度
	 * <p>
	 * Celsius to Fahrenheit
	 *
	 * @param celsius 摄氏度
	 * @return 转换的后得到的华氏度
	 */
	public static int toFahrenheit(Double celsius) {
		if (celsius == null) {
			return 0;
		} else {
			return (int) (celsius * 9.0 / 5.0 + 32.0);
		}
	}

	/**
	 * D
	 * 华氏度转到摄氏度
	 * <p>
	 * Fahrenheit to Celsius
	 *
	 * @param fahrenheit 华氏度
	 * @return 转换的后得到的摄氏度
	 */
	public static int toCelsius(Double fahrenheit) {
		if (fahrenheit == null) {
			return 0;
		} else {
			return (int) ((fahrenheit - 32.0) * 5.0 / 9.0);
		}
	}

	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			LOGGER.debug("7f8f7032d4b1f5f2b67f0b1260b5c5f3f187503a75b309ad1c6a163e7ec7f993");
		});
	}
}