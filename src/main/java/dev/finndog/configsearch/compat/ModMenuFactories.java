package dev.finndog.configsearch.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.finndog.configsearch.api.ScreenOpener;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ModMenuFactories {
	private static final Logger LOGGER = LoggerFactory.getLogger("configsearch");

	private ModMenuFactories() {
	}

	public static Map<String, ScreenOpener> collect() {
		FabricLoader loader = FabricLoader.getInstance();
		Map<String, ScreenOpener> factories = new LinkedHashMap<>();
		Map<String, ScreenOpener> provided = new LinkedHashMap<>();
		for (var container : loader.getEntrypointContainers("modmenu", ModMenuApi.class)) {
			String providerId = container.getProvider().getMetadata().getId();
			if (providerId.equals("configsearch")) {
				continue;
			}
			try {
				ModMenuApi api = container.getEntrypoint();
				factories.putIfAbsent(providerId, opener(api.getModConfigScreenFactory()));
				api.getProvidedConfigScreenFactories().forEach((targetId, factory) -> {
					if (loader.isModLoaded(targetId)) {
						provided.putIfAbsent(targetId, opener(factory));
					}
				});
			} catch (Throwable t) {
				LOGGER.warn("Failed to read Mod Menu config screen factories from mod {}", providerId, t);
			}
		}
		provided.forEach(factories::putIfAbsent);
		return factories;
	}

	private static ScreenOpener opener(ConfigScreenFactory<?> factory) {
		return factory::create;
	}
}
