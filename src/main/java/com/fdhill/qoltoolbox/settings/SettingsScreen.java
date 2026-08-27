package com.fdhill.qoltoolbox.settings;

import com.fdhill.qoltoolbox.config.QolConfig;
import com.fdhill.qoltoolbox.fullbright.Fullbright;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class SettingsScreen extends Screen {
	private static final int PANEL_W = 220;
	private static final int ROW_H = 18;
	private static final int TOGGLE_W = 40;
	private static final int SETTINGS_W = 20;
	private static final int PAD = 8;

	private int panelX;
	private int panelY;

	private final List<ToggleRow> toggles = new ArrayList<>();

	private record ToggleRow(int index, String label, java.util.function.BooleanSupplier getter,
			java.util.function.Consumer<Boolean> setter) {
	}

	public SettingsScreen() {
		super(Text.translatable("screen.qoltoolbox.settings"));
	}

	@Override
	protected void init() {
		QolConfig cfg = QolConfig.get();
		toggles.clear();

		toggles.add(new ToggleRow(0, "Fullbright",
				() -> cfg.fullbright.enabled, v -> { cfg.fullbright.enabled = v; Fullbright.set(v); }));
		toggles.add(new ToggleRow(1, "Trajectory",
				() -> cfg.trajectory.enabled, v -> cfg.trajectory.enabled = v));
		toggles.add(new ToggleRow(2, "Death Marker",
				() -> cfg.deathmarker.enabled, v -> cfg.deathmarker.enabled = v));
		toggles.add(new ToggleRow(3, "Recipe Viewer",
				() -> cfg.recipeviewer.enabled, v -> cfg.recipeviewer.enabled = v));
		toggles.add(new ToggleRow(4, "Vein Miner",
				() -> cfg.veinminer.enabled, v -> cfg.veinminer.enabled = v));

		int panelH = PAD + 14 + toggles.size() * ROW_H + PAD;
		panelX = (width - PANEL_W) / 2;
		panelY = (height - panelH) / 2;
	}

	@Override
	public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
		// Skip blur + darkening — game world renders behind panel
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		// Panel background
		ctx.fill(panelX, panelY, panelX + PANEL_W, panelY + (PAD + 14 + toggles.size() * ROW_H + PAD), 0xE0202020);

		// Title
		ctx.drawText(textRenderer, title, panelX + PAD, panelY + PAD, 0xFFFFFF55, true);

		// Separator
		int sepY = panelY + PAD + 14;
		ctx.fill(panelX + PAD, sepY, panelX + PANEL_W - PAD, sepY + 1, 0xFF555555);

		// Toggle rows
		for (ToggleRow row : toggles) {
			boolean on = row.getter.getAsBoolean();
			int absY = panelY + PAD + 14 + row.index * ROW_H;

			// Label
			ctx.drawText(textRenderer, row.label, panelX + PAD, absY + 4, 0xFFFFFFFF, true);

			int btnX = panelX + PANEL_W - PAD;

			// Settings button for DeathMarker (...)
			if (row.index == 2) {
				int sx = btnX - SETTINGS_W - 4;
				boolean hoverS = mouseX >= sx && mouseX <= sx + SETTINGS_W &&
						mouseY >= absY + 2 && mouseY <= absY + 14;
				ctx.fill(sx, absY + 2, sx + SETTINGS_W, absY + 14, hoverS ? 0xFF555588 : 0xFF444466);
				ctx.drawText(textRenderer, "...", sx + 3, absY + 4, 0xFFFFFFFF, true);
				btnX = sx - 4;
			}

			// Settings button for VeinMiner (...)
			if (row.index == 4) {
				int sx = btnX - SETTINGS_W - 4;
				boolean hoverS = mouseX >= sx && mouseX <= sx + SETTINGS_W &&
						mouseY >= absY + 2 && mouseY <= absY + 14;
				ctx.fill(sx, absY + 2, sx + SETTINGS_W, absY + 14, hoverS ? 0xFF555588 : 0xFF444466);
				ctx.drawText(textRenderer, "...", sx + 3, absY + 4, 0xFFFFFFFF, true);
				btnX = sx - 4;
			}

			// Toggle button
			int bx = btnX - TOGGLE_W;
			int by = absY + 2;
			boolean hoverT = mouseX >= bx && mouseX <= bx + TOGGLE_W &&
					mouseY >= by && mouseY <= by + 12;
			ctx.fill(bx, by, bx + TOGGLE_W, by + 12, on ? 0xFF2A6B2A : hoverT ? 0xFF6B3A3A : 0xFF4A2A2A);
			String txt = on ? "ON" : "OFF";
			int tw = textRenderer.getWidth(txt);
			ctx.drawText(textRenderer, txt, bx + (TOGGLE_W - tw) / 2, by + 2, 0xFFFFFFFF, true);
		}

		super.render(ctx, mouseX, mouseY, delta);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

		QolConfig cfg = QolConfig.get();

		for (ToggleRow row : toggles) {
			int absY = panelY + PAD + 14 + row.index * ROW_H;
			int btnX = panelX + PANEL_W - PAD;

			// Settings button (...) for DeathMarker
			if (row.index == 2) {
				int sx = btnX - SETTINGS_W - 4;
				if (mouseX >= sx && mouseX <= sx + SETTINGS_W &&
						mouseY >= absY + 2 && mouseY <= absY + 14) {
					client.setScreen(new DeathMarkerSettingsScreen(this));
					return true;
				}
				btnX = sx - 4;
			}

			// Settings button (...) for VeinMiner
			if (row.index == 4) {
				int sx = btnX - SETTINGS_W - 4;
				if (mouseX >= sx && mouseX <= sx + SETTINGS_W &&
						mouseY >= absY + 2 && mouseY <= absY + 14) {
					client.setScreen(new VeinMinerSettingsScreen(this));
					return true;
				}
				btnX = sx - 4;
			}

			// Toggle button
			int bx = btnX - TOGGLE_W;
			int by = absY + 2;
			if (mouseX >= bx && mouseX <= bx + TOGGLE_W &&
					mouseY >= by && mouseY <= by + 12) {
				row.setter.accept(!row.getter.getAsBoolean());
				QolConfig.save();
				return true;
			}
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
