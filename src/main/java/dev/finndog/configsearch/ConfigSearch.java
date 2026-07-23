package dev.finndog.configsearch;

import dev.finndog.configsearch.gui.ConfigSearchScreen;
//? if < 1.21.1 {
/*import dev.finndog.configsearch.gui.IconButton;
*///?}
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
//? if >= 1.21.1 {
import net.minecraft.client.gui.components.SpriteIconButton;
//?}
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
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
		Button configButton = findButtonByKey(event, "fml.menu.mods.config");
		if (configButton == null) {
			return;
		}
		int shrunk = configButton.getWidth() - BUTTON_SIZE - GAP;
		if (shrunk < 40) {
			return;
		}
		configButton.setWidth(shrunk);

		Component tooltip = Component.translatable("configsearch.button.tooltip");
		int buttonX = configButton.getX() + shrunk + GAP;
		int buttonY = configButton.getY();
		//? if >= 1.21.1 {
		SpriteIconButton button = SpriteIconButton.builder(tooltip,
				b -> Minecraft.getInstance().setScreen(new ConfigSearchScreen(screen)), true)
			.size(BUTTON_SIZE, BUTTON_SIZE)
			.sprite(ResourceLocation.fromNamespaceAndPath(MOD_ID, "search"), 16, 16)
			.build();
		button.setPosition(buttonX, buttonY);
		//?} else {
		/*IconButton button = new IconButton(buttonX, buttonY, BUTTON_SIZE, BUTTON_SIZE, tooltip,
			new ResourceLocation(MOD_ID, "textures/gui/sprites/search.png"),
			b -> Minecraft.getInstance().setScreen(new ConfigSearchScreen(screen)));
		*///?}
		button.setTooltip(Tooltip.create(tooltip));
		event.addListener(button);
	}

	private static Button findButtonByKey(ScreenEvent.Init.Post event, String translationKey) {
		for (GuiEventListener listener : event.getListenersList()) {
			if (!(listener instanceof Button candidate)) {
				continue;
			}
			Component message = candidate.getMessage();
			if (message != null && message.getContents() instanceof TranslatableContents tc && translationKey.equals(tc.getKey())) {
				return candidate;
			}
		}
		return null;
	}
}
