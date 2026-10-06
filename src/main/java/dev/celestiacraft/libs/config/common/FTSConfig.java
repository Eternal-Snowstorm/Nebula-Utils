package dev.celestiacraft.libs.config.common;

import dev.celestiacraft.libs.config.api.ConfigModule;
import net.minecraftforge.common.ForgeConfigSpec;

public class FTSConfig extends ConfigModule {
	public static ForgeConfigSpec.BooleanValue INDEX_EXPANDED;

	public FTSConfig(ForgeConfigSpec.Builder builder) {
		super(builder, "tab_configs", "Tab Sections");
	}

	@Override
	protected void addConfigs() {
		INDEX_EXPANDED = builder.comment("Whether the index panel on the left edge of the creative inventory is expanded")
				.comment("type: boolean")
				.comment("default: true")
				.define("index_expanded", true);
	}
}
