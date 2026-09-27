package dev.finndog.configsearch.compat;

import dev.finndog.configsearch.api.ScreenOpener;
import dev.finndog.configsearch.gui.SearchButton;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CatalogueCompat {
	private static final Logger LOGGER = LoggerFactory.getLogger("configsearch");
	private static final String SCREEN_CLASS = "com.mrcrayfish.catalogue.client.screen.CatalogueModListScreen";
	private static final int GAP = 2;

	private CatalogueCompat() {
	}

	public static void register() {
		ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
			if (screen.getClass().getName().equals(SCREEN_CLASS)) {
				addSearchButton(screen);
			}
		});
	}

	private static void addSearchButton(Screen screen) {
		List<AbstractWidget> widgets = Screens./*? if >= 26.1 {*//*getWidgets*//*?} else {*/getButtons/*?}*/(screen);
		Button back = null;
		for (AbstractWidget widget : widgets) {
			if (widget instanceof Button button && button.getMessage().getContents() instanceof TranslatableContents tc && tc.getKey().equals("gui.back")) {
				back = button;
				break;
			}
		}
		if (back == null) {
			return;
		}
		int shrunk = back.getWidth() - SearchButton.SIZE - GAP;
		if (shrunk < 40) {
			return;
		}
		back.setWidth(shrunk);
		widgets.add(SearchButton.create(screen, back.getX() + shrunk + GAP, back.getY()));
	}

	@SuppressWarnings("unchecked")
	public static Map<String, ScreenOpener> collectFactories() {
		Map<String, ScreenOpener> factories = new LinkedHashMap<>();
		try {
			Object providers = Class.forName("com.mrcrayfish.catalogue.Catalogue").getMethod("getConfigProviders").invoke(null);
			if (!(providers instanceof Map<?, ?> map)) {
				return factories;
			}
			((Map<String, BiFunction<Screen, ModContainer, Screen>>) map).forEach((modId, factory) -> {
				if (modId.equals("configsearch")) {
					return;
				}
				FabricLoader.getInstance().getModContainer(modId)
					.ifPresent(container -> factories.put(modId, parent -> factory.apply(parent, container)));
			});
		} catch (Throwable t) {
			LOGGER.warn("Failed to read Catalogue config screen factories", t);
		}
		return factories;
	}
}
