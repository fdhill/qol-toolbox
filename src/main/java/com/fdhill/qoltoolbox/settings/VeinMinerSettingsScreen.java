package com.fdhill.qoltoolbox.settings;

import com.fdhill.qoltoolbox.config.QolConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.List;

public class VeinMinerSettingsScreen extends Screen {
	private static final int PANEL_W = 260;
	private static final int ROW_H = 16;
	private static final int TOGGLE_W = 40;
	private static final int PAD = 8;
	private static final int VISIBLE_WL = 8;

	private final Screen parent;
	private int panelX;
	private int panelY;
	private int panelH;

	private TextFieldWidget maxBlocksField;
	private TextFieldWidget addBlockField;
	private int whitelistScroll;

	public VeinMinerSettingsScreen(Screen parent) {
		super(Text.literal("Vein Miner Settings"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		QolConfig.VeinMiner cfg = QolConfig.get().veinminer;

		// Calculate panel height
		int whitelistH = VISIBLE_WL * (ROW_H - 2);
		int bottomRowH = ROW_H + 4 + 12 + 4; // add field row + add button
		panelH = PAD + 14 + 4 + ROW_H + 4 + 14 + whitelistH + 4 + bottomRowH + PAD;
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
		int addFieldY = panelY + panelH - PAD - ROW_H - 4;
		addBlockField = new TextFieldWidget(textRenderer, panelX + PAD, addFieldY,
				PANEL_W - PAD * 2 - TOGGLE_W - 4, 12, Text.literal("Add block"));
		addBlockField.setPlaceholder(Text.literal("minecraft:stone"));
		addDrawableChild(addBlockField);
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		renderBackground(ctx, mouseX, mouseY, delta);

		QolConfig.VeinMiner cfg = QolConfig.get().veinminer;

		// Panel background
		ctx.fill(panelX, panelY, panelX + PANEL_W, panelY + panelH, 0xE0101010);

		// Title
		ctx.drawText(textRenderer, title, panelX + PAD, panelY + PAD, 0xFFFFFF55, true);

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

		// Whitelist items
		for (int i = 0; i < VISIBLE_WL && whitelistScroll + i < wl.size(); i++) {
			int itemY = wlY + i * (ROW_H - 2);
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
			ctx.drawText(textRenderer, "(empty)", panelX + PAD + 2, wlY + 1, 0xFF666666, false);
		}

		// Add block field + button
		int addFieldY = panelY + panelH - PAD - ROW_H - 4;
		int addBtnX = panelX + PANEL_W - PAD - TOGGLE_W;
		boolean hoverAdd = mouseX >= addBtnX && mouseX <= addBtnX + TOGGLE_W &&
				mouseY >= addFieldY && mouseY <= addFieldY + 12;
		ctx.fill(addBtnX, addFieldY, addBtnX + TOGGLE_W, addFieldY + 12, hoverAdd ? 0xFF55AA55 : 0xFF336633);
		ctx.drawText(textRenderer, "Add", addBtnX + 12, addFieldY + 2, 0xFFFFFFFF, true);

		// Back button
		int backW = 60;
		int backX = panelX + (PANEL_W - backW) / 2;
		int backY = panelY + panelH - PAD - 12;
		boolean hoverBack = mouseX >= backX && mouseX <= backX + backW &&
				mouseY >= backY && mouseY <= backY + 12;
		ctx.fill(backX, backY, backX + backW, backY + 12, hoverBack ? 0xFF555588 : 0xFF444466);
		int btw = textRenderer.getWidth("Back");
		ctx.drawText(textRenderer, "Back", backX + (backW - btw) / 2, backY + 2, 0xFFFFFFFF, true);

		super.render(ctx, mouseX, mouseY, delta);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

		QolConfig.VeinMiner cfg = QolConfig.get().veinminer;

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
		int addFieldY = panelY + panelH - PAD - ROW_H - 4;
		int addBtnX = panelX + PANEL_W - PAD - TOGGLE_W;
		if (mouseX >= addBtnX && mouseX <= addBtnX + TOGGLE_W &&
				mouseY >= addFieldY && mouseY <= addFieldY + 12) {
			addBlock();
			return true;
		}

		// Back button
		int backW = 60;
		int backX = panelX + (PANEL_W - backW) / 2;
		int backY = panelY + panelH - PAD - 12;
		if (mouseX >= backX && mouseX <= backX + backW &&
				mouseY >= backY && mouseY <= backY + 12) {
			client.setScreen(parent);
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
