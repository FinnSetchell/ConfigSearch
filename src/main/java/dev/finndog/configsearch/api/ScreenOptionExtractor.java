package dev.finndog.configsearch.api;

import java.util.List;
import net.minecraft.client.gui.screens.Screen;

public interface ScreenOptionExtractor {
	boolean supports(Screen screen);

	List<ConfigOptionEntry> extract(ExtractionContext context);
}
