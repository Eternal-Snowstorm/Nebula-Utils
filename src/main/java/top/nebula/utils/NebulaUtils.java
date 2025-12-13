package top.nebula.utils;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(NebulaUtils.MODID)
public class NebulaUtils {
	public static final String MODID = "nebula_utils";
	public static final String NAME = "Team Nebula Utils";
	private static final Logger LOGGER = LogUtils.getLogger();

	/**
	 * 使用图腾动画
	 *
	 * @param stack 传入物品
	 */
	public static void useTotemAnimation(ItemStack stack) {
		Minecraft.getInstance().gameRenderer.displayItemActivation(stack);
	}
}