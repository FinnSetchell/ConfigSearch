package dev.finndog.configsearch.index;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.finndog.configsearch.api.ConfigOptionEntry;
import dev.finndog.configsearch.api.ConfigSearchEntrypoint;
import dev.finndog.configsearch.api.ExtractionContext;
import dev.finndog.configsearch.api.GlobalOptionProvider;
import dev.finndog.configsearch.api.ScreenOptionExtractor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OptionIndex {
	private static final Logger LOGGER = LoggerFactory.getLogger("configsearch");
	private static final OptionIndex INSTANCE = new OptionIndex();

	private List<ConfigOptionEntry> entries;
	private int indexedModCount;
	private String builtLanguage;

	private OptionIndex() {
	}

	public static OptionIndex getInstance() {
		return INSTANCE;
	}

	public List<SearchResult> search(String query) {
		ensureBuilt();
		return OptionMatcher.match(entries, query);
	}

	public int modCount() {
		ensureBuilt();
		return indexedModCount;
	}

	public int optionCount() {
		ensureBuilt();
		return entries.size();
	}

	public void invalidate() {
		entries = null;
	}

	private void ensureBuilt() {
		String language = Minecraft.getInstance().getLanguageManager().getSelected();
		if (entries == null || !language.equals(builtLanguage)) {
			build(language);
		}
	}

	private void build(String language) {
		long start = System.nanoTime();
		Registration registration = collectRegistrations();
		List<ScreenOptionExtractor> extractors = new ArrayList<>(registration.extractors());
		extractors.addAll(BuiltinExtractors.createAll());

		Map<String, List<ConfigOptionEntry>> entriesByMod = new LinkedHashMap<>(registration.entriesByMod());
		for (ScreenOptionExtractor extractor : extractors) {
			if (extractor instanceof GlobalOptionProvider provider) {
				for (ConfigOptionEntry entry : scanAll(provider)) {
					entriesByMod.computeIfAbsent(entry.modId(), id -> new ArrayList<>()).add(entry);
				}
			}
		}

		collectFactories().forEach((modId, factory) -> {
			if (entriesByMod.containsKey(modId)) {
				return;
			}
			List<ConfigOptionEntry> extracted = extractFromScreen(modId, factory, extractors);
			if (!extracted.isEmpty()) {
				entriesByMod.put(modId, extracted);
			}
		});

		entries = entriesByMod.values().stream().flatMap(List::stream).toList();
		indexedModCount = entriesByMod.size();
		builtLanguage = language;
		long elapsedMs = (System.nanoTime() - start) / 1_000_000L;
		LOGGER.info("Indexed {} options from {} mods in {}ms", entries.size(), indexedModCount, elapsedMs);
	}

	private Registration collectRegistrations() {
		List<ScreenOptionExtractor> extractors = new ArrayList<>();
		Map<String, List<ConfigOptionEntry>> entriesByMod = new LinkedHashMap<>();
		ConfigSearchEntrypoint.Registrar registrar = new ConfigSearchEntrypoint.Registrar() {
			@Override
			public void addExtractor(ScreenOptionExtractor extractor) {
				extractors.add(extractor);
			}

			@Override
			public void addEntries(String modId, Collection<ConfigOptionEntry> newEntries) {
				entriesByMod.computeIfAbsent(modId, id -> new ArrayList<>()).addAll(newEntries);
			}
		};
		for (var container : FabricLoader.getInstance().getEntrypointContainers(ConfigSearchEntrypoint.KEY, ConfigSearchEntrypoint.class)) {
			try {
				container.getEntrypoint().register(registrar);
			} catch (Throwable t) {
				LOGGER.warn("Failed to collect config search registrations from mod {}", container.getProvider().getMetadata().getId(), t);
			}
		}
		return new Registration(extractors, entriesByMod);
	}

	private Map<String, ConfigScreenFactory<?>> collectFactories() {
		FabricLoader loader = FabricLoader.getInstance();
		Map<String, ConfigScreenFactory<?>> factories = new LinkedHashMap<>();
		Map<String, ConfigScreenFactory<?>> provided = new LinkedHashMap<>();
		for (var container : loader.getEntrypointContainers("modmenu", ModMenuApi.class)) {
			String providerId = container.getProvider().getMetadata().getId();
			try {
				ModMenuApi api = container.getEntrypoint();
				factories.putIfAbsent(providerId, api.getModConfigScreenFactory());
				api.getProvidedConfigScreenFactories().forEach((targetId, factory) -> {
					if (loader.isModLoaded(targetId)) {
						provided.putIfAbsent(targetId, factory);
					}
				});
			} catch (Throwable t) {
				LOGGER.warn("Failed to read Mod Menu config screen factories from mod {}", providerId, t);
			}
		}
		provided.forEach(factories::putIfAbsent);
		return factories;
	}

	private List<ConfigOptionEntry> extractFromScreen(String modId, ConfigScreenFactory<?> factory, List<ScreenOptionExtractor> extractors) {
		Optional<ModContainer> mod = FabricLoader.getInstance().getModContainer(modId);
		if (mod.isEmpty()) {
			return List.of();
		}
		try {
			Screen screen = factory.create(throwawayParent());
			if (screen == null) {
				return List.of();
			}
			ExtractionContext context = new ExtractionContext(mod.get(), screen, factory::create);
			for (ScreenOptionExtractor extractor : extractors) {
				if (extractor.supports(screen)) {
					return List.copyOf(extractor.extract(context));
				}
			}
			return List.of(fallbackEntry(mod.get(), factory));
		} catch (Throwable t) {
			LOGGER.warn("Failed to index config screen for mod {}", modId, t);
			return List.of();
		}
	}

	private static ConfigOptionEntry fallbackEntry(ModContainer mod, ConfigScreenFactory<?> factory) {
		Component modName = Component.literal(mod.getMetadata().getName());
		return new ConfigOptionEntry(
			mod.getMetadata().getId(),
			modName,
			List.of(),
			modName,
			Component.translatable("configsearch.result.screen_only"),
			factory::create
		);
	}

	private static List<ConfigOptionEntry> scanAll(GlobalOptionProvider provider) {
		try {
			return provider.scanAll();
		} catch (Throwable t) {
			LOGGER.warn("Global option provider {} failed", provider.getClass().getName(), t);
			return List.of();
		}
	}

	private static Screen throwawayParent() {
		return new Screen(Component.empty()) {
		};
	}

	private record Registration(List<ScreenOptionExtractor> extractors, Map<String, List<ConfigOptionEntry>> entriesByMod) {
	}
}
