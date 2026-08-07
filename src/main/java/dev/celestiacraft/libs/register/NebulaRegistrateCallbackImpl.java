package dev.celestiacraft.libs.register;

import com.mojang.datafixers.util.Either;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class NebulaRegistrateCallbackImpl {
	private static final Map<String, Either<List<NebulaRegistrateCallbackImpl.CallbackImpl<?, ?>>, NebulaRegistrate>> CALLBACKS = new HashMap<>();

	public static void provideRegistrate(NebulaRegistrate registrate) {
		synchronized (CALLBACKS) {
			String modid = registrate.getModid();

			Either<List<NebulaRegistrateCallbackImpl.CallbackImpl<?, ?>>, NebulaRegistrate> either = CALLBACKS.remove(modid);
			if (either != null) {
				Optional<List<CallbackImpl<?, ?>>> optionalCallbacks = either.left();
				if (optionalCallbacks.isEmpty()) {
					throw new IllegalArgumentException("Tried to register a duplicate NebulaRegistrate instance for mod ID: " + modid);
				}

				for (NebulaRegistrateCallbackImpl.CallbackImpl<?, ?> callback : optionalCallbacks.get()) {
					callback.addToRegistrate(registrate);
				}
			}

			CALLBACKS.put(modid, Either.right(registrate));
		}
	}

	public static <R, T extends R> void register(ResourceKey<? extends Registry<R>> registry, ResourceLocation id, NonNullConsumer<? super T> callback) {
		NebulaRegistrateCallbackImpl.CallbackImpl<R, T> callbackImpl = new NebulaRegistrateCallbackImpl.CallbackImpl<>(registry, id, callback);

		Either<List<CallbackImpl<?, ?>>, NebulaRegistrate> either;
		synchronized (CALLBACKS) {
			either = CALLBACKS.computeIfAbsent(id.getNamespace(), (string) -> {
				return Either.left(new ArrayList<>());
			});
			either.ifLeft((callbacks) -> {
				callbacks.add(callbackImpl);
			});
		}

		// This is safe to call outside the synchronized block, because a registrate will only ever be added once.
		either.ifRight(callbackImpl::addToRegistrate);
	}

	private record CallbackImpl<R, T extends R>(
			ResourceKey<? extends Registry<R>> registry,
			ResourceLocation id,
			NonNullConsumer<? super T> callback
	) {
		public void addToRegistrate(NebulaRegistrate registrate) {
			registrate.<R, T>addRegisterCallback(id.getPath(), registry, callback);
		}
	}
}