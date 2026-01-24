package top.nebula.libs.sounds;

import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

public record RecordSpec(Supplier<SoundEvent> sound, int analog, int length) {
	public static final int DEFAULT_ANALOG = 15;

	public static RecordSpec of(Supplier<SoundEvent> sound, int lengthSeconds) {
		return new RecordSpec(sound, DEFAULT_ANALOG, lengthSeconds);
	}
}