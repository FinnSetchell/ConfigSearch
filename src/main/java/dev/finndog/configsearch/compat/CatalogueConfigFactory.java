package dev.finndog.configsearch.compat;

import dev.finndog.configsearch.gui.ConfigSearchScreen;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.gui.screens.Screen;

public final class CatalogueConfigFactory {
	private CatalogueConfigFactory() {
	}

	public static Screen createConfigScreen(Screen parent, ModContainer container) {
		return new ConfigSearchScreen(parent);
	}
}
