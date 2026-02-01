package top.nebula.libs.compat.curios;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import top.theillusivec4.curios.api.CuriosApi;

public class CuriosUtils {
	public static boolean hasItem(Player player, Item item) {
		return CuriosApi.getCuriosInventory(player)
				.map((handler) -> {
					return handler.findFirstCurio((stack) -> {
						return stack.is(item);
					}).isPresent();
				}).orElse(false);
	}
}