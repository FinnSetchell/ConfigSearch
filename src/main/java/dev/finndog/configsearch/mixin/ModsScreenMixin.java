package dev.finndog.configsearch.mixin;

import com.terraformersmc.modmenu.gui.ModsScreen;
import dev.finndog.configsearch.gui.ConfigSearchScreen;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >= 1.21.11 {
/*import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.resources.Identifier;
*///?} else if >= 1.21.1 {
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.resources.ResourceLocation;
//?} else {
/*import dev.finndog.configsearch.gui.IconButton;
import net.minecraft.resources.ResourceLocation;
*///?}

@Mixin(ModsScreen.class)
public abstract class ModsScreenMixin extends Screen {
	@Shadow(remap = false)
	private int searchRowWidth;

	protected ModsScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "init", at = @At("TAIL"), require = 0)
	private void configsearch$addSearchButton(CallbackInfo ci) {
		Component message = Component.translatable("configsearch.button.tooltip");
		//? if >= 1.21.1 {
		SpriteIconButton button = SpriteIconButton.builder(message,
				b -> this.minecraft/*? if >= 26.2 {*//*.gui*//*?}*/.setScreen(new ConfigSearchScreen(this)), true)
			.size(20, 20)
			.sprite(/*? if >= 1.21.11 {*//*Identifier*//*?} else {*/ResourceLocation/*?}*/.fromNamespaceAndPath("configsearch", "search"), 16, 16)
			.build();
		button.setPosition(this.searchRowWidth + 2, 22);
		//?} else {
		/*IconButton button = new IconButton(this.searchRowWidth + 2, 22, 20, 20, message,
			new ResourceLocation("configsearch", "textures/gui/sprites/search.png"),
			b -> this.minecraft.setScreen(new ConfigSearchScreen(this)));
		*///?}
		button.setTooltip(Tooltip.create(Component.translatable("configsearch.button.tooltip")));
		this.addRenderableWidget(button);
	}
}
