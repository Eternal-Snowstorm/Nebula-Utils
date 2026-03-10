package dev.celestiacraft.libs.mixin.client;

import dev.celestiacraft.libs.client.tooltip.InlineItemPatternParser;
import dev.celestiacraft.libs.compat.ftbquests.client.InlineItemQuestWidget;
import dev.ftb.mods.ftblibrary.ui.TextField;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.BlankPanel;
import dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel;
import dev.ftb.mods.ftbquests.quest.Quest;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = ViewQuestPanel.class, remap = false)
public class ViewQuestPanelMixin {

	@Shadow
	private BlankPanel panelText;

	@Shadow
	private Quest quest;

	@Inject(method = "addDescriptionText", at = @At("TAIL"))
	private void nebula$afterAddDescriptionText(boolean canEdit, Component subtitle, CallbackInfo ci) {
		if (quest == null) return;

		List<Widget> widgets = panelText.getWidgets();

		for (int i = 0; i < widgets.size(); i++) {
			Widget widget = widgets.get(i);
			if (!(widget instanceof TextField tf)) continue;

			String text;
			try {
				Component rawText = ((TextFieldAccessor) tf).nebula$getRawText();
				text = rawText != null ? rawText.getString() : "";
			} catch (Exception e) {
				continue;
			}

			if (!InlineItemPatternParser.hasPattern(text)) continue;

			int maxW = tf.maxWidth > 0 ? tf.maxWidth : panelText.width;
			InlineItemQuestWidget replacement = new InlineItemQuestWidget(
					panelText, text, maxW
			);
			replacement.setPosAndSize(widget.posX, widget.posY, widget.width, replacement.height);
			widgets.set(i, replacement);
		}
	}
}
