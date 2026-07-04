package dev.celestiacraft.libs.api.interaction.fi;

@FunctionalInterface
public interface QuadConsumer<A, B, C, D> {
	void accept(A a, B b, C c, D d);
}