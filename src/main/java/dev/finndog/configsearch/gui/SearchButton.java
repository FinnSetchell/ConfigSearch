package dev.finndog.configsearch.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
//? if >= 1.21.11 {
/*import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.resources.Identifier;
*///?} else if >= 1.21.1 {
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.resources.ResourceLocation;
//?} else {
/*import net.minecraft.resources.ResourceLocation;
*///?}

public final class SearchButton {
	public static final int SIZE = 20;

	private SearchButton() {
	}

	public static Button create(Screen parent, int x, int y) {
		Component message = Component.translatable("configsearch.button.tooltip");
		//? if >= 1.21.1 {
		SpriteIconButton button = SpriteIconButton.builder(message,
				b -> Minecraft.getInstance()/*? if >= 26.2 {*//*.gui*//*?}*/.setScreen(new ConfigSearchScreen(parent)), true)
			.size(SIZE, SIZE)
			.sprite(/*? if >= 1.21.11 {*//*Identifier*//*?} else {*/ResourceLocation/*?}*/.fromNamespaceAndPath("configsearch", "search"), 16, 16)
			.build();
		button.setPosition(x, y);
		//?} else {
		/*IconButton button = new IconButton(x, y, SIZE, SIZE, message,
			new ResourceLocation("configsearch", "textures/gui/sprites/search.png"),
			b -> Minecraft.getInstance().setScreen(new ConfigSearchScreen(parent)));
		*///?}
		button.setTooltip(Tooltip.create(message));
		return button;
	}
}
