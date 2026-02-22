package dev.celestiacraft.libs.compat.patchouli.multiblock;

public interface IMultiblockProvider {
	MultiblockHandler getMultiblockHandler();

	default void showMultiblock() {
		getMultiblockHandler().toggleVisualization();
	}

	default boolean isStructureValid() {
		return getMultiblockHandler().isValid();
	}
}