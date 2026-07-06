package dev.finndog.configsearch.integration.cloth;

import dev.finndog.configsearch.api.ConfigOptionEntry;
import dev.finndog.configsearch.api.ExtractionContext;
import dev.finndog.configsearch.api.ScreenOptionExtractor;
import java.util.List;
import net.minecraft.client.gui.screens.Screen;

public final class ClothConfigExtractor implements ScreenOptionExtractor {
	@Override
	public boolean supports(Screen screen) {
		return false;
	}

	@Override
	public List<ConfigOptionEntry> extract(ExtractionContext context) {
		return List.of();
	}
}
