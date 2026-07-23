package dev.finndog.configsearch.index;

import dev.finndog.configsearch.api.ConfigOptionEntry;
import dev.finndog.configsearch.api.ConfigSearchEntrypoint;
import dev.finndog.configsearch.api.ExtractionContext;
import dev.finndog.configsearch.api.GlobalOptionProvider;
import dev.finndog.configsearch.api.ModInfo;
import dev.finndog.configsearch.api.ScreenOpener;
import dev.finndog.configsearch.api.ScreenOptionExtractor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
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
		for (ConfigSearchEntrypoint entrypoint : ServiceLoader.load(ConfigSearchEntrypoint.class, OptionIndex.class.getClassLoader())) {
			try {
				entrypoint.register(registrar);
			} catch (Throwable t) {
				LOGGER.warn("Failed to collect config search registrations from {}", entrypoint.getClass().getName(), t);
			}
		}
		return new Registration(extractors, entriesByMod);
	}

	private Map<String, ConfigScreenHandler.ConfigScreenFactory> collectFactories() {
		Map<String, ConfigScreenHandler.ConfigScreenFactory> factories = new LinkedHashMap<>();
		for (IModInfo info : ModList.get().getMods()) {
			String modId = info.getModId();
			if (modId.equals("configsearch")) {
				continue;
			}
			ModContainer container = ModList.get().getModContainerById(modId).orElse(null);
			if (container == null) {
				continue;
			}
			try {
				Optional<ConfigScreenHandler.ConfigScreenFactory> factory =
					container.getCustomExtension(ConfigScreenHandler.ConfigScreenFactory.class);
				factory.ifPresent(f -> factories.put(modId, f));
			} catch (Throwable t) {
				LOGGER.warn("Failed to read config screen factory for mod {}", modId, t);
			}
		}
		return factories;
	}

	private List<ConfigOptionEntry> extractFromScreen(String modId, ConfigScreenHandler.ConfigScreenFactory factory, List<ScreenOptionExtractor> extractors) {
		ModContainer modContainer = ModList.get().getModContainerById(modId).orElse(null);
		if (modContainer == null) {
			return List.of();
		}
		ModInfo info = new ModInfo(modContainer.getModId(), modContainer.getModInfo().getDisplayName());
		ScreenOpener opener = parent -> factory.screenFunction().apply(Minecraft.getInstance(), parent);
		try {
			Screen screen = opener.open(throwawayParent());
			if (screen == null) {
				return List.of();
			}
			ExtractionContext context = new ExtractionContext(info, screen, opener);
			for (ScreenOptionExtractor extractor : extractors) {
				if (extractor.supports(screen)) {
					return List.copyOf(extractor.extract(context));
				}
			}
			return List.of(fallbackEntry(info, opener));
		} catch (Throwable t) {
			LOGGER.warn("Failed to index config screen for mod {}", modId, t);
			return List.of();
		}
	}

	private static ConfigOptionEntry fallbackEntry(ModInfo info, ScreenOpener opener) {
		Component modName = Component.literal(info.name());
		return new ConfigOptionEntry(
			info.id(),
			modName,
			List.of(),
			modName,
			Component.translatable("configsearch.result.screen_only"),
			opener
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
