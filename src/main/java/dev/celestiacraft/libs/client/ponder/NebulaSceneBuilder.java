package dev.celestiacraft.libs.client.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.EntityElement;
import net.createmod.ponder.api.element.TextElementBuilder;
import net.createmod.ponder.api.level.PonderLevel;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.element.InputWindowElement;
import net.createmod.ponder.foundation.instruction.ShowInputInstruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class NebulaSceneBuilder extends CreateSceneBuilder {
	public static final Object OBJECT = new Object();
	private PonderScene ponderScene;
	private final ExtendedWorldInstructions world;

	public NebulaSceneBuilder(SceneBuilder builder) {
		super(builder);
		world = new ExtendedWorldInstructions();
	}

	@Override
	public @NotNull ExtendedWorldInstructions world() {
		return world;
	}

	public void showStructure() {
		showStructure(ponderScene.getBasePlateSize() * 2);
	}

	public void showStructure(int height) {
		BlockPos start = new BlockPos(ponderScene.getBasePlateOffsetX(), 0, ponderScene.getBasePlateOffsetZ());
		BlockPos size = new BlockPos(ponderScene.getBasePlateSize() - 1, height, ponderScene.getBasePlateSize() - 1);
		Selection selection = ponderScene.getSceneBuildingUtil().select().cuboid(start, size);
		encapsulateBounds(size);
		world().showSection(selection, Direction.UP);
	}

	public void encapsulateBounds(BlockPos size) {
		addInstruction((scene) -> {
			PonderLevel sceneWorld = scene.getWorld();
			sceneWorld.getBounds().encapsulate(size);
		});
	}

	public TextElementBuilder text(int duration, String text) {
		return overlay().showText(duration)
				.text(text);
	}

	public TextElementBuilder text(int duration, String text, Vec3 position) {
		return overlay().showText(duration)
				.text(text)
				.pointAt(position);
	}

	public TextElementBuilder sharedText(int duration, ResourceLocation location) {
		return overlay().showText(duration)
				.sharedText(location);
	}

	public TextElementBuilder sharedText(int duration, ResourceLocation location, Vec3 position) {
		return overlay().showText(duration)
				.sharedText(location)
				.pointAt(position)
				.colored(PonderPalette.BLUE);
	}

	public InputWindowElement showControls(int duration, Vec3 pos, Pointing pointing) {
		InputWindowElement element = new InputWindowElement(pos, pointing);
		addInstruction(new ShowInputInstruction(element, duration));
		return element;
	}

	public static void init5x5(SceneBuilder builder, SceneBuildingUtil util) {
		builder.configureBasePlate(0, 0, 5);
		builder.scaleSceneView(0.9f);
		builder.world().showSection(util.select().layer(0), Direction.UP);
	}

	public static void init7x7(SceneBuilder builder, SceneBuildingUtil util) {
		builder.configureBasePlate(0, 0, 7);
		builder.scaleSceneView(0.75f);
		builder.world().showSection(util.select().layer(0), Direction.UP);
	}

	public static void init9x9(SceneBuilder builder, SceneBuildingUtil util) {
		builder.configureBasePlate(0, 0, 9);
		builder.scaleSceneView(0.6f);
		builder.world().showSection(util.select().layer(0), Direction.UP);
	}

	public static void rotateAround(SceneBuilder builder, int duration, int angle) {
		float times = 360f / angle;

		for (int i = 0; i < times; i++) {
			rotate(builder, (int) (duration / times), angle);
		}
	}

	public static void rotate(SceneBuilder builder, int time, int angle) {
		builder.rotateCameraY(angle);
		builder.idle(time);
	}

	public class ExtendedWorldInstructions extends WorldInstructions {
		public void removeEntity(ElementLink<EntityElement> link) {
			addInstruction((scene) -> {
				EntityElement resolve = scene.resolve(link);
				if (resolve != null) {
					resolve.ifPresent(Entity::discard);
				}
			});
		}
	}
}