package top.nebula.utils;

import dev.latvian.mods.kubejs.typings.Info;
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
import top.nebula.utils.config.CommonConfig;

@Mod(NebulaUtils.MODID)
@Mod.EventBusSubscriber(modid = NebulaUtils.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class NebulaUtils {
	public static final String MODID = "nebula_utils";
	public static final String NAME = "Team Nebula Utils";
	private static final Logger LOGGER = LogManager.getLogger("Nebula");

	public NebulaUtils(FMLJavaModLoadingContext context) {
		context.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC, "nebula/common.toml");
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
	@Info("Use Totem Animation\n\n调用图腾动画")
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
	@Info("Celsius to Fahrenheit\n\n摄氏度转到华氏度")
	public static double toFahrenheit(Object celsius) {
		if (celsius == null) {
			return Double.NaN;
		}

		if (celsius instanceof Number number) {
			return number.doubleValue() * 9.0 / 5.0 + 32.0;
		}

		return Double.NaN;
	}

	/**
	 * 华氏度转到摄氏度
	 * <p>
	 * Fahrenheit to Celsius
	 *
	 * @param fahrenheit 华氏度
	 * @return 转换的后得到的摄氏度
	 */
	@Info("Fahrenheit to Celsius\n\n华氏度转到摄氏度")
	public static double toCelsius(Object fahrenheit) {
		if (fahrenheit == null) {
			return Double.NaN;
		}

		if (fahrenheit instanceof Number number) {
			return (number.doubleValue() - 32.0) * 5.0 / 9.0;
		}

		return Double.NaN;
	}

	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			LOGGER.debug("7f8f7032d4b1f5f2b67f0b1260b5c5f3f187503a75b309ad1c6a163e7ec7f993");
		});
	}
}