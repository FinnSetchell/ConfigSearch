package dev.finndog.configsearch.integration.configured;

//? if < 26.2 {
import com.mrcrayfish.configured.api.ConfigType;
import com.mrcrayfish.configured.api.IConfigEntry;
import com.mrcrayfish.configured.api.IConfigValue;
import com.mrcrayfish.configured.api.IModConfig;
import com.mrcrayfish.configured.client.screen.ConfigScreen;
import com.mrcrayfish.configured.client.screen.ModConfigSelectionScreen;
import dev.finndog.configsearch.api.ConfigOptionEntry;
import dev.finndog.configsearch.api.ExtractionContext;
import dev.finndog.configsearch.api.ScreenOpener;
import dev.finndog.configsearch.api.ScreenOptionExtractor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConfiguredExtractor implements ScreenOptionExtractor {
	private static final Logger LOGGER = LoggerFactory.getLogger("configsearch");

	@Override
	public boolean supports(Screen screen) {
		return screen instanceof ConfigScreen || screen instanceof ModConfigSelectionScreen;
	}

	@Override
	public List<ConfigOptionEntry> extract(ExtractionContext context) {
		try {
			String modId = context.mod().id();
			Component modName = Component.literal(context.mod().name());
			ScreenOpener opener = context::freshScreen;
			Screen screen = context.screen();
			List<IModConfig> configs = new ArrayList<>();
			boolean multi = false;
			if (screen instanceof ConfigScreen configScreen) {
				configs.add(configScreen.getActiveConfig());
			} else if (screen instanceof ModConfigSelectionScreen selectionScreen) {
				multi = true;
				configs.addAll(readConfigMap(selectionScreen, modId));
			}
			List<ConfigOptionEntry> entries = new ArrayList<>();
			for (IModConfig config : configs) {
				if (config == null) {
					continue;
				}
				ConfigType type = config.getType();
				if (isServerOrWorld(type)) {
					continue;
				}
				try {
					List<Component> base = multi ? List.of(Component.literal(ModConfigSelectionScreen.createLabelFromModConfig(config))) : List.of();
					IConfigEntry root = rootEntry(config);
					collect(entries, modId, modName, opener, root, base);
				} catch (Throwable t) {
					LOGGER.warn("Failed to index Configured config for mod {}", modId, t);
				}
			}
			return List.copyOf(entries);
		} catch (Throwable t) {
			LOGGER.warn("Failed to index Configured config for mod {}", context.mod().id(), t);
			return List.of();
		}
	}

	private static List<IModConfig> readConfigMap(ModConfigSelectionScreen screen, String modId) {
		try {
			Field field = ModConfigSelectionScreen.class.getDeclaredField("configMap");
			field.setAccessible(true);
			@SuppressWarnings("unchecked")
			Map<ConfigType, Set<IModConfig>> map = (Map<ConfigType, Set<IModConfig>>) field.get(screen);
			List<IModConfig> configs = new ArrayList<>();
			if (map != null) {
				for (Map.Entry<ConfigType, Set<IModConfig>> mapEntry : map.entrySet()) {
					if (isServerOrWorld(mapEntry.getKey())) {
						continue;
					}
					if (mapEntry.getValue() != null) {
						configs.addAll(mapEntry.getValue());
					}
				}
			}
			return configs;
		} catch (Throwable t) {
			LOGGER.warn("Failed to read Configured config map for mod {}", modId, t);
			return List.of();
		}
	}

	private static boolean isServerOrWorld(@Nullable ConfigType type) {
		return type == null || type.isServer() || type == ConfigType.WORLD || type == ConfigType.WORLD_SYNC;
	}

	private static IConfigEntry rootEntry(IModConfig config) throws ReflectiveOperationException {
		try {
			return (IConfigEntry) IModConfig.class.getMethod("createRootEntry").invoke(config);
		} catch (NoSuchMethodException e) {
			return (IConfigEntry) IModConfig.class.getMethod("getRoot").invoke(config);
		}
	}

	private static void collect(
		List<ConfigOptionEntry> entries,
		String modId,
		Component modName,
		ScreenOpener opener,
		IConfigEntry entry,
		List<Component> categoryPath
	) {
		for (IConfigEntry child : entry.getChildren()) {
			if (child.isLeaf()) {
				IConfigValue<?> value = child.getValue();
				String fallback = value != null ? value.getName() : child.getEntryName();
				Component optionName = displayName(child.getTranslationKey(), fallback);
				Component description = description(child);
				entries.add(new ConfigOptionEntry(modId, modName, categoryPath, optionName, description, opener));
			} else {
				Component folderName = displayName(child.getTranslationKey(), child.getEntryName());
				collect(entries, modId, modName, opener, child, append(categoryPath, folderName));
			}
		}
	}

	private static Component displayName(@Nullable String translationKey, String fallbackName) {
		if (translationKey != null && !translationKey.isEmpty() && Language.getInstance().has(translationKey)) {
			return Component.translatable(translationKey);
		}
		return Component.literal(fallbackName);
	}

	@Nullable
	private static Component description(IConfigEntry entry) {
		Component tooltip = entry.getTooltip();
		if (isPresent(tooltip)) {
			return tooltip;
		}
		IConfigValue<?> value = entry.getValue();
		if (value != null) {
			Component comment = value.getComment();
			if (isPresent(comment)) {
				return comment;
			}
		}
		return null;
	}

	private static boolean isPresent(@Nullable Component component) {
		return component != null && !component.getString().isBlank();
	}

	private static <T> List<T> append(List<T> list, T value) {
		List<T> result = new ArrayList<>(list);
		result.add(value);
		return List.copyOf(result);
	}
}
//?} else {
/*public final class ConfiguredExtractor {
	private ConfiguredExtractor() {
	}
}
*///?}
