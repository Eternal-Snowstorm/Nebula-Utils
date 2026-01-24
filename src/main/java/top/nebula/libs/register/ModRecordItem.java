package top.nebula.libs.register;

import net.minecraft.world.item.RecordItem;
import top.nebula.libs.sounds.RecordSpec;

public class ModRecordItem extends RecordItem {
	private static Properties applyProperties(Properties properties) {
		return properties.stacksTo(1);
	}

	public ModRecordItem(RecordSpec spec, Properties properties) {
		super(spec.analog(), spec.sound(), applyProperties(properties), spec.length());
	}
}