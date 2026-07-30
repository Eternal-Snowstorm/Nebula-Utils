package dev.celestiacraft.libs.config.api;

import net.minecraftforge.common.ForgeConfigSpec;

public abstract class ConfigModule {
	protected final ForgeConfigSpec.Builder builder;

	public ConfigModule(ForgeConfigSpec.Builder builder, String path, String comment) {
		this.builder = builder;

		builder.comment(comment + getSuffixText())
				.push(path);

		addConfigs();

		builder.pop();
	}

	protected abstract void addConfigs();

	protected String getSuffixText() {
		return " Settings";
	}

	public static boolean validateString(Object object) {
		return object instanceof String;
	}
}