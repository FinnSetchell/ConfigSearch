package dev.finndog.configsearch.mixin;

import com.terraformersmc.modmenu.gui.ModsScreen;
import dev.finndog.configsearch.gui.SearchButton;
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
		this.addRenderableWidget(SearchButton.create(this, this.searchRowWidth + 2, 22));
	}
}
