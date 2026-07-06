package dev.finndog.configsearch.mixin;

import com.terraformersmc.modmenu.gui.ModsScreen;
import dev.finndog.configsearch.gui.ConfigSearchScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModsScreen.class)
public abstract class ModsScreenMixin extends Screen {
	@Shadow(remap = false)
	private int searchRowWidth;

	protected ModsScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "init", at = @At("TAIL"), require = 0)
	private void configsearch$addSearchButton(CallbackInfo ci) {
		this.addRenderableWidget(Button.builder(Component.translatable("configsearch.button"),
				button -> this.minecraft/*? if >= 26.2 {*//*.gui*//*?}*/.setScreen(new ConfigSearchScreen(this)))
			.bounds(this.searchRowWidth + 2, 22, 20, 20)
			.tooltip(Tooltip.create(Component.translatable("configsearch.button.tooltip")))
			.build());
	}
}
