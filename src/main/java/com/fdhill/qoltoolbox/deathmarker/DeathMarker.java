package com.fdhill.qoltoolbox.deathmarker;

import com.fdhill.qoltoolbox.config.QolConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

// ponytail: beam = 4 vertical DEBUG_LINES (1px, no fill); swap to translucent quad column
// if visibility at range matters.
public class DeathMarker {
	private static final double CLEAR_DISTANCE = 5.0;
	private static final double BEAM_HALF_WIDTH = 0.12;
	private static final double BEAM_HEIGHT = 180.0;

	public static void record(ClientPlayerEntity player) {
		QolConfig.DeathMarker cfg = QolConfig.get().deathmarker;
		cfg.x = player.getX();
		cfg.y = player.getY();
		cfg.z = player.getZ();
		cfg.dimension = player.getWorld().getRegistryKey().getValue().toString();
		cfg.enabled = true;
		QolConfig.save();
	}

	public static void toggle() {
		QolConfig.DeathMarker cfg = QolConfig.get().deathmarker;
		cfg.enabled = !cfg.enabled;
		QolConfig.save();
	}

	private static void clear() {
		QolConfig.DeathMarker cfg = QolConfig.get().deathmarker;
		cfg.x = null;
		cfg.y = null;
		cfg.z = null;
		cfg.dimension = null;
		QolConfig.save();
	}

	public static void renderWorld(WorldRenderContext ctx) {
		QolConfig.DeathMarker cfg = QolConfig.get().deathmarker;
		if (!cfg.enabled || cfg.x == null || cfg.dimension == null) {
			return;
		}
		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlayerEntity player = client.player;
		if (player == null || client.world == null) {
			return;
		}
		if (!client.world.getRegistryKey().getValue().toString().equals(cfg.dimension)) {
			return;
		}
		Vec3d pos = new Vec3d(cfg.x, cfg.y, cfg.z);
		if (player.getPos().distanceTo(pos) < CLEAR_DISTANCE) {
			clear();
			return;
		}

		Vec3d cam = ctx.camera().getPos();
		double x1 = cfg.x - BEAM_HALF_WIDTH - cam.x;
		double x2 = cfg.x + BEAM_HALF_WIDTH - cam.x;
		double z1 = cfg.z - BEAM_HALF_WIDTH - cam.z;
		double z2 = cfg.z + BEAM_HALF_WIDTH - cam.z;
		double y0 = cfg.y - cam.y;
		double yTop = y0 + BEAM_HEIGHT;

		Matrix4f matrix = ctx.matrixStack().peek().getPositionMatrix();
		BufferBuilder buf = Tessellator.getInstance()
				.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
		line(buf, matrix, x1, y0, z1, x1, yTop, z1);
		line(buf, matrix, x2, y0, z1, x2, yTop, z1);
		line(buf, matrix, x1, y0, z2, x1, yTop, z2);
		line(buf, matrix, x2, y0, z2, x2, yTop, z2);
		line(buf, matrix, x1, y0, z1, x2, y0, z1);
		line(buf, matrix, x2, y0, z1, x2, y0, z2);
		line(buf, matrix, x2, y0, z2, x1, y0, z2);
		line(buf, matrix, x1, y0, z2, x1, y0, z1);

		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableDepthTest();
		BufferRenderer.drawWithGlobalProgram(buf.end());
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
	}

	public static void renderHud(DrawContext ctx, RenderTickCounter tickCounter) {
		QolConfig.DeathMarker cfg = QolConfig.get().deathmarker;
		if (!cfg.enabled || cfg.x == null) {
			return;
		}
		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlayerEntity player = client.player;
		if (player == null) {
			return;
		}
		Vec3d pos = new Vec3d(cfg.x, cfg.y, cfg.z);
		String coords = cfg.x.intValue() + " " + cfg.y.intValue() + " " + cfg.z.intValue();
		boolean sameDim = player.getWorld().getRegistryKey().getValue().toString().equals(cfg.dimension);
		String detail = sameDim ? (int) player.getPos().distanceTo(pos) + " m"
				: cfg.dimension.replaceFirst("^minecraft:", "");
		ctx.drawTextWithShadow(client.textRenderer,
				Text.translatable("text.qoltoolbox.death_marker_hud", coords, detail), 4, 4, 0xFFFF5555);
	}

	private static void line(BufferBuilder buf, Matrix4f m,
			double ax, double ay, double az, double bx, double by, double bz) {
		buf.vertex(m, (float) ax, (float) ay, (float) az).color(255, 60, 60, 200);
		buf.vertex(m, (float) bx, (float) by, (float) bz).color(255, 60, 60, 200);
	}
}
