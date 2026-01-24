package top.nebula.libs.tags;

import top.nebula.libs.tags.type.BlockTag;
import top.nebula.libs.tags.type.EntityTypeTag;
import top.nebula.libs.tags.type.FluidTag;
import top.nebula.libs.tags.type.ItemTag;

public class TagsBuilder {
	public static BlockTag block(String name) {
		return new BlockTag(name);
	}

	public static ItemTag item(String name) {
		return new ItemTag(name);
	}

	public static FluidTag fluid(String name) {
		return new FluidTag(name);
	}

	public static EntityTypeTag entity(String name) {
		return new EntityTypeTag(name);
	}
}