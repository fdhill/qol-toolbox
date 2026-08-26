package com.fdhill.qoltoolbox.settings;

import com.fdhill.qoltoolbox.config.QolConfig;
import com.fdhill.qoltoolbox.fullbright.Fullbright;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

// ponytail: compact flat layout — 5 toggles + veinminer whitelist on one screen.
public class SettingsScreen extends Screen {
	private static final int PANEL_W = 260;
	private static final int ROW_H = 16;
	private static final int TOGGLE_W = 50;
	private static final int VISIBLE_WL = 4;
	private static final int PAD = 8;

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

		// ponytail: pre-calculate toggle offsets
		String[] names = {"Fullbright", "Trajectory", "Death Marker", "Recipe Viewer", "Vein Miner"};
		for (int i = 0; i < 5; i++) {
			int oy = PAD + 14 + i * ROW_H; // title height = 14
			toggles.add(new ToggleRow(oy, names[i], null, null));
		}
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

		// ponytail: panel height = title + toggles + separator + vm section + padding
		int vmSectionH = 14 + ROW_H + 4 + 10 + VISIBLE_WL * ROW_H + 4 + ROW_H;
		panelH = PAD + 14 + 5 * ROW_H + 4 + vmSectionH + PAD;
		panelX = (width - PANEL_W) / 2;
		panelY = (height - panelH) / 2;

		// Max blocks field
		int vmBaseY = panelY + PAD + 14 + 5 * ROW_H + 4 + 14;
		maxBlocksField = new TextFieldWidget(textRenderer, panelX + 70, vmBaseY, 40, 12,
				Text.literal("Max Blocks"));
		maxBlocksField.setText(String.valueOf(cfg.veinminer.maxBlocks));
		maxBlocksField.setChangedListener(s -> {
			try {
				cfg.veinminer.maxBlocks = Math.max(1, Math.min(128, Integer.parseInt(s.trim())));
			} catch (NumberFormatException ignored) {
			}
		});
		addDrawableChild(maxBlocksField);

		// Add block field
		int addFieldY = panelY + panelH - PAD - ROW_H - 2;
		addBlockField = new TextFieldWidget(textRenderer, panelX + PAD, addFieldY,
				PANEL_W - PAD - TOGGLE_W - 4, 12, Text.literal("Add block"));
		addBlockField.setPlaceholder(Text.literal("minecraft:stone"));
		addDrawableChild(addBlockField);
		setInitialFocus(addBlockField);
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		renderBackground(ctx, mouseX, mouseY, delta);

		// Panel background
		ctx.fill(panelX, panelY, panelX + PANEL_W, panelY + panelH, 0xE0101010);

		// Title
		ctx.drawText(textRenderer, title, panelX + PAD, panelY + PAD, 0xFFFFFF55, true);

		// Separator line
		int sepY1 = panelY + PAD + 14;
		ctx.fill(panelX + PAD, sepY1, panelX + PANEL_W - PAD, sepY1 + 1, 0xFF555555);

		// Toggle rows
		QolConfig cfg = QolConfig.get();
		for (ToggleRow row : toggles) {
			boolean on = row.getter.getAsBoolean();
			int absY = panelY + row.offsetY;
			ctx.drawText(textRenderer, row.label, panelX + PAD, absY + 3, 0xFFFFFFFF, true);
			int bx = panelX + PANEL_W - TOGGLE_W - PAD;
			int by = absY + 1;
			ctx.fill(bx, by, bx + TOGGLE_W, by + 12, on ? 0xFF2A6B2A : 0xFF6B2A2A);
			String txt = on ? "ON" : "OFF";
			int tw = textRenderer.getWidth(txt);
			ctx.drawText(textRenderer, txt, bx + (TOGGLE_W - tw) / 2, by + 2, 0xFFFFFFFF, true);
		}

		// Separator line before Vein Miner
		int sepY2 = panelY + PAD + 14 + 5 * ROW_H + 2;
		ctx.fill(panelX + PAD, sepY2, panelX + PANEL_W - PAD, sepY2 + 1, 0xFF555555);

		// Vein Miner section
		int vmY = sepY2 + 4;
		ctx.drawText(textRenderer, "Vein Miner", panelX + PAD, vmY, 0xFFFFFF55, true);
		vmY += 14;

		// Max blocks
		ctx.drawText(textRenderer, "Max Blocks:", panelX + PAD, vmY + 2, 0xFFFFFFFF, true);
		vmY += ROW_H + 4;

		// Whitelist label + scroll hint
		ctx.drawText(textRenderer, "Whitelist:", panelX + PAD, vmY, 0xFFAAAAAA, true);
		List<String> wl = cfg.veinminer.whitelist;
		int maxScroll = Math.max(0, wl.size() - VISIBLE_WL);
		whitelistScroll = Math.max(0, Math.min(maxScroll, whitelistScroll));
		if (wl.size() > VISIBLE_WL) {
			String scrollTxt = (whitelistScroll + 1) + "-" +
					Math.min(whitelistScroll + VISIBLE_WL, wl.size()) + "/" + wl.size();
			int sw = textRenderer.getWidth(scrollTxt);
			ctx.drawText(textRenderer, scrollTxt, panelX + PANEL_W - PAD - sw, vmY, 0xFF888888, true);
		}
		vmY += 10;

		// Whitelist items
		for (int i = 0; i < VISIBLE_WL && whitelistScroll + i < wl.size(); i++) {
			int itemY = vmY + i * (ROW_H - 2);
			String entry = wl.get(whitelistScroll + i);
			String display = textRenderer.trimToWidth(entry, PANEL_W - PAD * 2 - 18);
			ctx.drawText(textRenderer, display, panelX + PAD + 2, itemY + 1, 0xFFFFFFFF, false);
			// Remove button
			int rmx = panelX + PANEL_W - PAD - 14;
			boolean hoverX = mouseX >= rmx && mouseX <= rmx + 12 && mouseY >= itemY && mouseY <= itemY + 12;
			ctx.fill(rmx, itemY, rmx + 12, itemY + 12, hoverX ? 0xFFFF5555 : 0xFF444444);
			ctx.drawText(textRenderer, "x", rmx + 4, itemY + 2, 0xFFFFFFFF, true);
		}
		if (wl.isEmpty()) {
			ctx.drawText(textRenderer, "(empty)", panelX + PAD + 2, vmY + 1, 0xFF666666, false);
		}

		// Add block button
		int addBtnY = panelY + panelH - PAD - ROW_H - 2;
		int addBtnX = panelX + PANEL_W - PAD - TOGGLE_W;
		boolean hoverAdd = mouseX >= addBtnX && mouseX <= addBtnX + TOGGLE_W &&
				mouseY >= addBtnY && mouseY <= addBtnY + 12;
		ctx.fill(addBtnX, addBtnY, addBtnX + TOGGLE_W, addBtnY + 12, hoverAdd ? 0xFF55AA55 : 0xFF336633);
		ctx.drawText(textRenderer, "Add", addBtnX + 16, addBtnY + 2, 0xFFFFFFFF, true);

		super.render(ctx, mouseX, mouseY, delta);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

		QolConfig cfg = QolConfig.get();

		// Toggle clicks
		for (ToggleRow row : toggles) {
			int bx = panelX + PANEL_W - TOGGLE_W - PAD;
			int absY = panelY + row.offsetY + 1;
			if (mouseX >= bx && mouseX <= bx + TOGGLE_W && mouseY >= absY && mouseY <= absY + 12) {
				row.setter.accept(!row.getter.getAsBoolean());
				QolConfig.save();
				return true;
			}
		}

		// Whitelist remove clicks
		int vmY = panelY + PAD + 14 + 5 * ROW_H + 4 + 14 + ROW_H + 4 + 10;
		for (int i = 0; i < VISIBLE_WL && whitelistScroll + i < cfg.veinminer.whitelist.size(); i++) {
			int itemY = vmY + i * (ROW_H - 2);
			int rmx = panelX + PANEL_W - PAD - 14;
			if (mouseX >= rmx && mouseX <= rmx + 12 && mouseY >= itemY && mouseY <= itemY + 12) {
				cfg.veinminer.whitelist.remove(whitelistScroll + i);
				QolConfig.save();
				return true;
			}
		}

		// Add block button
		int addBtnY = panelY + panelH - PAD - ROW_H - 2;
		int addBtnX = panelX + PANEL_W - PAD - TOGGLE_W;
		if (mouseX >= addBtnX && mouseX <= addBtnX + TOGGLE_W && mouseY >= addBtnY && mouseY <= addBtnY + 12) {
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
