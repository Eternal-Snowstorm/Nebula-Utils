package dev.celestiacraft.libs.common.recipe.anvil_craft;

import dev.celestiacraft.libs.NebulaLibs;
import dev.celestiacraft.libs.common.register.NebulaRecipe;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NebulaLibs.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AnvilCraftEvent {
	@SubscribeEvent
	public static void onAnvilUpdate(AnvilUpdateEvent event) {
		Player player = event.getPlayer();
		Level level = player.level();

		SimpleContainer container = new SimpleContainer(event.getLeft(), event.getRight());

		level.getRecipeManager()
				.getRecipeFor(NebulaRecipe.ANVIL_CRAFT.get(), container, level)
				.ifPresent((recipe) -> {
					event.setOutput(recipe.getResult().copy());
					event.setCost(recipe.getCost());
					event.setMaterialCost(recipe.getMaterialCost());
				});
	}
}