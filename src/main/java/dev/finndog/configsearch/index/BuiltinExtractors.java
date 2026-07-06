package dev.finndog.configsearch.index;

import dev.finndog.configsearch.api.ScreenOptionExtractor;
import dev.finndog.configsearch.integration.cloth.ClothConfigExtractor;
import dev.finndog.configsearch.integration.midnightlib.MidnightLibExtractor;
import dev.finndog.configsearch.integration.yacl.YaclExtractor;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;

public final class BuiltinExtractors {
	private BuiltinExtractors() {
	}

	public static List<ScreenOptionExtractor> createAll() {
		FabricLoader loader = FabricLoader.getInstance();
		List<ScreenOptionExtractor> extractors = new ArrayList<>();
		if (loader.isModLoaded("yet_another_config_lib_v3")) {
			extractors.add(new YaclExtractor());
		}
		if (loader.isModLoaded("cloth-config")) {
			extractors.add(new ClothConfigExtractor());
		}
		if (loader.isModLoaded("midnightlib")) {
			extractors.add(new MidnightLibExtractor());
		}
		return List.copyOf(extractors);
	}
}
