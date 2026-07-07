package dev.finndog.configsearch.gui;

//? if < 1.21.1 {
/*import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class IconButton extends Button {
	private final ResourceLocation texture;

	public IconButton(int x, int y, int width, int height, Component message, ResourceLocation texture, OnPress onPress) {
		super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
		this.texture = texture;
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.renderWidget(graphics, mouseX, mouseY, delta);
		graphics.blit(this.texture, this.getX() + 2, this.getY() + 2, 0.0F, 0.0F, 16, 16, 16, 16);
	}

	@Override
	public void renderString(GuiGraphics graphics, Font font, int color) {
	}
}
*///?} else {
public final class IconButton {
	private IconButton() {
	}
}
//?}
