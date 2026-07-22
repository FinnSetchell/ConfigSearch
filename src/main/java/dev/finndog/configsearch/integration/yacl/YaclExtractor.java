package dev.finndog.configsearch.integration.yacl;

import dev.finndog.configsearch.api.ConfigOptionEntry;
import dev.finndog.configsearch.api.ExtractionContext;
import dev.finndog.configsearch.api.ScreenOptionExtractor;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.LabelOption;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.gui.YACLScreen;
import java.util.ArrayList;
import java.util.List;
import dev.finndog.configsearch.api.ModInfo;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class YaclExtractor implements ScreenOptionExtractor {
	private static final Logger LOGGER = LoggerFactory.getLogger("configsearch");

	@Override
	public boolean supports(Screen screen) {
		return screen instanceof YACLScreen;
	}

	@Override
	public List<ConfigOptionEntry> extract(ExtractionContext context) {
		List<ConfigOptionEntry> entries = new ArrayList<>();
		try {
			YetAnotherConfigLib config = ((YACLScreen) context.screen()).config;
			ModInfo metadata = context.mod();
			String modId = metadata.id();
			Component modName = Component.literal(metadata.name());
			for (ConfigCategory category : config.categories()) {
				for (OptionGroup group : category.groups()) {
					if (group instanceof ListOption<?> listOption) {
						entries.add(entry(context, modId, modName, List.of(category.name()), listOption));
						continue;
					}
					List<Component> categoryPath = group.isRoot()
						? List.of(category.name())
						: List.of(category.name(), group.name());
					for (Option<?> option : group.options()) {
						if (option instanceof LabelOption) {
							continue;
						}
						entries.add(entry(context, modId, modName, categoryPath, option));
					}
				}
			}
		} catch (Throwable t) {
			LOGGER.warn("Failed to index YACL config screen for mod {}", context.mod().id(), t);
			return List.copyOf(entries);
		}
		return List.copyOf(entries);
	}

	private static ConfigOptionEntry entry(
		ExtractionContext context,
		String modId,
		Component modName,
		List<Component> categoryPath,
		Option<?> option
	) {
		return new ConfigOptionEntry(modId, modName, categoryPath, option.name(), description(option), context::freshScreen);
	}

	private static @Nullable Component description(Option<?> option) {
		OptionDescription description = option.description();
		if (description == null) {
			return null;
		}
		Component text = description.text();
		if (text == null || text.getString().isEmpty()) {
			return null;
		}
		return text;
	}
}
