package com.fdhill.qoltoolbox.settings;

import com.fdhill.qoltoolbox.config.QolConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class VeinMinerSettingsScreen extends Screen {
	private static final int PANEL_W = 260;
	private static final int ROW_H = 16;
	private static final int TOGGLE_W = 40;
	private static final int PAD = 8;
	private static final int VISIBLE_WL = 8;
	private static final int VISIBLE_SUGGESTIONS = 6;

	private final Screen parent;
	private int panelX;
	private int panelY;
	private int panelH;

	private TextFieldWidget maxBlocksField;
	private TextFieldWidget addBlockField;
	private int whitelistScroll;

	private List<String> allBlockIds = List.of();
	private List<String> suggestions = List.of();
	private int selectedSuggestion = -1;
	private int suggestionScroll = 0;

	public VeinMinerSettingsScreen(Screen parent) {
		super(Text.literal("Vein Miner Settings"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		QolConfig.VeinMiner cfg = QolConfig.get().veinminer;

		// Load all block IDs from registry
		allBlockIds = Registries.BLOCK.getIds().stream()
				.map(Identifier::toString)
				.sorted()
				.toList();

		// Calculate panel height
		int whitelistH = VISIBLE_WL * (ROW_H - 2);
		int bottomRowH = ROW_H + 4 + 12; // add field + gap + add button
		panelH = PAD + 14 + 4 + ROW_H + 4 + 14 + whitelistH + 4 + bottomRowH + 4;
		panelX = (width - PANEL_W) / 2;
		panelY = (height - panelH) / 2;

		// Max blocks field
		int mbY = panelY + PAD + 14 + 4;
		maxBlocksField = new TextFieldWidget(textRenderer, panelX + 90, mbY, 40, 12,
				Text.literal("Max Blocks"));
		maxBlocksField.setText(String.valueOf(cfg.maxBlocks));
		maxBlocksField.setChangedListener(s -> {
			try {
				cfg.maxBlocks = Math.max(1, Math.min(128, Integer.parseInt(s.trim())));
			} catch (NumberFormatException ignored) {
			}
		});
		addDrawableChild(maxBlocksField);

		// Add block field
		int addFieldY = panelY + panelH - PAD - ROW_H - 4 - 12;
		addBlockField = new TextFieldWidget(textRenderer, panelX + PAD, addFieldY,
				PANEL_W - PAD * 2 - TOGGLE_W - 4, 12, Text.literal("Add block"));
		addBlockField.setPlaceholder(Text.literal("minecraft:stone"));
		addBlockField.setChangedListener(this::updateSuggestions);
		addDrawableChild(addBlockField);
	}

	private void updateSuggestions(String input) {
		String query = input.trim().toLowerCase();
		if (query.isEmpty()) {
			suggestions = List.of();
			selectedSuggestion = -1;
			suggestionScroll = 0;
			return;
		}
		suggestions = allBlockIds.stream()
				.filter(id -> id.contains(query))
				.limit(50)
				.toList();
		selectedSuggestion = suggestions.isEmpty() ? -1 : 0;
		suggestionScroll = 0;
	}

	@Override
	public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		QolConfig.VeinMiner cfg = QolConfig.get().veinminer;

		// Panel background
		ctx.fill(panelX, panelY, panelX + PANEL_W, panelY + panelH, 0xE0202020);

		// Back arrow
		boolean hoverBack = mouseX >= panelX + 2 && mouseX <= panelX + 14 &&
				mouseY >= panelY + 2 && mouseY <= panelY + 14;
		ctx.drawText(textRenderer, "<", panelX + 4, panelY + PAD, hoverBack ? 0xFFFFFF55 : 0xFFAAAAAA, true);

		// Title
		ctx.drawText(textRenderer, title, panelX + 18, panelY + PAD, 0xFFFFFF55, true);

		// Separator
		int sepY = panelY + PAD + 14;
		ctx.fill(panelX + PAD, sepY, panelX + PANEL_W - PAD, sepY + 1, 0xFF555555);

		// Max blocks
		int mbY = panelY + PAD + 14 + 4;
		ctx.drawText(textRenderer, "Max Blocks:", panelX + PAD, mbY + 2, 0xFFFFFFFF, true);

		// Whitelist section
		int wlY = mbY + ROW_H + 4;
		ctx.drawText(textRenderer, "Whitelist:", panelX + PAD, wlY, 0xFFAAAAAA, true);

		List<String> wl = cfg.whitelist;
		int maxScroll = Math.max(0, wl.size() - VISIBLE_WL);
		whitelistScroll = Math.max(0, Math.min(maxScroll, whitelistScroll));
		if (wl.size() > VISIBLE_WL) {
			String scrollTxt = (whitelistScroll + 1) + "-" +
					Math.min(whitelistScroll + VISIBLE_WL, wl.size()) + "/" + wl.size();
			int sw = textRenderer.getWidth(scrollTxt);
			ctx.drawText(textRenderer, scrollTxt, panelX + PANEL_W - PAD - sw, wlY, 0xFF888888, true);
		}
		wlY += 12;

		// Whitelist background box
		int wlBoxTop = wlY;
		int wlBoxBottom = wlY + VISIBLE_WL * (ROW_H - 2);
		ctx.fill(panelX + PAD, wlBoxTop - 2, panelX + PANEL_W - PAD, wlBoxBottom + 2, 0xFF1A1A2A);

		// Whitelist items
		for (int i = 0; i < VISIBLE_WL && whitelistScroll + i < wl.size(); i++) {
			int itemY = wlY + i * (ROW_H - 2);
			String entry = wl.get(whitelistScroll + i);
			String display = textRenderer.trimToWidth(entry, PANEL_W - PAD * 2 - 18);
			boolean hoverItem = mouseX >= panelX + PAD && mouseX <= panelX + PANEL_W - PAD - 16 &&
					mouseY >= itemY && mouseY <= itemY + 12;
			if (hoverItem) {
				ctx.fill(panelX + PAD, itemY - 1, panelX + PANEL_W - PAD - 16, itemY + 13, 0xFF2A2A4A);
			}
			ctx.drawText(textRenderer, display, panelX + PAD + 2, itemY + 1, 0xFFE0E0E0, false);
			// Remove button
			int rmx = panelX + PANEL_W - PAD - 14;
			boolean hoverX = mouseX >= rmx && mouseX <= rmx + 12 && mouseY >= itemY && mouseY <= itemY + 12;
			ctx.fill(rmx, itemY, rmx + 12, itemY + 12, hoverX ? 0xFFFF5555 : 0xFF553333);
			ctx.drawText(textRenderer, "x", rmx + 4, itemY + 2, 0xFFFFFFFF, true);
		}
		if (wl.isEmpty()) {
			ctx.drawText(textRenderer, "(empty — add blocks below)", panelX + PAD + 4, wlY + 20, 0xFF777777, false);
		}

		// Add block field + button
		int addFieldY = panelY + panelH - PAD - ROW_H - 4 - 12;
		int addBtnX = panelX + PANEL_W - PAD - TOGGLE_W;
		boolean hoverAdd = mouseX >= addBtnX && mouseX <= addBtnX + TOGGLE_W &&
				mouseY >= addFieldY && mouseY <= addFieldY + 12;
		ctx.fill(addBtnX, addFieldY, addBtnX + TOGGLE_W, addFieldY + 12, hoverAdd ? 0xFF55AA55 : 0xFF336633);
		ctx.drawText(textRenderer, "Add", addBtnX + 12, addFieldY + 2, 0xFFFFFFFF, true);

		// Suggestion dropdown
		if (!suggestions.isEmpty() && addBlockField.isFocused()) {
			int dropY = addFieldY + 14;
			int dropW = PANEL_W - PAD * 2;
			int visibleCount = Math.min(suggestions.size(), VISIBLE_SUGGESTIONS);
			int dropH = visibleCount * (ROW_H - 2) + 4;

			// Clamp scroll
			int sugMaxScroll = Math.max(0, suggestions.size() - VISIBLE_SUGGESTIONS);
			suggestionScroll = Math.max(0, Math.min(sugMaxScroll, suggestionScroll));

			// Dropdown background
			ctx.fill(panelX + PAD, dropY, panelX + PAD + dropW, dropY + dropH, 0xFF1A1A2A);
			ctx.fill(panelX + PAD, dropY, panelX + PAD + dropW, dropY + 1, 0xFF555555);

			// Suggestion items
			for (int i = 0; i < visibleCount; i++) {
				int idx = suggestionScroll + i;
				int itemY = dropY + 2 + i * (ROW_H - 2);
				String id = suggestions.get(idx);
				boolean selected = idx == selectedSuggestion;
				boolean hoverItem = mouseX >= panelX + PAD && mouseX <= panelX + PAD + dropW &&
						mouseY >= itemY && mouseY <= itemY + 12;

				if (selected || hoverItem) {
					ctx.fill(panelX + PAD, itemY - 1, panelX + PAD + dropW, itemY + 13, 0xFF2A2A4A);
				}

				String display = textRenderer.trimToWidth(id, dropW - 8);
				ctx.drawText(textRenderer, display, panelX + PAD + 4, itemY + 1, 0xFFE0E0E0, false);
			}

			// Scroll indicator
			if (suggestions.size() > VISIBLE_SUGGESTIONS) {
				String scrollTxt = (suggestionScroll + 1) + "-" +
						Math.min(suggestionScroll + VISIBLE_SUGGESTIONS, suggestions.size()) +
						"/" + suggestions.size();
				int sw = textRenderer.getWidth(scrollTxt);
				ctx.drawText(textRenderer, scrollTxt, panelX + PAD + dropW - sw - 4, dropY + 2, 0xFF888888, true);
			}
		}

		super.render(ctx, mouseX, mouseY, delta);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

		QolConfig.VeinMiner cfg = QolConfig.get().veinminer;
		int addFieldY = panelY + panelH - PAD - ROW_H - 4 - 12;

		// Back arrow
		if (mouseX >= panelX + 2 && mouseX <= panelX + 14 &&
				mouseY >= panelY + 2 && mouseY <= panelY + 14) {
			client.setScreen(parent);
			return true;
		}

		// Suggestion dropdown click
		if (!suggestions.isEmpty() && addBlockField.isFocused()) {
			int dropY = addFieldY + 14;
			int dropW = PANEL_W - PAD * 2;
			int visibleCount = Math.min(suggestions.size(), VISIBLE_SUGGESTIONS);

			for (int i = 0; i < visibleCount; i++) {
				int idx = suggestionScroll + i;
				int itemY = dropY + 2 + i * (ROW_H - 2);
				if (mouseX >= panelX + PAD && mouseX <= panelX + PAD + dropW &&
						mouseY >= itemY && mouseY <= itemY + 12) {
					selectSuggestion(idx);
					return true;
				}
			}
		}

		// Whitelist remove clicks
		int wlY = panelY + PAD + 14 + 4 + ROW_H + 4 + 12;
		for (int i = 0; i < VISIBLE_WL && whitelistScroll + i < cfg.whitelist.size(); i++) {
			int itemY = wlY + i * (ROW_H - 2);
			int rmx = panelX + PANEL_W - PAD - 14;
			if (mouseX >= rmx && mouseX <= rmx + 12 && mouseY >= itemY && mouseY <= itemY + 12) {
				cfg.whitelist.remove(whitelistScroll + i);
				QolConfig.save();
				return true;
			}
		}

		// Add block button
		int addBtnX = panelX + PANEL_W - PAD - TOGGLE_W;
		if (mouseX >= addBtnX && mouseX <= addBtnX + TOGGLE_W &&
				mouseY >= addFieldY && mouseY <= addFieldY + 12) {
			addBlock();
			return true;
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (!suggestions.isEmpty() && addBlockField.isFocused()) {
			if (keyCode == 265) { // Arrow Up
				if (selectedSuggestion > 0) {
					selectedSuggestion--;
					if (selectedSuggestion < suggestionScroll) suggestionScroll = selectedSuggestion;
				}
				return true;
			}
			if (keyCode == 264) { // Arrow Down
				if (selectedSuggestion < suggestions.size() - 1) {
					selectedSuggestion++;
					if (selectedSuggestion >= suggestionScroll + VISIBLE_SUGGESTIONS) {
						suggestionScroll = selectedSuggestion - VISIBLE_SUGGESTIONS + 1;
					}
				}
				return true;
			}
			if (keyCode == 257 || keyCode == 335) { // Enter or Numpad Enter
				if (selectedSuggestion >= 0 && selectedSuggestion < suggestions.size()) {
					selectSuggestion(selectedSuggestion);
					return true;
				}
			}
			if (keyCode == 258) { // Tab
				if (selectedSuggestion >= 0 && selectedSuggestion < suggestions.size()) {
					selectSuggestion(selectedSuggestion);
					return true;
				}
			}
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	private void selectSuggestion(int idx) {
		String selected = suggestions.get(idx);
		addBlockField.setText(selected);
		suggestions = List.of();
		selectedSuggestion = -1;
		suggestionScroll = 0;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
		if (!suggestions.isEmpty() && addBlockField.isFocused()) {
			int maxScroll = Math.max(0, suggestions.size() - VISIBLE_SUGGESTIONS);
			suggestionScroll = Math.max(0, Math.min(maxScroll, suggestionScroll - (int) Math.signum(vertical)));
			return true;
		}
		whitelistScroll -= (int) Math.signum(vertical);
		return true;
	}

	private void addBlock() {
		String text = addBlockField.getText().trim();
		if (text.isEmpty()) return;
		QolConfig.VeinMiner cfg = QolConfig.get().veinminer;
		if (!cfg.whitelist.contains(text)) {
			cfg.whitelist.add(text);
			QolConfig.save();
		}
		addBlockField.setText("");
	}

	@Override
	public void close() {
		client.setScreen(parent);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
