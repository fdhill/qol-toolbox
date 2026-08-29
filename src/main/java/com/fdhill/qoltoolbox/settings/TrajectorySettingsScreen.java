package com.fdhill.qoltoolbox.settings;

import com.fdhill.qoltoolbox.config.QolConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class TrajectorySettingsScreen extends Screen {
	private static final int PANEL_W = 220;
	private static final int ROW_H = 16;
	private static final int TOGGLE_W = 40;
	private static final int PAD = 8;

	private final Screen parent;
	private int panelX;
	private int panelY;
	private int panelH;

	public TrajectorySettingsScreen(Screen parent) {
		super(Text.literal("Trajectory Settings"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		panelH = PAD + 14 + 4 + ROW_H + 4 + ROW_H;
		panelX = (width - PANEL_W) / 2;
		panelY = (height - panelH) / 2;
	}

	@Override
	public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		QolConfig.Trajectory cfg = QolConfig.get().trajectory;

		ctx.fill(panelX, panelY, panelX + PANEL_W, panelY + panelH, 0xE0202020);

		// Back arrow
		boolean hoverBack = mouseX >= panelX + 2 && mouseX <= panelX + 14 &&
				mouseY >= panelY + 2 && mouseY <= panelY + 14;
		ctx.drawText(textRenderer, "<", panelX + 4, panelY + PAD, hoverBack ? 0xFFFFFF55 : 0xFFAAAAAA, true);

		// Title
		ctx.drawText(textRenderer, title, panelX + 18, panelY + PAD, 0xFFFFFF55, true);

		int sepY = panelY + PAD + 14;
		ctx.fill(panelX + PAD, sepY, panelX + PANEL_W - PAD, sepY + 1, 0xFF555555);

		// Enable toggle
		int toggleY = sepY + 8;
		ctx.drawText(textRenderer, "Enabled:", panelX + PAD, toggleY + 2, 0xFFFFFFFF, true);
		int btnX = panelX + PANEL_W - PAD - TOGGLE_W;
		boolean on = cfg.enabled;
		boolean hoverT = mouseX >= btnX && mouseX <= btnX + TOGGLE_W &&
				mouseY >= toggleY && mouseY <= toggleY + 12;
		ctx.fill(btnX, toggleY, btnX + TOGGLE_W, toggleY + 12, on ? 0xFF2A6B2A : hoverT ? 0xFF6B3A3A : 0xFF4A2A2A);
		String txt = on ? "ON" : "OFF";
		int tw = textRenderer.getWidth(txt);
		ctx.drawText(textRenderer, txt, btnX + (TOGGLE_W - tw) / 2, toggleY + 2, 0xFFFFFFFF, true);

		super.render(ctx, mouseX, mouseY, delta);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

		// Back arrow
		if (mouseX >= panelX + 2 && mouseX <= panelX + 14 &&
				mouseY >= panelY + 2 && mouseY <= panelY + 14) {
			client.setScreen(parent);
			return true;
		}

		QolConfig.Trajectory cfg = QolConfig.get().trajectory;
		int toggleY = panelY + PAD + 14 + 8;
		int btnX = panelX + PANEL_W - PAD - TOGGLE_W;

		// Toggle
		if (mouseX >= btnX && mouseX <= btnX + TOGGLE_W &&
				mouseY >= toggleY && mouseY <= toggleY + 12) {
			cfg.enabled = !cfg.enabled;
			QolConfig.save();
			return true;
		}

		return super.mouseClicked(mouseX, mouseY, button);
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
