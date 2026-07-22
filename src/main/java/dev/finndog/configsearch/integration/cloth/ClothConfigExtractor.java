package dev.finndog.configsearch.integration.cloth;

import dev.finndog.configsearch.api.ConfigOptionEntry;
import dev.finndog.configsearch.api.ExtractionContext;
import dev.finndog.configsearch.api.ScreenOpener;
import dev.finndog.configsearch.api.ScreenOptionExtractor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import me.shedaniel.clothconfig2.api.AbstractConfigEntry;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.gui.AbstractConfigScreen;
import me.shedaniel.clothconfig2.gui.entries.SubCategoryListEntry;
import me.shedaniel.clothconfig2.gui.entries.TextListEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClothConfigExtractor implements ScreenOptionExtractor {
	private static final Logger LOGGER = LoggerFactory.getLogger("configsearch");

	@Override
	public boolean supports(Screen screen) {
		return screen instanceof AbstractConfigScreen;
	}

	@Override
	public List<ConfigOptionEntry> extract(ExtractionContext context) {
		try {
			if (!(context.screen() instanceof AbstractConfigScreen configScreen)) {
				return List.of();
			}
			String modId = context.mod().id();
			Component modName = Component.literal(context.mod().name());
			List<ConfigOptionEntry> entries = new ArrayList<>();
			int categoryIndex = 0;
			for (Map.Entry<Component, List<AbstractConfigEntry<?>>> category : configScreen.getCategorizedEntries().entrySet()) {
				ScreenOpener opener = categoryOpener(context, categoryIndex);
				List<Component> categoryPath = List.of(category.getKey());
				for (AbstractConfigEntry<?> entry : category.getValue()) {
					collect(entries, modId, modName, categoryPath, entry, opener, true);
				}
				categoryIndex++;
			}
			return List.copyOf(entries);
		} catch (Throwable t) {
			LOGGER.warn("Failed to index Cloth Config screen for mod {}", context.mod().id(), t);
			return List.of();
		}
	}

	private static void collect(
		List<ConfigOptionEntry> entries,
		String modId,
		Component modName,
		List<Component> categoryPath,
		AbstractConfigEntry<?> entry,
		ScreenOpener opener,
		boolean recurse
	) {
		if (entry instanceof TextListEntry) {
			return;
		}
		Component optionName = entry.getFieldName();
		if (optionName == null) {
			return;
		}
		if (entry instanceof SubCategoryListEntry subCategory) {
			if (!recurse) {
				return;
			}
			List<Component> subPath = new ArrayList<>(categoryPath);
			subPath.add(optionName);
			for (AbstractConfigListEntry<?> child : subCategory.getValue()) {
				collect(entries, modId, modName, subPath, child, opener, false);
			}
			return;
		}
		entries.add(new ConfigOptionEntry(modId, modName, categoryPath, optionName, null, opener));
	}

	private static ScreenOpener categoryOpener(ExtractionContext context, int categoryIndex) {
		return parent -> {
			Screen fresh = context.freshScreen(parent);
			if (fresh instanceof AbstractConfigScreen configScreen) {
				try {
					if (configScreen.getCategorizedEntries().size() > categoryIndex) {
						configScreen.selectedCategoryIndex = categoryIndex;
					}
				} catch (Throwable ignored) {
				}
			}
			return fresh;
		};
	}
}
