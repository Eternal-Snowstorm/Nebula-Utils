package dev.celestiacraft.libs.register;

import com.tterrag.registrate.util.nullness.NonNullConsumer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class NebulaRegistrateCallback {
	public static <R, T extends R> void register(ResourceKey<? extends Registry<R>> registry, ResourceLocation id, NonNullConsumer<? super T> callback) {
		NebulaRegistrateCallbackImpl.<R, T>register(registry, id, callback);
	}

	public static void provideRegistrate(NebulaRegistrate registrate) {
		NebulaRegistrateCallbackImpl.provideRegistrate(registrate);
	}
}