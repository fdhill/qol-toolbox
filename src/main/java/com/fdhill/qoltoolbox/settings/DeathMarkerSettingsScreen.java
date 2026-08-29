package com.fdhill.qoltoolbox.settings;

import com.fdhill.qoltoolbox.config.QolConfig;
import com.fdhill.qoltoolbox.deathmarker.DeathMarker;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class DeathMarkerSettingsScreen extends Screen {
	private static final int PANEL_W = 220;
	private static final int ROW_H = 16;
	private static final int BTN_W = 100;
	private static final int PAD = 8;
	private static final int SWATCH_SIZE = 14;
	private static final int SWATCH_PAD = 4;

	private record ColorPreset(String label, int r, int g, int b) {}

	private static final ColorPreset[] COLORS = {
		new ColorPreset("R", 255, 40, 40),
		new ColorPreset("G", 40, 255, 40),
		new ColorPreset("B", 40, 40, 255),
		new ColorPreset("Y", 255, 255, 40),
		new ColorPreset("P", 180, 40, 255),
		new ColorPreset("W", 255, 255, 255),
		new ColorPreset("C", 40, 255, 255),
		new ColorPreset("O", 255, 140, 40),
	};

	private final Screen parent;
	private int panelX;
	private int panelY;
	private int panelH;

	public DeathMarkerSettingsScreen(Screen parent) {
		super(Text.literal("Death Marker Settings"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		// title(14) + sep(1+8) + "Last Death"(ROW_H+4) + X/Y/Z(ROW_H) + Dim(ROW_H+8)
		// + "Pillar Color"(ROW_H+4) + swatches(SWATCH_SIZE+4) + gap(8)
		// + Reset(ROW_H+4) + bottom pad(4)
		panelH = PAD + 14 + 1 + 8 + (ROW_H + 4) + ROW_H + (ROW_H + 8)
				+ (ROW_H + 4) + (SWATCH_SIZE + 4) + 8
				+ (ROW_H + 4) + 4;
		panelX = (width - PANEL_W) / 2;
		panelY = (height - panelH) / 2;
	}

	@Override
	public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		QolConfig.DeathMarker cfg = QolConfig.get().deathmarker;

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

		// Last Death info
		int y = sepY + 8;
		ctx.drawText(textRenderer, "Last Death:", panelX + PAD, y, 0xFFAAAAAA, true);
		y += ROW_H + 4;

		if (cfg.x != null) {
			String dim = cfg.dimension != null ? cfg.dimension.replaceFirst("^minecraft:", "") : "?";
			ctx.drawText(textRenderer, "X: " + cfg.x.intValue(), panelX + PAD + 4, y, 0xFFFFFFFF, false);
			ctx.drawText(textRenderer, "Y: " + cfg.y.intValue(), panelX + PAD + 60, y, 0xFFFFFFFF, false);
			ctx.drawText(textRenderer, "Z: " + cfg.z.intValue(), panelX + PAD + 120, y, 0xFFFFFFFF, false);
			y += ROW_H;
			ctx.drawText(textRenderer, "Dimension: " + dim, panelX + PAD + 4, y, 0xFFFFFFFF, false);
			y += ROW_H + 8;
		} else {
			ctx.drawText(textRenderer, "(none)", panelX + PAD + 4, y, 0xFF666666, false);
			y += ROW_H + 8;
		}

		// Pillar Color section
		ctx.drawText(textRenderer, "Pillar Color:", panelX + PAD, y, 0xFFAAAAAA, true);
		y += ROW_H + 4;

		int totalW = COLORS.length * (SWATCH_SIZE + SWATCH_PAD) - SWATCH_PAD;
		int swatchX = panelX + (PANEL_W - totalW) / 2;
		for (int i = 0; i < COLORS.length; i++) {
			int sx = swatchX + i * (SWATCH_SIZE + SWATCH_PAD);
			ColorPreset c = COLORS[i];
			boolean active = cfg.pillarR == c.r && cfg.pillarG == c.g && cfg.pillarB == c.b;
			boolean hover = mouseX >= sx && mouseX <= sx + SWATCH_SIZE &&
					mouseY >= y && mouseY <= y + SWATCH_SIZE;

			if (active) {
				ctx.fill(sx - 1, y - 1, sx + SWATCH_SIZE + 1, y + SWATCH_SIZE + 1, 0xFFFFFFFF);
			}
			int packed = 0xFF000000 | (c.r << 16) | (c.g << 8) | c.b;
			ctx.fill(sx, y, sx + SWATCH_SIZE, y + SWATCH_SIZE, packed);
			if (hover && !active) {
				ctx.fill(sx, y, sx + SWATCH_SIZE, y + SWATCH_SIZE, 0x40FFFFFF);
			}
		}
		y += SWATCH_SIZE + 12;

		// Reset button
		int bx = panelX + (PANEL_W - BTN_W) / 2;
		boolean hoverReset = mouseX >= bx && mouseX <= bx + BTN_W &&
				mouseY >= y && mouseY <= y + 14;
		ctx.fill(bx, y, bx + BTN_W, y + 14, hoverReset ? 0xFFFF5555 : 0xFF883333);
		String resetTxt = "Reset Marker";
		int tw = textRenderer.getWidth(resetTxt);
		ctx.drawText(textRenderer, resetTxt, bx + (BTN_W - tw) / 2, y + 3, 0xFFFFFFFF, true);

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

		QolConfig.DeathMarker cfg = QolConfig.get().deathmarker;

		int y = panelY + PAD + 14 + 1 + 8 + (ROW_H + 4);
		if (cfg.x != null) {
			y += ROW_H + (ROW_H + 8);
		} else {
			y += ROW_H + 8;
		}

		// Pillar Color swatches
		y += ROW_H + 4;
		int totalW = COLORS.length * (SWATCH_SIZE + SWATCH_PAD) - SWATCH_PAD;
		int swatchX = panelX + (PANEL_W - totalW) / 2;
		for (int i = 0; i < COLORS.length; i++) {
			int sx = swatchX + i * (SWATCH_SIZE + SWATCH_PAD);
			ColorPreset c = COLORS[i];
			if (mouseX >= sx && mouseX <= sx + SWATCH_SIZE &&
					mouseY >= y && mouseY <= y + SWATCH_SIZE) {
				cfg.pillarR = c.r;
				cfg.pillarG = c.g;
				cfg.pillarB = c.b;
				QolConfig.save();
				return true;
			}
		}
		y += SWATCH_SIZE + 12;

		// Reset button
		int bx = panelX + (PANEL_W - BTN_W) / 2;
		if (mouseX >= bx && mouseX <= bx + BTN_W &&
				mouseY >= y && mouseY <= y + 14) {
			DeathMarker.reset();
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
