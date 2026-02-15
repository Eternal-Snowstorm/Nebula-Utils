package top.nebula.libs.compat.curios;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import top.theillusivec4.curios.api.CuriosApi;

public class CuriosUtils {
	public static boolean hasItem(LivingEntity entity, Item item) {
		return CuriosApi.getCuriosInventory(entity)
				.map((handler) -> {
					return handler.findFirstCurio((stack) -> {
						return stack.is(item);
					}).isPresent();
				}).orElse(false);
	}
}