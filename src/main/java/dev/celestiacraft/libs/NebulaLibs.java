package dev.celestiacraft.libs;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.item.TooltipModifier;
import dev.celestiacraft.libs.common.material.MaterialRegister;
import dev.celestiacraft.libs.common.register.NebulaItem;
import dev.celestiacraft.libs.common.register.TestMaterial;
import dev.celestiacraft.libs.compat.ftbquests.client.FTBQuestsClientCompat;
import dev.celestiacraft.libs.debug.DebugUserManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import dev.celestiacraft.libs.client.tooltip.InlineItemClientTooltipComponent;
import dev.celestiacraft.libs.client.tooltip.InlineItemTooltipComponent;
import dev.celestiacraft.libs.compat.ICheckModLoaded;
import dev.celestiacraft.libs.config.CommonConfig;

@Mod(NebulaLibs.MODID)
@Mod.EventBusSubscriber(modid = NebulaLibs.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class NebulaLibs {
	public static final String MODID = "nebula_libs";
	public static final String NAME = "Nebula Libs";
	public static final Logger LOGGER = LogManager.getLogger("Nebula");
	public final CreateRegistrate registrate;
	public static NebulaLibs INSTANCE;

	public NebulaLibs(FMLJavaModLoadingContext context) {
		IEventBus bus = context.getModEventBus();
		INSTANCE = this;

		this.registrate = CreateRegistrate.create(MODID)
				.setTooltipModifierFactory((item) -> {
					return new ItemDescription.Modifier(item, TooltipHelper.Palette.STANDARD_CREATE)
							.andThen(TooltipModifier.mapNull(KineticStats.create(item)));
				});

		registrate.registerEventListeners(bus);

		TestMaterial.init();
		MaterialRegister.register(registrate);

		NebulaItem.register(bus);

		context.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC, "nebula/libs/common.toml");
		DebugUserManager.load();
	}

	public static ResourceLocation loadResource(String path) {
		return ResourceLocation.fromNamespaceAndPath(MODID, path);
	}

	public static ResourceLocation loadForgeResource(String path) {
		return ResourceLocation.fromNamespaceAndPath("forge", path);
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
	public static void onRegisterTooltipFactories(RegisterClientTooltipComponentFactoriesEvent event) {
		event.register(InlineItemTooltipComponent.class, InlineItemClientTooltipComponent::new);
	}

	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			if (ICheckModLoaded.hasFTBQuests()) {
				LOGGER.debug("7f8f7032d4b1f5f2b67f0b1260b5c5f3f187503a75b309ad1c6a163e7ec7f993");
				FTBQuestsClientCompat.init();
			}
		});
	}
}