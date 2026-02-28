package dev.celestiacraft.libs.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.client.exporter.StructureExportScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Mod.EventBusSubscriber(modid = NebulaLibs.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ExporterCommand {
	@SubscribeEvent
	public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
		CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
		Minecraft minecraft = Minecraft.getInstance();

		dispatcher.register(Commands.literal("nebula")
				.then(Commands.literal("export")
						.executes((context) -> {
							minecraft.tell(() -> {
								Minecraft.getInstance().setScreen(new StructureExportScreen());
							});
							return 1;
						})
						.then(Commands.argument("file", StringArgumentType.string())
								.suggests((context, builder) -> {
									Path schematicsDir = minecraft.gameDirectory.toPath().resolve("schematics");
									if (Files.isDirectory(schematicsDir)) {
										try (Stream<Path> stream = Files.list(schematicsDir)) {
											stream.filter((path) -> {
														return path.toString().endsWith(".nbt");
													})
													.map((path) -> {
														return path.getFileName().toString();
													})
													.filter((name) -> {
														return name.toLowerCase().startsWith(builder.getRemainingLowerCase());
													})
													.forEach(builder::suggest);
										} catch (Exception ignored) {
										}
									}
									return builder.buildFuture();
								})
								.executes((context) -> {
									String file = StringArgumentType.getString(context, "file");
									minecraft.tell(() -> {
										minecraft.setScreen(new StructureExportScreen(file));
									});
									return 1;
								})
						)
				)
		);
	}
}