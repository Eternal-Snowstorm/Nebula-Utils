package dev.celestiacraft.libs.compat.mekanism;

import dev.celestiacraft.libs.common.material.Material;
import dev.celestiacraft.libs.common.material.MaterialRegistration;
import dev.celestiacraft.libs.common.material.IMaterialType;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryBuilder;
import net.minecraft.resources.ResourceLocation;

/**
 * <h2>MekanismMaterialCompat</h2>
 *
 * <p>
 * Mekanism 矿浆兼容.
 * </p>
 *
 * <p>
 * 该类只会在 Mekanism 已加载时被加载, 因此可以安全地直接引用 Mekanism API.
 * 贴图由 {@link IMaterialType#slurryTexture()} 提供, 所以自定义的矿浆类型也能用.
 * </p>
 */
public final class MekanismMaterialCompat {
	/**
	 * 没有指定贴图时使用的默认矿浆贴图
	 */
	public static final ResourceLocation DEFAULT_SLURRY_TEXTURE = ResourceLocation.parse("mekanism:slurry/clean");

	private MekanismMaterialCompat() {
	}

	/**
	 * 注册材料的矿浆
	 *
	 * @param material 材料
	 * @param type     {@link dev.celestiacraft.libs.common.material.MaterialKinds#SLURRY} 分类的类型
	 */
	public static void register(Material material, IMaterialType type) {
		ResourceLocation id = material.id(type);
		ResourceLocation texture = type.slurryTexture();
		int tint = material.primaryColor() & 0xFFFFFF;

		SlurryBuilder builder = SlurryBuilder.builder(texture == null ? DEFAULT_SLURRY_TEXTURE : texture)
				.tint(tint);

		MaterialRegistration.add(MekanismAPI.SLURRY_REGISTRY_NAME, id, () -> new Slurry(builder));
	}
}
