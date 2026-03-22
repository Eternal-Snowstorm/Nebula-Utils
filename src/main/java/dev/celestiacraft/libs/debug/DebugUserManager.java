package dev.celestiacraft.libs.debug;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import dev.celestiacraft.libs.NebulaLibs;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DebugUserManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private static final Path FILE = FMLPaths.CONFIGDIR.get()
			.resolve("nebula")
			.resolve("libs")
			.resolve("debug_users.json");

	private static final Set<String> DEBUG_USERS = new HashSet<>();

	private static final Type LIST_TYPE = new TypeToken<List<String>>() {
	}.getType();

	// 默认 Debug 用户
	private static final List<String> DEFAULT_DEBUG_USERS = List.of(
			"Qi_Month",
			"EternalSnowstorm",
			"Re_Construction",
			"117458866249",
			"Ye_Anqing",
			"KEYboardManDesu",
			"oooooooo_zane",
			"Flash_Yi"
	);

	public static void load() {
		try {
			if (!Files.exists(FILE)) {
				Files.createDirectories(FILE.getParent());

				// 写入默认用户
				try (Writer writer = Files.newBufferedWriter(FILE)) {
					JsonWriter jsonWriter = new JsonWriter(writer);
					jsonWriter.setIndent("\t");
					GSON.toJson(DEFAULT_DEBUG_USERS, LIST_TYPE, jsonWriter);
				}
			}

			try (Reader reader = Files.newBufferedReader(FILE)) {
				List<String> users = GSON.fromJson(reader, LIST_TYPE);

				DEBUG_USERS.clear();

				if (users != null) {
					users.forEach((user) -> {
						DEBUG_USERS.add(user.toLowerCase());
					});
				}
			}

			NebulaLibs.LOGGER.info("Loaded Debug Users: {}", DEBUG_USERS.size());

		} catch (Exception exception) {
			NebulaLibs.LOGGER.error("Error loading debug users", exception);
		}
	}

	public static boolean isDebugger(Player player) {
		if (player == null) {
			return false;
		}

		String name = player.getGameProfile().getName();
		return name != null && DEBUG_USERS.contains(name.toLowerCase());
	}
}