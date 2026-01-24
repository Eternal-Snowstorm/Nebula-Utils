package top.nebula.libs.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class CommonConfig {
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BURNING_FLUIDS;
	public static final ForgeConfigSpec.BooleanValue ENABLE_LARGE_SPRUCE_PODZOL_CONVERSION;

	static {
		BUILDER.comment("All settings below will only take effect after restarting the server or client.")
				.push("general");

		BURNING_FLUIDS = BUILDER
				.comment("Fluids in this list can burn entities like lava")
				.comment("supports fluid tags, declared with \"#\"")
				.defineListAllowEmpty("burningFluids", List.of("#forge:molten_materials"), CommonConfig::validateString);

		ENABLE_LARGE_SPRUCE_PODZOL_CONVERSION = BUILDER
				.comment("Whether to enable the feature that converts surrounding dirt to podzol when large spruce trees grow")
				.comment("Default value: false")
				.define("enableLargeSprucePodzolConversion", false);
	}

	public static final ForgeConfigSpec SPEC = BUILDER.build();

	/**
	 * 云杉木控制
	 *
	 * @return
	 */
	public static boolean isLargeSprucePodzolConversionEnabled() {
		return ENABLE_LARGE_SPRUCE_PODZOL_CONVERSION.get();
	}

	private static boolean validateString(Object obj) {
		return obj instanceof String;
	}
}