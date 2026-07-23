package dev.finndog.configsearch.index;

import dev.finndog.configsearch.api.ScreenOptionExtractor;
import dev.finndog.configsearch.integration.cloth.ClothConfigExtractor;
import dev.finndog.configsearch.integration.configured.ConfiguredExtractor;
import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.fml.ModList;

public final class BuiltinExtractors {
	private BuiltinExtractors() {
	}

	public static List<ScreenOptionExtractor> createAll() {
		ModList loader = ModList.get();
		List<ScreenOptionExtractor> extractors = new ArrayList<>();
		if (loader.isLoaded("cloth_config")) {
			extractors.add(new ClothConfigExtractor());
		}
		if (loader.isLoaded("configured")) {
			extractors.add(new ConfiguredExtractor());
		}
		return List.copyOf(extractors);
	}
}
