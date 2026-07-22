package dev.finndog.configsearch.integration.midnightlib;

import dev.finndog.configsearch.api.ConfigOptionEntry;
import dev.finndog.configsearch.api.ExtractionContext;
import dev.finndog.configsearch.api.GlobalOptionProvider;
import dev.finndog.configsearch.api.ScreenOpener;
import dev.finndog.configsearch.api.ScreenOptionExtractor;
import eu.midnightdust.lib.config.MidnightConfig;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.neoforged.fml.ModList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MidnightLibExtractor implements ScreenOptionExtractor, GlobalOptionProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger("configsearch");

	@Override
	public boolean supports(Screen screen) {
		return false;
	}

	@Override
	public List<ConfigOptionEntry> extract(ExtractionContext context) {
		return List.of();
	}

	@Override
	public List<ConfigOptionEntry> scanAll() {
		List<ConfigOptionEntry> entries = new ArrayList<>();
		try {
			for (Map.Entry<String, MidnightConfig> registered : new LinkedHashMap<>(MidnightConfig.configInstances).entrySet()) {
				try {
					scanConfig(registered.getKey(), registered.getValue(), entries);
				} catch (Throwable t) {
					LOGGER.warn("Failed to index MidnightLib config for mod {}", registered.getKey(), t);
				}
			}
		} catch (Throwable t) {
			LOGGER.warn("Failed to enumerate MidnightLib configs", t);
		}
		return entries;
	}

	private static void scanConfig(String modid, MidnightConfig config, List<ConfigOptionEntry> entries) {
		if (modid == null || config == null || config.configClass == null) {
			return;
		}
		Component modName = ModList.get().getModContainerById(modid)
			.map(container -> Component.literal(container.getModInfo().getDisplayName()))
			.orElseGet(() -> Component.literal(modid));
		ScreenOpener opener = parent -> MidnightConfig.getScreen(parent, modid);
		for (Field field : config.configClass.getFields()) {
			MidnightConfig.Entry entry = field.getAnnotation(MidnightConfig.Entry.class);
			if (entry == null
				|| field.isAnnotationPresent(MidnightConfig.Server.class)
				|| field.isAnnotationPresent(MidnightConfig.Hidden.class)) {
				continue;
			}
			String translationKey = entry.name().isEmpty() ? modid + ".midnightconfig." + field.getName() : entry.name();
			entries.add(new ConfigOptionEntry(
				modid,
				modName,
				categoryPath(modid, entry.category()),
				Component.translatable(translationKey),
				description(translationKey),
				opener
			));
		}
	}

	private static List<Component> categoryPath(String modid, String category) {
		String categoryKey = modid + ".midnightconfig.category." + category;
		if (category.equals("default") && !Language.getInstance().has(categoryKey)) {
			return List.of();
		}
		return List.of(Component.translatable(categoryKey));
	}

	@Nullable
	private static Component description(String translationKey) {
		String tooltipKey = translationKey + ".tooltip";
		return Language.getInstance().has(tooltipKey) ? Component.translatable(tooltipKey) : null;
	}
}
