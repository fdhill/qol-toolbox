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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class DeathMarker {
	private static final Identifier SKULL_TEXTURE = Identifier.of("qoltoolbox", "textures/death_skull.png");
	private static final double CLEAR_DISTANCE = 5.0;
	private static final double PILLAR_HEIGHT = 180.0;
	private static final double PILLAR_WIDTH = 0.15;
	private static final float SKULL_SIZE = 1.6f;

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
		Matrix4f matrix = ctx.matrixStack().peek().getPositionMatrix();

		// Ponytail: draw translucent pillar first (behind skull)
		drawPillar(matrix, cam, cfg.x, cfg.y, cfg.z);

		// Draw billboard skull
		drawSkull(matrix, cam, cfg.x, cfg.y, cfg.z);
	}

	private static void drawSkull(Matrix4f matrix, Vec3d cam,
			double dx, double dy, double dz) {
		// Camera-relative position of death marker
		float cx = (float) (dx - cam.x);
		float cy = (float) (dy - cam.y);
		float cz = (float) (dz - cam.z);

		// Ponytail: proper billboard — forward, right, up from cross products
		// Forward = normalize(deathPos - camPos)
		float fx = cx, fy = cy, fz = cz;
		float fLen = (float) Math.sqrt(fx * fx + fy * fy + fz * fz);
		if (fLen < 0.001f) return;
		fx /= fLen; fy /= fLen; fz /= fLen;

		// World up = (0, 1, 0)
		// Right = normalize(forward × worldUp)
		float rx = fy * 0 - fz * 1;
		float ry = fz * 0 - fx * 0;
		float rz = fx * 1 - fy * 0;
		// Simplified: right = (-fz, 0, fx), but handle edge case
		rx = -fz; ry = 0; rz = fx;
		float rLen = (float) Math.sqrt(rx * rx + ry * ry + rz * rz);
		if (rLen < 0.001f) { // looking straight up/down, fallback
			rx = 1; ry = 0; rz = 0;
		} else {
			rx /= rLen; ry /= rLen; rz /= rLen;
		}

		// Up = normalize(right × forward)
		float ux = ry * fz - rz * fy;
		float uy = rz * fx - rx * fz;
		float uz = rx * fy - ry * fx;
		float uLen = (float) Math.sqrt(ux * ux + uy * uy + uz * uz);
		if (uLen > 0.001f) { ux /= uLen; uy /= uLen; uz /= uLen; }

		float half = SKULL_SIZE * 0.5f;

		// Four corners of billboard quad
		float x0 = cx - rx * half - ux * half;
		float y0 = cy - ry * half - uy * half;
		float z0 = cz - rz * half - uz * half;

		float x1 = cx + rx * half - ux * half;
		float y1 = cy + ry * half - uy * half;
		float z1 = cz + rz * half - uz * half;

		float x2 = cx + rx * half + ux * half;
		float y2 = cy + ry * half + uy * half;
		float z2 = cz + rz * half + uz * half;

		float x3 = cx - rx * half + ux * half;
		float y3 = cy - ry * half + uy * half;
		float z3 = cz - rz * half + uz * half;

		RenderSystem.setShaderTexture(0, SKULL_TEXTURE);
		RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableDepthTest();

		BufferBuilder buf = Tessellator.getInstance()
				.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
		buf.vertex(matrix, x0, y0, z0).texture(0, 1).color(255, 255, 255, 220);
		buf.vertex(matrix, x1, y1, z1).texture(1, 1).color(255, 255, 255, 220);
		buf.vertex(matrix, x2, y2, z2).texture(1, 0).color(255, 255, 255, 220);
		buf.vertex(matrix, x3, y3, z3).texture(0, 0).color(255, 255, 255, 220);
		BufferRenderer.drawWithGlobalProgram(buf.end());

		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
	}

	private static void drawPillar(Matrix4f matrix, Vec3d cam,
			double dx, double dy, double dz) {
		float x1 = (float) (dx - PILLAR_WIDTH - cam.x);
		float x2 = (float) (dx + PILLAR_WIDTH - cam.x);
		float z1 = (float) (dz - PILLAR_WIDTH - cam.z);
		float z2 = (float) (dz + PILLAR_WIDTH - cam.z);
		float y0 = (float) (dy - cam.y);
		float yTop = (float) (dy + PILLAR_HEIGHT - cam.y);

		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableDepthTest();

		// Ponytail: 4 translucent quads for pillar, much more visible than DEBUG_LINES
		int r = 255, g = 40, b = 40, a = 60;
		BufferBuilder buf = Tessellator.getInstance()
				.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		// +X face
		quad(buf, matrix, x2, y0, z1, x2, yTop, z1, x2, yTop, z2, x2, y0, z2, r, g, b, a);
		// -X face
		quad(buf, matrix, x1, y0, z2, x1, yTop, z2, x1, yTop, z1, x1, y0, z1, r, g, b, a);
		// +Z face
		quad(buf, matrix, x2, y0, z2, x2, yTop, z2, x1, yTop, z2, x1, y0, z2, r, g, b, a);
		// -Z face
		quad(buf, matrix, x1, y0, z1, x1, yTop, z1, x2, yTop, z1, x2, y0, z1, r, g, b, a);
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

	private static void quad(BufferBuilder buf, Matrix4f m,
			float x0, float y0, float z0, float x1, float y1, float z1,
			float x2, float y2, float z2, float x3, float y3, float z3,
			int r, int g, int b, int a) {
		buf.vertex(m, x0, y0, z0).color(r, g, b, a);
		buf.vertex(m, x1, y1, z1).color(r, g, b, a);
		buf.vertex(m, x2, y2, z2).color(r, g, b, a);
		buf.vertex(m, x3, y3, z3).color(r, g, b, a);
	}
}
