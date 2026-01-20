package top.nebula.utils.tags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public abstract class AbstractTagBuilder<T> {
	protected final String name;
	protected String namespace;

	protected AbstractTagBuilder(String name) {
		this.name = name;
	}

	public AbstractTagBuilder<T> namespace(String namespace) {
		this.namespace = namespace;
		return this;
	}

	public AbstractTagBuilder<T> vanilla() {
		return namespace("minecraft");
	}

	public AbstractTagBuilder<T> forge() {
		return namespace("forge");
	}

	public AbstractTagBuilder<T> mod(String modid) {
		return namespace(modid);
	}

	protected ResourceLocation id() {
		if (namespace == null) {
			throw new IllegalStateException("Tag namespace not specified: " + name);
		}
		return ResourceLocation.fromNamespaceAndPath(namespace, name);
	}

	public abstract TagKey<T> build();
}
