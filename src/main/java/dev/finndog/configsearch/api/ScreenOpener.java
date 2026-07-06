package dev.finndog.configsearch.api;

import net.minecraft.client.gui.screens.Screen;

@FunctionalInterface
public interface ScreenOpener {
	Screen open(Screen parent);
}
