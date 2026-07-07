package dev.finndog.configsearch.integration.forgeconfigapiport;

//? if >= 1.21.1 {
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import dev.finndog.configsearch.api.ConfigOptionEntry;
import dev.finndog.configsearch.api.ExtractionContext;
import dev.finndog.configsearch.api.ScreenOpener;
import dev.finndog.configsearch.api.ScreenOptionExtractor;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfigs;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ForgeConfigApiPortExtractor implements ScreenOptionExtractor {
	private static final Logger LOGGER = LoggerFactory.getLogger("configsearch");

	@Override
	public boolean supports(Screen screen) {
		return screen instanceof ConfigurationScreen;
	}

	@Override
	public List<ConfigOptionEntry> extract(ExtractionContext context) {
		try {
			String modId = context.mod().getMetadata().getId();
			Component modName = Component.literal(context.mod().getMetadata().getName());
			ScreenOpener opener = context::freshScreen;
			List<ConfigOptionEntry> entries = new ArrayList<>();
			for (ModConfig config : ModConfigs.getModConfigs(modId)) {
				ModConfig.Type type = config.getType();
				if (type != ModConfig.Type.CLIENT && type != ModConfig.Type.COMMON) {
					continue;
				}
				if (!(config.getSpec() instanceof ModConfigSpec spec)) {
					continue;
				}
				Component typeLabel = Component.translatable("neoforge.configuration.uitext.type." + type.name().toLowerCase(Locale.ROOT));
				collect(entries, modId, modName, opener, spec, spec.getSpec(), List.of(), List.of(typeLabel));
			}
			return List.copyOf(entries);
		} catch (Throwable t) {
			LOGGER.warn("Failed to index Forge Config API Port config for mod {}", context.mod().getMetadata().getId(), t);
			return List.of();
		}
	}

	private static void collect(
		List<ConfigOptionEntry> entries,
		String modId,
		Component modName,
		ScreenOpener opener,
		ModConfigSpec spec,
		UnmodifiableConfig node,
		List<String> path,
		List<Component> categoryPath
	) {
		for (UnmodifiableConfig.Entry entry : node.entrySet()) {
			String key = entry.getKey();
			Object raw = entry.getRawValue();
			List<String> childPath = append(path, key);
			if (raw instanceof ModConfigSpec.ValueSpec valueSpec) {
				Component optionName = displayName(valueSpec.getTranslationKey(), key);
				Component description = description(valueSpec.getTranslationKey(), valueSpec.getComment());
				entries.add(new ConfigOptionEntry(modId, modName, categoryPath, optionName, description, opener));
			} else if (raw instanceof UnmodifiableConfig group) {
				Component groupName = displayName(spec.getLevelTranslationKey(childPath), key);
				collect(entries, modId, modName, opener, spec, group, childPath, append(categoryPath, groupName));
			}
		}
	}

	private static Component displayName(@Nullable String translationKey, String key) {
		if (translationKey != null && !translationKey.isEmpty()) {
			return Component.translatable(translationKey);
		}
		return Component.literal(key);
	}

	@Nullable
	private static Component description(@Nullable String translationKey, @Nullable String comment) {
		if (translationKey != null && !translationKey.isEmpty()) {
			String tooltipKey = translationKey + ".tooltip";
			if (Language.getInstance().has(tooltipKey)) {
				return Component.translatable(tooltipKey);
			}
		}
		if (comment != null && !comment.isBlank()) {
			return Component.literal(comment);
		}
		return null;
	}

	private static <T> List<T> append(List<T> list, T value) {
		List<T> result = new ArrayList<>(list);
		result.add(value);
		return List.copyOf(result);
	}
}
//?} else {
/*public final class ForgeConfigApiPortExtractor {
	private ForgeConfigApiPortExtractor() {
	}
}
*///?}
