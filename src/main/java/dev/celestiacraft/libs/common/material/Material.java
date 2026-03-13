package dev.celestiacraft.libs.common.material;

import net.minecraft.world.level.block.SoundType;

import java.util.ArrayList;
import java.util.List;

public class Material {
	public static final List<Material> MATERIALS = new ArrayList<>();
	public final List<MaterialType> types = new ArrayList<>();

	public final String name;
	public final String level;

	public int hardness = 5;
	public int resistance = 5;
	public int color1 = 0xFFFFFF;
	public int color2 = 0xFFFFFF;
	public SoundType sound;

	public boolean metal = false;

	public Material(String name, String level) {
		this.name = name;
		this.level = level;
		this.sound = SoundType.METAL;

		MATERIALS.add(this);
	}

	public Material hardness(int hardness) {
		this.hardness = hardness;
		return this;
	}

	public Material resistance(int resistance) {
		this.resistance = resistance;
		return this;
	}

	public Material destroy(int hardness, int resistance) {
		this.hardness = hardness;
		this.resistance = resistance;
		return this;
	}

	public Material color(int color1, int color2) {
		this.color1 = color1;
		this.color2 = color2;
		return this;
	}

	public Material sound(SoundType sound) {
		this.sound = sound;
		return this;
	}

	public Material isMetal() {
		this.metal = true;
		return this;
	}

	private Material type(MaterialType type) {
		types.add(type);
		return this;
	}

	public Material ingot() {
		return type(MaterialType.INGOT);
	}

	public Material plate() {
		return type(MaterialType.PLATE);
	}

	public Material nugget() {
		return type(MaterialType.NUGGET);
	}

	public Material dust() {
		return type(MaterialType.DUST);
	}

	public Material rod() {
		return type(MaterialType.ROD);
	}

	public Material gear() {
		return type(MaterialType.GEAR);
	}

	public Material prism() {
		return type(MaterialType.PRISM);
	}

	public Material dirty() {
		return type(MaterialType.DIRTY);
	}

	public Material wire() {
		return type(MaterialType.WIRE);
	}

	public Material clump() {
		return type(MaterialType.CLUMP);
	}

	public Material shard() {
		return type(MaterialType.SHARD);
	}

	public Material crystal() {
		return type(MaterialType.CRYSTAL);
	}

	public Material slurry() {
		return type(MaterialType.SLURRY);
	}

	public Material dirtySlurry() {
		return type(MaterialType.DIRTY_SLURRY);
	}

	public Material block() {
		return type(MaterialType.BLOCK);
	}

	public Material molten() {
		return type(MaterialType.MOLTEN);
	}
}