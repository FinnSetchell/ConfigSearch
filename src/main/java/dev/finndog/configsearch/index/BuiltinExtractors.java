package dev.finndog.configsearch.index;

import dev.finndog.configsearch.api.ScreenOptionExtractor;
import dev.finndog.configsearch.integration.cloth.ClothConfigExtractor;
import dev.finndog.configsearch.integration.configured.ConfiguredExtractor;
import dev.finndog.configsearch.integration.midnightlib.MidnightLibExtractor;
import dev.finndog.configsearch.integration.neoforge.NeoForgeConfigRegistryExtractor;
import dev.finndog.configsearch.integration.yacl.YaclExtractor;
import java.util.ArrayList;
import java.util.List;
import net.neoforged.fml.ModList;

public final class BuiltinExtractors {
	private BuiltinExtractors() {
	}

	public static List<ScreenOptionExtractor> createAll() {
		ModList loader = ModList.get();
		List<ScreenOptionExtractor> extractors = new ArrayList<>();
		extractors.add(new NeoForgeConfigRegistryExtractor());
		if (loader.isLoaded("yet_another_config_lib_v3")) {
			extractors.add(new YaclExtractor());
		}
		if (loader.isLoaded("cloth_config")) {
			extractors.add(new ClothConfigExtractor());
		}
		if (loader.isLoaded("midnightlib")) {
			extractors.add(new MidnightLibExtractor());
		}
		//? if < 26.2 {
		if (loader.isLoaded("configured")) {
			extractors.add(new ConfiguredExtractor());
		}
		//?}
		return List.copyOf(extractors);
	}
}
