package top.nebula.utils.compat.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import top.nebula.utils.NebulaUtils;
import top.nebula.utils.compat.kubejs.event.NebulaEvents;
import top.nebula.utils.multiblock.MultiblockStructureBuilder;
import top.nebula.utils.multiblock.PropertyImmutableMap;

import java.time.LocalDateTime;

public class ModKubeJSPlugin extends KubeJSPlugin {
	public void registerEvents() {
		super.registerEvents();

		NebulaEvents.GROUP.register();
	}

	public void registerBindings(BindingsEvent event) {
		super.registerBindings(event);

		event.add("NebulaUtils", NebulaUtils.class);
		event.add("MultiblockStructureBuilder", MultiblockStructureBuilder.class);
		event.add("PropertyImmutableMap", PropertyImmutableMap.class);
		event.add("LocalDateTime", LocalDateTime.class);
	}
}