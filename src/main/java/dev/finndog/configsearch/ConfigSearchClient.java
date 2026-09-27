package dev.finndog.configsearch;

import dev.finndog.configsearch.compat.CatalogueCompat;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class ConfigSearchClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		if (FabricLoader.getInstance().isModLoaded("catalogue")) {
			CatalogueCompat.register();
		}
	}
}
