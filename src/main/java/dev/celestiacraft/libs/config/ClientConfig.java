package dev.celestiacraft.libs.config;

import dev.celestiacraft.libs.config.common.FTSConfig;
import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;

	public static final FTSConfig FTS;

	static {
		BUILDER.comment("All settings below will only take effect after restarting the client.")
				.push("client");

		FTS = new FTSConfig(BUILDER);

		SPEC = BUILDER.build();
		BUILDER.pop();
	}
}