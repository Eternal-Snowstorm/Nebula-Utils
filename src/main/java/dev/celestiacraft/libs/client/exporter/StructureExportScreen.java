package dev.celestiacraft.libs.client.exporter;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.client.NebulaLang;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class StructureExportScreen extends Screen {
	private EditBox pathInput;
	private EditBox resolutionInput;
	private EditBox rotXInput;
	private EditBox rotYInput;
	private StructureScene scene;
	private StructureRenderer renderer;
	private final Minecraft minecraft = Minecraft.getInstance();

	private float rotationX = -30f;
	private float rotationY = 45f;
	private float zoom = 1.0f;
	private float panX = 0f;
	private float panY = 0f;
	private boolean dragging = false;
	private boolean angleLocked = false;
	private Checkbox saveConfigCheckbox;

	private final int SELECTED_RESOLUTION = 2048;
	private boolean pendingExport = false;
	private final String initialPath;

	private final List<String> TAB_COMPLETION_LIST = new ArrayList<>();
	private int tabCompletionIndex = -1;
	private String lastInputForSuggestions = null;
	private boolean showSuggestions = false;
	private static final int SUGGESTION_HEIGHT = 12;
	private static final int MAX_SUGGESTIONS = 8;

	public StructureExportScreen(String path) {
		super(NebulaLang.translateDirect("screen.structure_export"));
		this.initialPath = path;
	}

	public StructureExportScreen() {
		this(null);
	}

	@Override
	protected void init() {
		ExportConfig cfg = ExportConfig.load();
		rotationX = cfg.rotationX;
		rotationY = cfg.rotationY;
		angleLocked = cfg.angleLocked;
		zoom = cfg.zoom;

		int centerX = this.width / 2;

		pathInput = new EditBox(
				this.font,
				centerX - 140,
				10,
				200,
				20,
				Component.literal("Path")
		);
		pathInput.setMaxLength(512);
		pathInput.setHint(Component.literal("filename.nbt"));
		pathInput.setResponder((text) -> {
			refreshSuggestions();
		});
		if (initialPath != null) {
			pathInput.setValue(initialPath);
		}
		addRenderableWidget(pathInput);

		addRenderableWidget(Button.builder(
				NebulaLang.translateDirect("export.load"),
				(button) -> {
					loadStructure();
				}
		).bounds(centerX + 65, 10, 50, 20).build());

		resolutionInput = new EditBox(
				this.font,
				centerX - 140,
				this.height - 30,
				130,
				20,
				Component.literal("Resolution")
		);
		resolutionInput.setMaxLength(5);
		resolutionInput.setValue(String.valueOf(SELECTED_RESOLUTION));
		resolutionInput.setHint(Component.literal("2048"));
		resolutionInput.setFilter((string) -> {
			return string.isEmpty() || string.matches("\\d+");
		});
		addRenderableWidget(resolutionInput);

		addRenderableWidget(
				Button.builder(NebulaLang.translateDirect("export.save"),
						(button) -> {
							pendingExport = true;
						}
				).bounds(centerX + 5, this.height - 30, 110, 20).build());

		// 角度控制行
		int angleY = this.height - 55;
		rotXInput = new EditBox(
				this.font,
				centerX - 125,
				angleY,
				45,
				20,
				Component.literal("RotX")
		);
		rotXInput.setMaxLength(7);
		rotXInput.setValue(String.format("%.1f", rotationX));
		rotXInput.setFilter(s -> s.isEmpty() || s.equals("-") || s.matches("-?\\d*\\.?\\d*"));
		rotXInput.setResponder((text) -> {
			try {
				float val = Float.parseFloat(text);
				rotationX = Math.max(-90f, Math.min(90f, val));
			} catch (NumberFormatException ignored) {
			}
		});
		addRenderableWidget(rotXInput);

		rotYInput = new EditBox(
				this.font,
				centerX - 50,
				angleY,
				45,
				20,
				Component.literal("RotY")
		);
		rotYInput.setMaxLength(7);
		rotYInput.setValue(String.format("%.1f", rotationY));
		rotYInput.setFilter((string) -> {
			return string.isEmpty() || string.equals("-") || string.matches("-?\\d*\\.?\\d*");
		});
		rotYInput.setResponder((text) -> {
			try {
				rotationY = Float.parseFloat(text);
			} catch (NumberFormatException ignored) {
			}
		});
		addRenderableWidget(rotYInput);

		Button lockButton = Button.builder(angleLocked
								? NebulaLang.translateDirect("export.unlock")
								: NebulaLang.translateDirect("export.lock"),
						(button) -> {
							angleLocked = !angleLocked;
							button.setMessage(angleLocked
									? NebulaLang.translateDirect("export.unlock")
									: NebulaLang.translateDirect("export.lock"));
						})
				.bounds(centerX + 5, angleY, 40, 20).build();
		addRenderableWidget(lockButton);

		addRenderableWidget(
				Button.builder(
						NebulaLang.translateDirect("export.rotate"),
						(button) -> {
							rotationY += 90f;
							if (rotationY >= 360f) rotationY -= 360f;
							syncAngleInputs();
						}
				).bounds(centerX + 50, angleY, 65, 20).build());

		saveConfigCheckbox = new Checkbox(
				centerX + 120,
				angleY,
				20,
				20,
				NebulaLang.translateDirect("export.save_config"),
				false
		);
		addRenderableWidget(saveConfigCheckbox);

		if (initialPath != null && scene == null) {
			loadStructure();
		}
	}

	private void syncAngleInputs() {
		if (rotXInput != null && !rotXInput.isFocused()) {
			rotXInput.setValue(String.format("%.1f", rotationX));
		}
		if (rotYInput != null && !rotYInput.isFocused()) {
			rotYInput.setValue(String.format("%.1f", rotationY));
		}
	}

	private void loadStructure() {
		String pathStr = pathInput.getValue().trim();

		if (pathStr.isEmpty()) {
			return;
		}

		if (!pathStr.endsWith(".nbt")) {
			pathStr = pathStr + ".nbt";
		}

		Path path = minecraft.gameDirectory
				.toPath()
				.resolve("schematics")
				.resolve(pathStr);

		if (!Files.exists(path)) {
			if (minecraft.player != null) {
				minecraft.player.displayClientMessage(
						Component.literal("File not found: " + pathStr),
						false
				);
			}
			return;
		}

		try {
			scene = StructureScene.loadFromFile(path);

			// 提示缺失方块
			if (!scene.getMissingBlocks().isEmpty() && minecraft.player != null) {

				minecraft.player.displayClientMessage(
						Component.literal(
								"§e[Warning] Missing blocks: "
										+ String.join(", ", scene.getMissingBlocks())
						),
						false
				);
			}

			if (minecraft.level != null) {
				renderer = new StructureRenderer(scene, minecraft.level);
			}
		} catch (IOException exception) {
			NebulaLibs.LOGGER.error(
					"Failed to load structure: {}",
					pathStr,
					exception
			);
			if (minecraft.player != null) {
				minecraft.player.displayClientMessage(
						Component.literal("Failed to load: " + exception.getMessage()),
						false
				);
			}
		}
	}

	private void refreshSuggestions() {
		String input = pathInput.getValue().trim().toLowerCase();

		if (input.equals(lastInputForSuggestions)) {
			return;
		}

		lastInputForSuggestions = input;
		TAB_COMPLETION_LIST.clear();
		tabCompletionIndex = -1;

		Path schematicsDir = minecraft.gameDirectory
				.toPath()
				.resolve("schematics");

		if (Files.isDirectory(schematicsDir)) {
			try (Stream<Path> stream = Files.list(schematicsDir)) {
				stream.filter((Path path) -> {
							return path.toString().endsWith(".nbt");
						})
						.map((Path path) -> {
							return path.getFileName().toString();
						})
						.filter((String name) -> {
							return name.toLowerCase().startsWith(input);
						})
						.sorted()
						.forEach(TAB_COMPLETION_LIST::add);
			} catch (Exception ignored) {
			}
		}

		showSuggestions = pathInput.isFocused() && !TAB_COMPLETION_LIST.isEmpty();
	}

	@Override
	public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		renderBackground(graphics);

		if (!pathInput.isFocused() && !resolutionInput.isFocused() && !rotXInput.isFocused() && !rotYInput.isFocused()) {
			long window = minecraft.getWindow().getWindow();
			float panSpeed = 0.05f / zoom;

			if (InputConstants.isKeyDown(window, InputConstants.KEY_A)) {
				panX += panSpeed;
			}
			if (InputConstants.isKeyDown(window, InputConstants.KEY_D)) {
				panX -= panSpeed;
			}
			if (InputConstants.isKeyDown(window, InputConstants.KEY_W)) {
				panY -= panSpeed;
			}
			if (InputConstants.isKeyDown(window, InputConstants.KEY_S)) {
				panY += panSpeed;
			}
		}
		super.render(graphics, mouseX, mouseY, partialTick);

		// 角度标签
		int angleY = this.height - 50;
		int centerLabelX = this.width / 2;
		graphics.drawString(this.font, "X:", centerLabelX - 140, angleY, 0xFFFFFF);
		graphics.drawString(this.font, "Y:", centerLabelX - 65, angleY, 0xFFFFFF);

		if (scene == null || renderer == null) {
			graphics.drawCenteredString(
					this.font,
					NebulaLang.translateDirect("export.no_structure"),
					this.width / 2, this.height / 2, 0xAAAAAA
			);
		} else {
			int previewH = this.height - 80;
			PoseStack poseStack = graphics.pose();
			renderer.renderPreview(
					poseStack,
					rotationX,
					rotationY,
					zoom,
					panX,
					panY,
					this.width,
					previewH
			);
		}
		if (pendingExport && renderer != null) {
			pendingExport = false;
			doExport();
		}
		showSuggestions = pathInput.isFocused() && !TAB_COMPLETION_LIST.isEmpty();
		if (showSuggestions) {
			int dropX = pathInput.getX();
			int dropY = pathInput.getY() + pathInput.getHeight();
			int dropW = pathInput.getWidth();
			int visibleCount = Math.min(TAB_COMPLETION_LIST.size(), MAX_SUGGESTIONS);
			// Background
			graphics.fill(
					dropX,
					dropY,
					dropX + dropW,
					dropY + visibleCount * SUGGESTION_HEIGHT,
					0xE0000000
			);
			graphics.fill(
					dropX,
					dropY,
					dropX + 1,
					dropY + visibleCount * SUGGESTION_HEIGHT,
					0xFF555555
			);
			graphics.fill(
					dropX + dropW - 1,
					dropY,
					dropX + dropW,
					dropY + visibleCount * SUGGESTION_HEIGHT,
					0xFF555555
			);
			graphics.fill(
					dropX,
					dropY + visibleCount * SUGGESTION_HEIGHT - 1,
					dropX + dropW,
					dropY + visibleCount * SUGGESTION_HEIGHT,
					0xFF555555
			);
			for (int i = 0; i < visibleCount; i++) {
				int itemY = dropY + i * SUGGESTION_HEIGHT;
				boolean hovered = mouseX >= dropX && mouseX < dropX + dropW && mouseY >= itemY && mouseY < itemY + SUGGESTION_HEIGHT;
				boolean selected = i == tabCompletionIndex;
				if (selected || hovered) {
					graphics.fill(dropX + 1, itemY, dropX + dropW - 1, itemY + SUGGESTION_HEIGHT, 0xFF333355);
				}
				int textColor = selected ? 0xFFFFFF00 : (hovered ? 0xFFFFFFCC : 0xFFCCCCCC);
				graphics.drawString(this.font, TAB_COMPLETION_LIST.get(i), dropX + 3, itemY + 2, textColor);
			}
		}
	}

	private void doExport() {
		int resolution;

		try {
			resolution = Integer.parseInt(resolutionInput.getValue().trim());
		} catch (NumberFormatException exception) {
			resolution = SELECTED_RESOLUTION;
		}

		resolution = Math.max(1024, Math.min(16384, resolution));

		String pathStr = pathInput.getValue().trim();

		String baseName = Path.of(pathStr)
				.getFileName()
				.toString()
				.replaceAll("\\.[^.]+$", "");

		Path exportDir = minecraft.gameDirectory
				.toPath()
				.resolve("screenshots")
				.resolve("exports");

		Path outputPath = exportDir.resolve(
				baseName + "_" + resolution + ".png"
		);

		if (minecraft.player != null) {
			minecraft.player.displayClientMessage(
					Component.literal("§7Exporting..."),
					false
			);
		}

		renderer.exportToPng(outputPath, resolution, rotationX, rotationY, zoom, panX, panY, (path) -> {
					if (minecraft.player != null) {
						minecraft.player.displayClientMessage(
								NebulaLang.translateDirect(
										"export.success",
										path.toString()
								),
								false
						);
					}
				}, (error) -> {
					if (minecraft.player != null) {
						minecraft.player.displayClientMessage(
								Component.literal(
										"Export failed: "
												+ error.getMessage()
								),
								false
						);
					}
				}
		);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (pathInput.isFocused() && showSuggestions && !TAB_COMPLETION_LIST.isEmpty()) {
			int visibleCount = Math.min(TAB_COMPLETION_LIST.size(), MAX_SUGGESTIONS);
			if (keyCode == InputConstants.KEY_DOWN) {
				tabCompletionIndex = Math.min(tabCompletionIndex + 1, visibleCount - 1);
				return true;
			}
			if (keyCode == InputConstants.KEY_UP) {
				tabCompletionIndex = Math.max(tabCompletionIndex - 1, 0);
				return true;
			}
			if ((keyCode == InputConstants.KEY_TAB || keyCode == InputConstants.KEY_RETURN) && tabCompletionIndex >= 0) {
				applySuggestion(tabCompletionIndex);
				return true;
			}
			if (keyCode == InputConstants.KEY_TAB) {
				applySuggestion(0);
				return true;
			}
			if (keyCode == InputConstants.KEY_ESCAPE) {
				showSuggestions = false;
				return true;
			}
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	private void applySuggestion(int index) {
		if (index >= 0 && index < TAB_COMPLETION_LIST.size()) {
			pathInput.setValue(TAB_COMPLETION_LIST.get(index));
			showSuggestions = false;
			tabCompletionIndex = -1;
			lastInputForSuggestions = null;
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 && showSuggestions && !TAB_COMPLETION_LIST.isEmpty()) {
			int dropX = pathInput.getX();
			int dropY = pathInput.getY() + pathInput.getHeight();
			int dropW = pathInput.getWidth();

			int visibleCount = Math.min(TAB_COMPLETION_LIST.size(), MAX_SUGGESTIONS);

			if (mouseX >= dropX
					&& mouseX < dropX + dropW
					&& mouseY >= dropY
					&& mouseY < dropY
					+ visibleCount * SUGGESTION_HEIGHT) {

				int clickedIndex = (int) ((mouseY - dropY) / SUGGESTION_HEIGHT);

				if (clickedIndex >= 0 && clickedIndex < visibleCount) {
					applySuggestion(clickedIndex);
					return true;
				}
			}
		}

		// 先让控件处理点击（输入框、按钮等）
		if (super.mouseClicked(mouseX, mouseY, button)) {
			return true;
		}

		// 没有控件处理时，才启动预览区域拖拽
		if (button == 0 && mouseY > 35 && mouseY < this.height - 40) {
			dragging = true;
			return true;
		}

		return false;
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (button == 0) {
			dragging = false;
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (dragging && button == 0 && !angleLocked) {
			rotationY += (float) dragX * 0.5f;
			rotationX += (float) dragY * 0.5f;
			rotationX = Math.max(-90f, Math.min(90f, rotationX));
			syncAngleInputs();
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		zoom = (float) Math.max(0.1, Math.min(10.0, zoom + delta * 0.1));
		return true;
	}

	@Override
	public void onClose() {
		if (saveConfigCheckbox != null && saveConfigCheckbox.selected()) {
			ExportConfig cfg = new ExportConfig();
			cfg.rotationX = rotationX;
			cfg.rotationY = rotationY;
			cfg.angleLocked = angleLocked;
			cfg.zoom = zoom;
			ExportConfig.save(cfg);
		}
		super.onClose();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}