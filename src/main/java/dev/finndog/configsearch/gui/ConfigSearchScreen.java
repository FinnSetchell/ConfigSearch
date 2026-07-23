package dev.finndog.configsearch.gui;

import dev.finndog.configsearch.api.ConfigOptionEntry;
import dev.finndog.configsearch.index.OptionIndex;
import dev.finndog.configsearch.index.SearchResult;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//? if >= 26.1.2 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?} else {
import net.minecraft.client.gui.GuiGraphics;
//?}
//? if >= 1.21.11 {
/*import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
*///?}

public final class ConfigSearchScreen extends Screen {
	private static final Logger LOGGER = LoggerFactory.getLogger("configsearch");
	private static final int LIST_TOP = 48;
	private static final int LIST_BOTTOM_MARGIN = 36;
	private static final int ROW_HEIGHT = 36;
	private static final int WHITE = 0xFFFFFFFF;
	private static final int GRAY = 0xFFAAAAAA;

	private final Screen parent;
	private String query = "";
	private EditBox searchBox;
	private ResultsList resultsList;
	private Component status;

	public ConfigSearchScreen(Screen parent) {
		super(Component.translatable("configsearch.screen.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		Component hint = Component.translatable("configsearch.screen.hint");
		this.searchBox = new EditBox(this.font, this.width / 2 - 110, 22, 220, 20, hint);
		this.searchBox.setHint(hint);
		this.searchBox.setMaxLength(256);
		this.searchBox.setResponder(this::onQueryChanged);
		this.resultsList = new ResultsList(this.minecraft, this.width, this.height);
		this.addRenderableWidget(this.resultsList);
		this.addRenderableWidget(this.searchBox);
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
			.bounds(this.width / 2 - 100, this.height - 27, 200, 20)
			.build());
		this.searchBox.setValue(this.query);
		this.updateResults();
		this.setInitialFocus(this.searchBox);
	}

	//? if < 1.21 {
	/*@Override
	public void tick() {
		this.searchBox.tick();
	}
	*///?}

	//? if >= 26.1.2 {
	/*@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		super.extractRenderState(graphics, mouseX, mouseY, delta);
		graphics.centeredText(this.font, this.title, this.width / 2, 8, WHITE);
		if (this.status != null) {
			graphics.centeredText(this.font, this.status, this.width / 2, this.statusY(), GRAY);
		}
		ResultEntry hovered = this.resultsList.hoveredEntry();
		if (hovered != null && hovered.option.description() != null) {
			graphics.setTooltipForNextFrame(this.font, hovered.option.description(), mouseX, mouseY);
		}
	}
	*///?} else {
	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		//? if < 1.21 {
		/*this.renderBackground(graphics);
		*///?}
		super.render(graphics, mouseX, mouseY, delta);
		graphics.drawCenteredString(this.font, this.title, this.width / 2, 8, WHITE);
		if (this.status != null) {
			graphics.drawCenteredString(this.font, this.status, this.width / 2, this.statusY(), GRAY);
		}
		ResultEntry hovered = this.resultsList.hoveredEntry();
		if (hovered != null && hovered.option.description() != null) {
			//? if >= 1.21.11 {
			/*graphics.setTooltipForNextFrame(this.font, hovered.option.description(), mouseX, mouseY);
			*///?} else {
			this.setTooltipForNextRenderPass(hovered.option.description());
			//?}
		}
	}
	//?}

	//? if >= 1.21.11 {
	/*@Override
	public boolean keyPressed(KeyEvent event) {
		if (super.keyPressed(event)) {
			return true;
		}
		if (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER) {
			return this.activateFirst();
		}
		return false;
	}
	*///?} else {
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (super.keyPressed(keyCode, scanCode, modifiers)) {
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			return this.activateFirst();
		}
		return false;
	}
	//?}

	@Override
	public void onClose() {
		this.minecraft/*? if >= 26.2 {*//*.gui*//*?}*/.setScreen(this.parent);
	}

	private int statusY() {
		return (LIST_TOP + this.height - LIST_BOTTOM_MARGIN) / 2 - 4;
	}

	private void onQueryChanged(String value) {
		if (!value.equals(this.query)) {
			this.query = value;
			this.updateResults();
		}
	}

	private void updateResults() {
		OptionIndex index = OptionIndex.getInstance();
		String trimmed = this.query.trim();
		if (trimmed.isEmpty()) {
			this.status = Component.translatable("configsearch.screen.stats", index.optionCount(), index.modCount());
			this.resultsList.setResults(List.of());
		} else {
			List<SearchResult> results = index.search(trimmed);
			this.status = results.isEmpty() ? Component.translatable("configsearch.screen.empty") : null;
			this.resultsList.setResults(results);
		}
	}

	private boolean activateFirst() {
		ResultEntry entry = this.resultsList.getSelected();
		if (entry == null && !this.resultsList.children().isEmpty()) {
			entry = this.resultsList.children().get(0);
		}
		if (entry == null) {
			return false;
		}
		this.openEntry(entry.option);
		return true;
	}

	private void openEntry(ConfigOptionEntry option) {
		try {
			Screen screen = option.opener().open(this);
			if (screen != null) {
				Minecraft.getInstance()/*? if >= 26.2 {*//*.gui*//*?}*/.setScreen(screen);
			}
		} catch (Throwable t) {
			LOGGER.warn("Failed to open config screen for mod {}", option.modId(), t);
		}
	}

	private final class ResultsList extends ObjectSelectionList<ResultEntry> {
		private ResultsList(Minecraft minecraft, int screenWidth, int screenHeight) {
			//? if >= 1.21.1 {
			super(minecraft, screenWidth, screenHeight - LIST_TOP - LIST_BOTTOM_MARGIN, LIST_TOP, ROW_HEIGHT);
			//?} else {
			/*super(minecraft, screenWidth, screenHeight, LIST_TOP, screenHeight - LIST_BOTTOM_MARGIN, ROW_HEIGHT);
			*///?}
		}

		@Override
		public int getRowWidth() {
			return Math.min(360, this.width - 40);
		}

		@Override
		protected int /*? if >= 1.21.11 {*/ /*scrollBarX*/ /*?} else {*/ getScrollbarPosition /*?}*/() {
			return this.width / 2 + this.getRowWidth() / 2 + 6;
		}

		private void setResults(List<SearchResult> results) {
			this.replaceEntries(results.stream().map(result -> new ResultEntry(result.entry())).toList());
			this.setSelected(null);
			this.setScrollAmount(0.0);
		}

		private ResultEntry hoveredEntry() {
			return this.getHovered();
		}
	}

	private final class ResultEntry extends ObjectSelectionList.Entry<ResultEntry> {
		private final ConfigOptionEntry option;
		private int cachedWidth = -1;
		private List<String> cachedLines = List.of();

		private ResultEntry(ConfigOptionEntry option) {
			this.option = option;
		}

		private List<String> breadcrumbLines() {
			int width = ConfigSearchScreen.this.resultsList.getRowWidth() - 4;
			if (width == this.cachedWidth) {
				return this.cachedLines;
			}
			var font = ConfigSearchScreen.this.font;
			String text = this.option.breadcrumb().getString();
			List<String> lines;
			if (font.width(text) <= width) {
				lines = List.of(text);
			} else {
				String first = font.plainSubstrByWidth(text, width);
				int wordBreak = first.lastIndexOf(' ');
				if (wordBreak > first.length() / 2) {
					first = first.substring(0, wordBreak);
				}
				String rest = text.substring(first.length()).strip();
				if (font.width(rest) <= width) {
					lines = List.of(first, rest);
				} else {
					lines = List.of(first, font.plainSubstrByWidth(rest, width - font.width("...")) + "...");
				}
			}
			this.cachedWidth = width;
			this.cachedLines = lines;
			return lines;
		}

		@Override
		public Component getNarration() {
			return this.option.optionName();
		}

		//? if >= 26.1.2 {
		/*@Override
		public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float delta) {
			graphics.text(ConfigSearchScreen.this.font, this.option.optionName(), this.getContentX() + 2, this.getContentY() + 1, WHITE);
			List<String> lines = this.breadcrumbLines();
			for (int i = 0; i < lines.size(); i++) {
				graphics.text(ConfigSearchScreen.this.font, Component.literal(lines.get(i)), this.getContentX() + 2, this.getContentY() + 12 + i * 10, GRAY);
			}
		}
		*///?} else if >= 1.21.11 {
		/*@Override
		public void renderContent(GuiGraphics graphics, int mouseX, int mouseY, boolean hovered, float delta) {
			graphics.drawString(ConfigSearchScreen.this.font, this.option.optionName(), this.getContentX() + 2, this.getContentY() + 1, WHITE);
			List<String> lines = this.breadcrumbLines();
			for (int i = 0; i < lines.size(); i++) {
				graphics.drawString(ConfigSearchScreen.this.font, lines.get(i), this.getContentX() + 2, this.getContentY() + 12 + i * 10, GRAY);
			}
		}
		*///?} else {
		@Override
		public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
			graphics.drawString(ConfigSearchScreen.this.font, this.option.optionName(), left + 2, top + 3, WHITE);
			List<String> lines = this.breadcrumbLines();
			for (int i = 0; i < lines.size(); i++) {
				graphics.drawString(ConfigSearchScreen.this.font, lines.get(i), left + 2, top + 14 + i * 10, GRAY);
			}
		}
		//?}

		//? if >= 1.21.11 {
		/*@Override
		public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
			if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
				this.activate();
				return true;
			}
			return super.mouseClicked(event, doubleClick);
		}
		*///?} else {
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
				this.activate();
				return true;
			}
			return super.mouseClicked(mouseX, mouseY, button);
		}
		//?}

		private void activate() {
			ConfigSearchScreen.this.resultsList.setSelected(this);
			ConfigSearchScreen.this.openEntry(this.option);
		}
	}
}
