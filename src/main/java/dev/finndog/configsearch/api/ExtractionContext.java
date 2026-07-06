package dev.finndog.configsearch.api;

import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.gui.screens.Screen;

public record ExtractionContext(ModContainer mod, Screen screen, ScreenOpener screenFactory) {
	public Screen freshScreen(Screen parent) {
		return screenFactory.open(parent);
	}
}
