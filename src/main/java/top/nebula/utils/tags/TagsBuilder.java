package top.nebula.utils.tags;

import top.nebula.utils.tags.type.BlockTag;
import top.nebula.utils.tags.type.EntityTypeTag;
import top.nebula.utils.tags.type.FluidTag;
import top.nebula.utils.tags.type.ItemTag;

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