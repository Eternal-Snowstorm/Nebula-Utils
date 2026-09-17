package dev.celestiacraft.libs.compat.mekanism;

import dev.celestiacraft.libs.common.material.IMaterialType;
import dev.celestiacraft.libs.common.material.NebulaMaterial;
import dev.celestiacraft.libs.common.material.MaterialRegistration;
import dev.celestiacraft.libs.common.material.MaterialTypes;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryBuilder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

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
	public static void register(NebulaMaterial material, IMaterialType type) {
		ResourceLocation id = material.id(type);
		ResourceLocation texture = type.slurryTexture();
		int tint = material.primaryColor() & 0xFFFFFF;

		SlurryBuilder builder = SlurryBuilder.builder(texture == null ? DEFAULT_SLURRY_TEXTURE : texture)
				.tint(tint);

		MaterialRegistration.add(MekanismAPI.SLURRY_REGISTRY_NAME, id, () -> new Slurry(builder));
	}

	/**
	 * 查询材料的矿浆
	 *
	 * <p>
	 * 矿浆的返回类型是 Mekanism 的类, 所以这些查询放在兼容类里;
	 * {@code material.getSlurry()} 这种写法会要求没装 Mekanism 的整合包也能加载 Mekanism 的类.
	 * </p>
	 *
	 * @param material 材料
	 * @param type     {@link dev.celestiacraft.libs.common.material.MaterialKinds#SLURRY} 分类的类型
	 * @return 矿浆, 材料没有声明该类型(或没装 Mekanism)时返回 null
	 */
	@Nullable
	public static Slurry getSlurry(NebulaMaterial material, IMaterialType type) {
		return MekanismAPI.slurryRegistry().getValue(material.id(type));
	}

	/**
	 * @param material 材料
	 * @return 干净矿浆, 没有声明 {@code slurry()} 时返回 null
	 */
	@Nullable
	public static Slurry getSlurry(NebulaMaterial material) {
		return getSlurry(material, MaterialTypes.SLURRY);
	}

	/**
	 * @param material 材料
	 * @return 脏矿浆, 没有声明 {@code dirtySlurry()} 时返回 null
	 */
	@Nullable
	public static Slurry getDirtySlurry(NebulaMaterial material) {
		return getSlurry(material, MaterialTypes.DIRTY_SLURRY);
	}
}
