package com.fdhill.qoltoolbox.trajectory;

import com.fdhill.qoltoolbox.config.QolConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

// ponytail: straight-line raycast per simulated tick; ignores block-collision sub-steps,
// so very thin gaps can be skipped at high speeds — upgrade to ProjectileUtil sweep if it matters.
public class TrajectoryRenderer {
	private static final double GRAVITY = 0.05;
	private static final double DRAG = 0.99;
	private static final double BOW_MAX_SPEED = 3.0;
	private static final double CROSSBOW_SPEED = 3.15;
	private static final int MAX_TICKS = 300;

	public static void render(WorldRenderContext ctx) {
		if (!QolConfig.get().trajectory.enabled) {
			return;
		}
		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlayerEntity player = client.player;
		if (player == null || client.world == null) {
			return;
		}
		Vec3d velocity = initialVelocity(player);
		if (velocity == null) {
			return;
		}

		float delta = ctx.tickCounter().getTickDelta(true);
		Vec3d pos = player.getLerpedPos(delta).add(0, player.getStandingEyeHeight(), 0);
		List<Vec3d> points = new ArrayList<>();
		points.add(pos);
		Vec3d landing = null;
		for (int i = 0; i < MAX_TICKS; i++) {
			BlockHitResult hit = client.world.raycast(new RaycastContext(
					pos, pos.add(velocity),
					RaycastContext.ShapeType.COLLIDER,
					RaycastContext.FluidHandling.NONE, player));
			if (hit.getType() != HitResult.Type.MISS) {
				landing = hit.getPos();
				break;
			}
			pos = pos.add(velocity);
			points.add(pos);
			velocity = velocity.multiply(DRAG).subtract(0, GRAVITY, 0);
		}
		if (points.size() < 2 && landing == null) {
			return;
		}

		Matrix4f matrix = ctx.matrixStack().peek().getPositionMatrix();
		Vec3d cam = ctx.camera().getPos();
		BufferBuilder buf = Tessellator.getInstance()
				.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
		for (int i = 1; i < points.size(); i++) {
			line(buf, matrix, points.get(i - 1).subtract(cam), points.get(i).subtract(cam));
		}
		if (landing != null) {
			boxEdges(buf, matrix, new Box(landing, landing).expand(0.06), cam);
		}
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableDepthTest();
		BufferRenderer.drawWithGlobalProgram(buf.end());
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
	}

	private static Vec3d initialVelocity(ClientPlayerEntity player) {
		Vec3d dir = player.getRotationVec(1.0f);
		if (player.isUsingItem() && player.getActiveItem().getItem() instanceof BowItem) {
			float pull = BowItem.getPullProgress(player.getItemUseTime());
			return dir.multiply(pull * BOW_MAX_SPEED);
		}
		ItemStack main = player.getMainHandStack();
		ItemStack off = player.getOffHandStack();
		if ((main.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(main))
				|| (off.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(off))) {
			return dir.multiply(CROSSBOW_SPEED);
		}
		return null;
	}

	private static void line(BufferBuilder buf, Matrix4f m, Vec3d a, Vec3d b) {
		buf.vertex(m, (float) a.x, (float) a.y, (float) a.z).color(255, 255, 220, 210);
		buf.vertex(m, (float) b.x, (float) b.y, (float) b.z).color(255, 255, 220, 210);
	}

	private static void boxEdges(BufferBuilder buf, Matrix4f m, Box box, Vec3d cam) {
		double x1 = box.minX - cam.x;
		double y1 = box.minY - cam.y;
		double z1 = box.minZ - cam.z;
		double x2 = box.maxX - cam.x;
		double y2 = box.maxY - cam.y;
		double z2 = box.maxZ - cam.z;
		line(buf, m, new Vec3d(x1, y1, z1), new Vec3d(x2, y1, z1));
		line(buf, m, new Vec3d(x2, y1, z1), new Vec3d(x2, y1, z2));
		line(buf, m, new Vec3d(x2, y1, z2), new Vec3d(x1, y1, z2));
		line(buf, m, new Vec3d(x1, y1, z2), new Vec3d(x1, y1, z1));
		line(buf, m, new Vec3d(x1, y1, z1), new Vec3d(x1, y2, z1));
		line(buf, m, new Vec3d(x2, y1, z1), new Vec3d(x2, y2, z1));
		line(buf, m, new Vec3d(x1, y1, z2), new Vec3d(x1, y2, z2));
		line(buf, m, new Vec3d(x2, y1, z2), new Vec3d(x2, y2, z2));
		line(buf, m, new Vec3d(x1, y2, z1), new Vec3d(x2, y2, z1));
		line(buf, m, new Vec3d(x2, y2, z1), new Vec3d(x2, y2, z2));
		line(buf, m, new Vec3d(x2, y2, z2), new Vec3d(x1, y2, z2));
		line(buf, m, new Vec3d(x1, y2, z2), new Vec3d(x1, y2, z1));
	}
}
