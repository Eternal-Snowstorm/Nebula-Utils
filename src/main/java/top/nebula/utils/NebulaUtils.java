package top.nebula.utils;

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

	/**
	 * 使用图腾动画
	 *
	 * @param stack 传入物品
	 */
	public static void useTotemAnimation(ItemStack stack) {
		Minecraft.getInstance().gameRenderer.displayItemActivation(stack);
	}

	public NebulaUtils(FMLJavaModLoadingContext context) {
		context.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC, "nebula/common.toml");
	}
}