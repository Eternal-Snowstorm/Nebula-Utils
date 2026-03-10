package dev.celestiacraft.libs.mixin.client;

import dev.ftb.mods.ftblibrary.ui.TextField;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = TextField.class, remap = false)
public interface TextFieldAccessor {

	@Accessor("rawText")
	Component nebula$getRawText();
}
