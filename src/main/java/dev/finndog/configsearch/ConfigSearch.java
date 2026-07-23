package dev.finndog.configsearch;

import dev.finndog.configsearch.gui.ConfigSearchScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.ModListScreen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(ConfigSearch.MOD_ID)
public final class ConfigSearch {
	public static final String MOD_ID = "configsearch";
	private static final int BUTTON_SIZE = 20;
	private static final int GAP = 2;

	public ConfigSearch() {
		if (FMLEnvironment.dist == Dist.CLIENT) {
			MinecraftForge.EVENT_BUS.addListener(ConfigSearch::onScreenInit);
		}
	}

	private static void onScreenInit(ScreenEvent.Init.Post event) {
		Screen screen = event.getScreen();
		if (!(screen instanceof ModListScreen)) {
			return;
		}
		EditBox searchBox = findSearchBox(event);
		if (searchBox == null) {
			return;
		}
		Button configButton = findButtonBelow(event, searchBox);
		if (configButton == null) {
			return;
		}
		int shrunk = configButton.getWidth() - BUTTON_SIZE - GAP;
		if (shrunk < 40) {
			return;
		}
		configButton.setWidth(shrunk);

		Component tooltip = Component.translatable("configsearch.button.tooltip");
		SpriteIconButton button = SpriteIconButton.builder(tooltip,
				b -> Minecraft.getInstance().setScreen(new ConfigSearchScreen(screen)), true)
			.size(BUTTON_SIZE, BUTTON_SIZE)
			.sprite(ResourceLocation.fromNamespaceAndPath(MOD_ID, "search"), 16, 16)
			.build();
		button.setPosition(configButton.getX() + shrunk + GAP, configButton.getY());
		button.setTooltip(Tooltip.create(tooltip));
		event.addListener(button);
	}

	private static EditBox findSearchBox(ScreenEvent.Init.Post event) {
		for (GuiEventListener listener : event.getListenersList()) {
			if (listener instanceof EditBox editBox) {
				return editBox;
			}
		}
		return null;
	}

	private static Button findButtonBelow(ScreenEvent.Init.Post event, AbstractWidget anchor) {
		Button best = null;
		int anchorBottom = anchor.getY() + anchor.getHeight();
		int anchorX = anchor.getX();
		for (GuiEventListener listener : event.getListenersList()) {
			if (!(listener instanceof Button candidate)) {
				continue;
			}
			if (candidate.getX() != anchorX) {
				continue;
			}
			if (candidate.getY() < anchorBottom) {
				continue;
			}
			if (best == null || candidate.getY() < best.getY()) {
				best = candidate;
			}
		}
		return best;
	}
}
