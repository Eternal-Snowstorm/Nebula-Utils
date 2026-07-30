package dev.celestiacraft.libs.config;

import dev.celestiacraft.libs.config.common.CommonConfigs;
import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfig {
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;

	public static final CommonConfigs COMMON;

	static {
		BUILDER.comment("All settings below will only take effect after restarting the server or client.")
				.push("general");

		COMMON = new CommonConfigs(BUILDER);

		SPEC = BUILDER.build();
		BUILDER.pop();
	}
}