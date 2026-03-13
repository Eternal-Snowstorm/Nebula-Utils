package dev.celestiacraft.libs.common.material;

import dev.celestiacraft.libs.common.fluid.FluidRegistrar;

public class RegisterManager {
	static MaterialRegistrar registrar = new MaterialRegistrar();
	static FluidRegistrar fluidRegistrar = new FluidRegistrar();

	protected void ingot(Material material) {
		registrar.createItem("ingot", material)
				.register();
	}

	protected void plate(Material material) {
		registrar.createItem("plate", material)
				.register();
	}

	protected void nugget(Material material) {
		registrar.createItem("nugget", material)
				.register();
	}

	protected void dust(Material material) {
		registrar.createItem("dust", material)
				.register();
	}

	protected void rod(Material material) {
		registrar.createItem("rod", material)
				.register();
	}

	protected void gear(Material material) {
		registrar.createItem("gear", material)
				.register();
	}

	protected void wire(Material material) {
		registrar.createItem("wire", material)
				.register();
	}

	protected void prism(Material material) {
		registrar.createItem("prism", material)
				.register();
	}

	protected void clump(Material material) {
		registrar.createItem("clump", material)
				.register();
	}

	protected void shard(Material material) {
		registrar.createItem("shard", material)
				.register();
	}

	protected void crystal(Material material) {
		registrar.createItem("crystal", material)
				.register();
	}

	protected void dirtySlurry(Material material) {
		registrar.createItem("dirty_slurry", material)
				.register();
	}

	protected void slurry(Material material) {
		registrar.createItem("slurry", material)
				.register();
	}

	protected void molten(Material material) {
		fluidRegistrar.createMoltenFluid(material)
				.register();
	}
}