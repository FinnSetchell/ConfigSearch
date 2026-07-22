package dev.finndog.configsearch.api;

import net.minecraft.client.gui.screens.Screen;

public record ExtractionContext(ModInfo mod, Screen screen, ScreenOpener screenFactory) {
	public Screen freshScreen(Screen parent) {
		return screenFactory.open(parent);
	}
}
