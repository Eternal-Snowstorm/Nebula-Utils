package dev.celestiacraft.libs.client.exporter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.celestiacraft.libs.NebulaLibs;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ExportConfig {
	public float rotationX = -30f;
	public float rotationY = 45f;
	public boolean angleLocked = false;
	public float zoom = 1.0f;

	private static final Gson GSON = new GsonBuilder()
			.setPrettyPrinting()
			.create();

	private static Path getConfigPath() {
		return Minecraft.getInstance().gameDirectory.toPath()
				.resolve("config")
				.resolve("nebula")
				.resolve("libs")
				.resolve("export_preset.json");
	}

	public static ExportConfig load() {
		Path path = getConfigPath();

		if (!Files.exists(path)) {
			return new ExportConfig();
		}

		try (Reader reader = Files.newBufferedReader(path)) {
			ExportConfig config = GSON.fromJson(reader, ExportConfig.class);

			if (config != null) {
				return config;
			} else {
				return new ExportConfig();
			}
		} catch (Exception exception) {
			NebulaLibs.LOGGER.warn(
					"Failed to load export config, using defaults",
					exception
			);

			return new ExportConfig();
		}
	}

	public static void save(ExportConfig config) {
		Path path = getConfigPath();
		try {
			Files.createDirectories(path.getParent());

			try (Writer writer = Files.newBufferedWriter(path)) {
				GSON.toJson(config, writer);
			}
		} catch (IOException exception) {
			NebulaLibs.LOGGER.error(
					"Failed to save export config",
					exception
			);
		}
	}
}