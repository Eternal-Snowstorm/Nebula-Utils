package dev.celestiacraft.libs.api.tab.creativetab;

import lombok.Getter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 存放若干种能以某种方式表示 ItemStack 的对象
 *
 * @since 2.0
 */
public class ConglomerateOfItems {
	public final List<Object> conglomerate = new ArrayList<>();
	/**
	 * -- GETTER --
	 *
	 */
	@Getter
	private final List<ItemStack> stacks = new ArrayList<>();

	public static ConglomerateOfItems create() {
		return new ConglomerateOfItems();
	}

	/**
	 * @since 4.0
	 */
	public void resolveStacks(RegistryAccess registryAccess) {
		stacks.clear();

		for (Object o : conglomerate) {
			if (o instanceof ItemStack i) {
				stacks.add(i);
				continue;
			}

			if (o instanceof Item i) {
				stacks.add(i.getDefaultInstance());
				continue;
			}

			if (o instanceof RegistryObject<?> i) {
				if (i.get() instanceof Item item) {
						stacks.add(item.getDefaultInstance());
				}
				continue;
			}

			if (o instanceof ItemLike i) {
				stacks.add(i.asItem().getDefaultInstance());
				continue;
			}

			if (o instanceof Supplier<?> supplier) {
				if (supplier.get() instanceof ItemStack is) {
					stacks.add(is);
				}
			}

			if (o instanceof List<?> list) {
				if (list.stream().allMatch(ItemStack.class::isInstance)) {
					@SuppressWarnings("unchecked")
					List<ItemStack> itemStacks = (List<ItemStack>) list;
					stacks.addAll(itemStacks);
				}
			}

			if (o instanceof RegistryDependentEntry entry) {
				stacks.addAll(entry.add(registryAccess));
			}
		}
	}

	/**
	 * 向聚合体中添加一个物品。
	 * 该物品之后会通过 {@link Item#getDefaultInstance()}} 解析为 ItemStack
	 *
	 * @since 2.0
	 */
	public ConglomerateOfItems add(Item item) {
		conglomerate.add(item);
		return this;
	}

	/**
	 * 向聚合体中添加一个 ItemStack。
	 * 该 ItemStack 不会被解析，而是直接传入。
	 *
	 * @since 2.0
	 */
	public ConglomerateOfItems add(ItemStack stack) {
		conglomerate.add(stack);
		return this;
	}

	/**
	 * 向聚合体中添加一个 {@link RegistryObject}。
	 * 即使在注册发生之前也可以调用。
	 * 该 RegistryObject 之后会通过 {@link RegistryObject#get()} 解析为 ItemStack
	 *
	 * @since 2.0
	 */
	public ConglomerateOfItems add(RegistryObject<Item> registryObjectOfItem) {
		conglomerate.add(registryObjectOfItem);
		return this;
	}

	/**
	 * 向聚合体中添加一个 {@link ItemLike}。
	 * 该 ItemLike 之后会通过 {@link ItemLike#asItem()} 与 {@link Item#getDefaultInstance()} 解析为 ItemStack
	 *
	 * @since 2.0
	 */
	public ConglomerateOfItems add(ItemLike itemLike) {
		conglomerate.add(itemLike);
		return this;
	}

	/**
	 * 向聚合体中添加一个 ItemStack 的 Supplier。
	 * 该 Supplier 之后会被解析，并添加其中的 ItemStack
	 *
	 * @since 2.0
	 */
	public ConglomerateOfItems add(Supplier<ItemStack> itemStackSupplier) {
		conglomerate.add(itemStackSupplier);
		return this;
	}

	/**
	 * 向聚合体中添加一个 ItemStack 列表。
	 *
	 * @since 4.0
	 */
	public ConglomerateOfItems add(List<ItemStack> listofStacks) {
		conglomerate.add(listofStacks);
		return this;
	}

	/**
	 * 向聚合体中添加一个 RegistryDependentEntry。
	 * 该条目持有 {@link RegistryAccess}
	 * 之后会被解析，添加它返回的所有 ItemStack
	 *
	 * @since 4.0
	 */
	public ConglomerateOfItems add(RegistryDependentEntry entry) {
		conglomerate.add(entry);
		return this;
	}

	/**
	 * 可在解析阶段访问 {@link RegistryAccess}，并返回一组 ItemStack 的条目
	 *
	 * @since 4.0
	 */
	public interface RegistryDependentEntry {
		List<ItemStack> add(RegistryAccess registryAccess);
	}
}
