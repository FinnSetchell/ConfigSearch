package dev.finndog.configsearch;

import dev.finndog.configsearch.gui.ConfigSearchScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.ModListScreen;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = ConfigSearch.MOD_ID, dist = Dist.CLIENT)
public final class ConfigSearch {
	public static final String MOD_ID = "configsearch";

	public ConfigSearch(IEventBus modBus) {
		NeoForge.EVENT_BUS.addListener(ConfigSearch::onScreenInit);
	}

	private static void onScreenInit(ScreenEvent.Init.Post event) {
		Screen screen = event.getScreen();
		if (!(screen instanceof ModListScreen)) {
			return;
		}
		Component tooltip = Component.translatable("configsearch.button.tooltip");
		SpriteIconButton button = SpriteIconButton.builder(tooltip,
				b -> Minecraft.getInstance().setScreen(new ConfigSearchScreen(screen)), true)
			.size(20, 20)
			.sprite(ResourceLocation.fromNamespaceAndPath(MOD_ID, "search"), 16, 16)
			.build();
		button.setPosition(screen.width - 24, 4);
		button.setTooltip(Tooltip.create(tooltip));
		event.addListener(button);
	}
}
