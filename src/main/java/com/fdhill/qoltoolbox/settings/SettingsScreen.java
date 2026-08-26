package com.fdhill.qoltoolbox.settings;

import com.fdhill.qoltoolbox.config.QolConfig;
import com.fdhill.qoltoolbox.fullbright.Fullbright;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

// ponytail: flat layout, no scroll container — 5 features + veinminer whitelist fit on one screen.
public class SettingsScreen extends Screen {
	private static final int PANEL_W = 300;
	private static final int ROW_H = 20;
	private static final int TOGGLE_W = 60;
	private static final int VISIBLE_WL = 6;

	private int panelX;
	private int panelY;
	private int panelH;

	private final List<ToggleRow> toggles = new ArrayList<>();
	private TextFieldWidget maxBlocksField;
	private TextFieldWidget addBlockField;
	private int whitelistScroll;

	private record ToggleRow(int offsetY, String label, java.util.function.BooleanSupplier getter,
			java.util.function.Consumer<Boolean> setter) {
	}

	public SettingsScreen() {
		super(Text.translatable("screen.qoltoolbox.settings"));
	}

	@Override
	protected void init() {
		QolConfig cfg = QolConfig.get();
		toggles.clear();

		// ponytail: calculate panel dimensions FIRST, then build toggles with correct offsets
		int vmSectionH = 24 + ROW_H + 6 + VISIBLE_WL * ROW_H + 4 + 18;
		panelH = 5 * ROW_H + 12 + vmSectionH + 10;
		panelX = (width - PANEL_W) / 2;
		panelY = (height - panelH) / 2;

		// ponytail: store y offset relative to panel top, add panelY during render
		int toggleY = 6; // title takes ~6px
		for (int i = 0; i < 5; i++) {
			int oy = toggleY;
			String label = switch (i) {
				case 0 -> "Fullbright";
				case 1 -> "Trajectory";
				case 2 -> "Death Marker";
				case 3 -> "Recipe Viewer";
				case 4 -> "Vein Miner";
				default -> "";
			};
			toggles.add(new ToggleRow(oy, label, null, null));
			toggleY += ROW_H;
		}
		// ponytail: set getters/setters separately to avoid capturing 'this' before record init
		toggles.set(0, new ToggleRow(toggles.get(0).offsetY, "Fullbright",
				() -> cfg.fullbright.enabled, v -> { cfg.fullbright.enabled = v; Fullbright.set(v); }));
		toggles.set(1, new ToggleRow(toggles.get(1).offsetY, "Trajectory",
				() -> cfg.trajectory.enabled, v -> cfg.trajectory.enabled = v));
		toggles.set(2, new ToggleRow(toggles.get(2).offsetY, "Death Marker",
				() -> cfg.deathmarker.enabled, v -> cfg.deathmarker.enabled = v));
		toggles.set(3, new ToggleRow(toggles.get(3).offsetY, "Recipe Viewer",
				() -> cfg.recipeviewer.enabled, v -> cfg.recipeviewer.enabled = v));
		toggles.set(4, new ToggleRow(toggles.get(4).offsetY, "Vein Miner",
				() -> cfg.veinminer.enabled, v -> cfg.veinminer.enabled = v));

		// Max blocks field — positioned inside panel
		int vmY = panelY + 5 * ROW_H + 12 + 24;
		maxBlocksField = new TextFieldWidget(textRenderer, panelX + 80, vmY, 50, 14,
				Text.literal("Max Blocks"));
		maxBlocksField.setText(String.valueOf(cfg.veinminer.maxBlocks));
		maxBlocksField.setChangedListener(s -> {
			try {
				cfg.veinminer.maxBlocks = Math.max(1, Math.min(128, Integer.parseInt(s.trim())));
			} catch (NumberFormatException ignored) {
			}
		});
		addDrawableChild(maxBlocksField);

		// Add block field — positioned inside panel
		int addY = vmY + ROW_H + 6 + VISIBLE_WL * ROW_H + 2;
		addBlockField = new TextFieldWidget(textRenderer, panelX + 6, addY, PANEL_W - 76, 14,
				Text.literal("Add block"));
		addBlockField.setPlaceholder(Text.literal("minecraft:stone"));
		addDrawableChild(addBlockField);
		setInitialFocus(addBlockField);
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		renderBackground(ctx, mouseX, mouseY, delta);

		// Panel background
		ctx.fill(panelX, panelY, panelX + PANEL_W, panelY + panelH, 0xF0111111);

		// Title
		ctx.drawText(textRenderer, title, panelX + 6, panelY + 4, 0xFFFFFF55, true);

		// Toggle rows
		QolConfig cfg = QolConfig.get();
		for (ToggleRow row : toggles) {
			boolean on = row.getter.getAsBoolean();
			int absY = panelY + row.offsetY;
			// Label
			ctx.drawText(textRenderer, row.label, panelX + 6, absY + 5, 0xFFFFFFFF, true);
			// Toggle button
			int bx = panelX + PANEL_W - TOGGLE_W - 6;
			int by = absY + 2;
			int color = on ? 0xFF55FF55 : 0xFFFF5555;
			ctx.fill(bx, by, bx + TOGGLE_W, by + 14, 0xFF333333);
			String txt = on ? "ON" : "OFF";
			int tw = textRenderer.getWidth(txt);
			ctx.drawText(textRenderer, txt, bx + (TOGGLE_W - tw) / 2, by + 3, color, true);
		}

		// Vein Miner section
		int vmY = panelY + 5 * ROW_H + 12;
		ctx.drawText(textRenderer, "--- Vein Miner ---", panelX + 6, vmY + 5, 0xFFFFFF55, true);
		vmY += 20;

		// Max blocks
		ctx.drawText(textRenderer, "Max Blocks:", panelX + 6, vmY + 3, 0xFFFFFFFF, true);
		vmY += ROW_H + 6;

		// Whitelist
		ctx.drawText(textRenderer, "Whitelist:", panelX + 6, vmY, 0xFFAAAAAA, true);
		vmY += ROW_H - 4;

		// Whitelist items
		List<String> wl = cfg.veinminer.whitelist;
		int maxScroll = Math.max(0, wl.size() - VISIBLE_WL);
		whitelistScroll = Math.max(0, Math.min(maxScroll, whitelistScroll));

		for (int i = 0; i < VISIBLE_WL && whitelistScroll + i < wl.size(); i++) {
			int itemY = vmY + i * ROW_H;
			String entry = wl.get(whitelistScroll + i);
			String display = textRenderer.trimToWidth(entry, PANEL_W - 40);
			ctx.drawText(textRenderer, display, panelX + 6, itemY + 3, 0xFFFFFFFF, true);
			// Remove button [×]
			int rmx = panelX + PANEL_W - 20;
			boolean hoverX = mouseX >= rmx && mouseX <= rmx + 14 && mouseY >= itemY && mouseY <= itemY + 14;
			ctx.fill(rmx, itemY, rmx + 14, itemY + 14, hoverX ? 0xFFFF5555 : 0xFF555555);
			ctx.drawText(textRenderer, "x", rmx + 4, itemY + 3, 0xFFFFFFFF, true);
		}
		if (wl.isEmpty()) {
			ctx.drawText(textRenderer, "(empty)", panelX + 6, vmY + 3, 0xFF888888, true);
		}

		// Scroll hint
		if (wl.size() > VISIBLE_WL) {
			ctx.drawText(textRenderer, whitelistScroll + "/" + maxScroll,
					panelX + PANEL_W - 80, vmY - 2, 0xFF888888, true);
		}

		// Add block button
		int addY = vmY + VISIBLE_WL * ROW_H + 2;
		int addBtnX = panelX + PANEL_W - 60;
		boolean hoverAdd = mouseX >= addBtnX && mouseX <= addBtnX + 54 && mouseY >= addY && mouseY <= addY + 16;
		ctx.fill(addBtnX, addY, addBtnX + 54, addY + 16, hoverAdd ? 0xFF55AA55 : 0xFF336633);
		ctx.drawText(textRenderer, "Add", addBtnX + 18, addY + 4, 0xFFFFFFFF, true);

		super.render(ctx, mouseX, mouseY, delta);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

		QolConfig cfg = QolConfig.get();

		// Toggle clicks
		for (ToggleRow row : toggles) {
			int bx = panelX + PANEL_W - TOGGLE_W - 6;
			int absY = panelY + row.offsetY + 2;
			if (mouseX >= bx && mouseX <= bx + TOGGLE_W && mouseY >= absY && mouseY <= absY + 14) {
				row.setter.accept(!row.getter.getAsBoolean());
				QolConfig.save();
				return true;
			}
		}

		// Whitelist remove clicks
		int vmY = panelY + 5 * ROW_H + 12 + 20 + ROW_H - 4;
		for (int i = 0; i < VISIBLE_WL && whitelistScroll + i < cfg.veinminer.whitelist.size(); i++) {
			int itemY = vmY + i * ROW_H;
			int rmx = panelX + PANEL_W - 20;
			if (mouseX >= rmx && mouseX <= rmx + 14 && mouseY >= itemY && mouseY <= itemY + 14) {
				cfg.veinminer.whitelist.remove(whitelistScroll + i);
				QolConfig.save();
				return true;
			}
		}

		// Add block button
		int addY = vmY + VISIBLE_WL * ROW_H + 2;
		int addBtnX = panelX + PANEL_W - 60;
		if (mouseX >= addBtnX && mouseX <= addBtnX + 54 && mouseY >= addY && mouseY <= addY + 16) {
			addBlock();
			return true;
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
		whitelistScroll -= (int) Math.signum(vertical);
		return true;
	}

	private void addBlock() {
		String text = addBlockField.getText().trim();
		if (text.isEmpty()) return;
		QolConfig cfg = QolConfig.get();
		if (!cfg.veinminer.whitelist.contains(text)) {
			cfg.veinminer.whitelist.add(text);
			QolConfig.save();
		}
		addBlockField.setText("");
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
