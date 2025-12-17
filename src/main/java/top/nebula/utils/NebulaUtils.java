package top.nebula.utils;

import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.nebula.utils.config.CommonConfig;

@Mod(NebulaUtils.MODID)
public class NebulaUtils {
	public static final String MODID = "nebula_utils";
	public static final String NAME = "Team Nebula Utils";
	private static final Logger LOGGER = LogManager.getLogger("Nebula");

	public NebulaUtils(FMLJavaModLoadingContext context) {
		context.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC, "nebula/common.toml");
	}

	/**
	 * 使用图腾动画
	 * <p>
	 * Use Totem Animation
	 *
	 * @param stack 传入物品
	 */
	@Info("Use Totem Animation\n\n调用图腾动画")
	public static void useTotemAnimation(ItemStack stack) {
		Minecraft.getInstance().gameRenderer.displayItemActivation(stack);
	}

	/**
	 * 摄氏度转到华氏度
	 * <p>
	 * Celsius to Fahrenheit
	 *
	 * @param celsius 摄氏度
	 * @return
	 */
	@Info("Celsius to Fahrenheit\n\n摄氏度转到华氏度")
	public static double toFahrenheit(double celsius) {
		return celsius * 9.0 / 5.0 + 32.0;
	}

	/**
	 * 华氏度转到摄氏度
	 * <p>
	 * Fahrenheit to Celsius
	 *
	 * @param fahrenheit 华氏度
	 * @return
	 */
	@Info("Fahrenheit to Celsius\n\n华氏度转到摄氏度")
	public static double toCelsius(double fahrenheit) {
		return (fahrenheit - 32.0) * 5.0 / 9.0;
	}
}