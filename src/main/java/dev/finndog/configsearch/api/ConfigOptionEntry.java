package dev.finndog.configsearch.api;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

public record ConfigOptionEntry(
	String modId,
	Component modName,
	List<Component> categoryPath,
	Component optionName,
	@Nullable Component description,
	ScreenOpener opener
) {
	private static final String SEPARATOR = " > ";

	public ConfigOptionEntry {
		categoryPath = List.copyOf(categoryPath);
	}

	public Component breadcrumb() {
		MutableComponent breadcrumb = modName.copy();
		for (Component category : categoryPath) {
			breadcrumb.append(SEPARATOR).append(category);
		}
		return breadcrumb.append(SEPARATOR).append(optionName);
	}
}
