package dev.celestiacraft.libs.common.material;

import com.simibubi.create.foundation.data.CreateRegistrate;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

import java.util.ArrayList;
import java.util.List;

public class Material {
	public static final List<Material> MATERIALS = new ArrayList<>();
	public final List<MaterialType> types = new ArrayList<>();

	// 新增 namespace
	public final String namespace;
	// 材料名
	public String name;
	// 显示名称
	public String displayName;
	// 挖掘等级
	public TagKey<Block> level;

	public int hardness = 5;
	public int resistance = 5;
	public int color1 = 0xFFFFFF;
	public int color2 = 0xFFFFFF;
	public SoundType sound;

	public boolean metal = false;

	/**
	 * 传入 namespace
	 *
	 * @param namespace 注册的Material的namespace
	 */
	public Material(String namespace) {
		this.namespace = namespace;
		// 具体材料名在 material() 里设置
		this.name = null;
		this.level = null;
		this.sound = SoundType.METAL;
	}

	// 设置具体材料信息
	public Material create(String name, TagKey<Block> level) {
		this.name = name;
		this.level = level;
		MATERIALS.add(this);
		return this;
	}

	public Material create(String name, String displayName, TagKey<Block> level) {
		this.name = name;
		this.displayName = displayName;
		this.level = level;
		MATERIALS.add(this);
		return this;
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

	/**
	 * 设置材料的显示名称
	 * <p>
	 * 由于Registrate自身的限制
	 * 显示名称只能使用英文
	 *
	 * @param displayName
	 * @return
	 */
	public Material displayName(String displayName) {
		this.displayName = displayName;
		return this;
	}

	public Material color(int color1, int color2) {
		this.color1 = color1;
		this.color2 = color2;
		return this;
	}

	public Material color(int color) {
		this.color1 = color;
		this.color2 = color;
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

	public Material rawOre() {
		return type(MaterialType.RAW_ORE);
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

	public Material metalBlock() {
		return type(MaterialType.BLOCK);
	}

	public Material rawBlock() {
		return type(MaterialType.RAW_BLOCK);
	}

	public Material molten() {
		return type(MaterialType.MOLTEN);
	}
}