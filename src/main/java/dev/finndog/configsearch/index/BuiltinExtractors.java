package dev.finndog.configsearch.index;

import dev.finndog.configsearch.api.ScreenOptionExtractor;
//? if < 26.1 {
import dev.finndog.configsearch.integration.cloth.ClothConfigExtractor;
import dev.finndog.configsearch.integration.configured.ConfiguredExtractor;
import net.minecraftforge.fml.ModList;
//?}
import java.util.ArrayList;
import java.util.List;

public final class BuiltinExtractors {
	private BuiltinExtractors() {
	}

	public static List<ScreenOptionExtractor> createAll() {
		List<ScreenOptionExtractor> extractors = new ArrayList<>();
		//? if < 26.1 {
		ModList loader = ModList.get();
		if (loader.isLoaded("cloth_config")) {
			extractors.add(new ClothConfigExtractor());
		}
		if (loader.isLoaded("configured")) {
			extractors.add(new ConfiguredExtractor());
		}
		//?}
		return List.copyOf(extractors);
	}
}
