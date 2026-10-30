package dev.celestiacraft.libs.common.material;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;

/**
 * <h2>MaterialTags</h2>
 *
 * <p>
 * 材料标签的解析工具 —— 把 {@link IMaterialType} 声明的标签模板解析成最终的标签 ID,
 * 再按 {@link IMaterialKind#tagRegistry()} 包装成 {@link TagKey}.
 * </p>
 *
 * <p>
 * 标签写入({@link MaterialAssets})与取用({@link NebulaMaterial})共用这里的解析,
 * 保证取到的标签与写进数据包的是同一个. 注册表 ID 用字符串构造, 因此矿浆这类
 * 属于其他模组注册表的标签不需要在公共代码里引用对应模组的类.
 * </p>
 *
 * @see IMaterialType#commonTags()
 * @see IMaterialType#specificTags()
 */
public class MaterialTags {
	/**
	 * 材料名占位符
	 */
	public static final String MATERIAL_PLACEHOLDER = "%s";

	/**
	 * 解析标签模板
	 *
	 * @param material 材料
	 * @param template 标签模板, 例如 {@code forge:ingots/%s}
	 * @return 标签 ID
	 */
	public static ResourceLocation resolve(NebulaMaterial material, String template) {
		return ResourceLocation.parse(template.formatted(material.getPath()));
	}

	/**
	 * 按注册表把标签 ID 包装成 {@code TagKey}
	 *
	 * @param registry 注册表 ID, 例如 {@code minecraft:item}
	 * @param tag      标签 ID
	 * @param <T>      注册表元素类型
	 * @return 标签; 注册表为 null 时返回 null
	 */
	@Nullable
	public static <T> TagKey<T> key(@Nullable ResourceLocation registry, ResourceLocation tag) {
		if (registry == null) {
			return null;
		}

		return (TagKey<T>) (TagKey<?>) TagKey.create(ResourceKey.createRegistryKey(registry), tag);
	}
}
