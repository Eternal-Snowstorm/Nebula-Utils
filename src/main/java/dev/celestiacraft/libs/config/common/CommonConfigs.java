package dev.celestiacraft.libs.config.common;

import dev.celestiacraft.libs.config.api.ConfigModule;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class CommonConfigs extends ConfigModule {
	public static ForgeConfigSpec.ConfigValue<List<? extends String>> BURNING_FLUIDS;
	public static ForgeConfigSpec.BooleanValue ENABLE_LARGE_SPRUCE_PODZOL_CONVERSION;
	public static ForgeConfigSpec.ConfigValue<List<? extends String>> MUST_USE_TOOL_BLOCKS;

	public CommonConfigs(ForgeConfigSpec.Builder builder) {
		super(builder, "common_configs", "Common Config");
	}

	@Override
	protected void addConfigs() {
		BURNING_FLUIDS = builder.comment("Fluids in this list can burn entities like lava")
				.comment("supports fluid tags, declared with \"#\"")
				.comment("type: String[]")
				.comment("default: [#forge:molten_materials]")
				.defineListAllowEmpty(
						"burningFluids",
						List.of("#forge:molten_materials"),
						ConfigModule::validateString
				);

		ENABLE_LARGE_SPRUCE_PODZOL_CONVERSION = builder.comment("Whether to enable the feature that converts surrounding dirt to podzol when large spruce trees grow")
				.comment("type: boolean")
				.comment("default: false")
				.define("enableLargeSprucePodzolConversion", false);

		MUST_USE_TOOL_BLOCKS = builder.comment("Blocks that require correct tool. Use \"#\" for block tags.")
				.comment("type: String[]")
				.comment("default: []")
				.defineListAllowEmpty(
						"must_use_tool_blocks",
						List.of(),
						ConfigModule::validateString
				);
	}
}