package dev.finndog.configsearch.api;

import java.util.Collection;

public interface ConfigSearchEntrypoint {
	String KEY = "configsearch";

	void register(Registrar registrar);

	interface Registrar {
		void addExtractor(ScreenOptionExtractor extractor);

		void addEntries(String modId, Collection<ConfigOptionEntry> entries);
	}
}
