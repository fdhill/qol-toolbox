package com.fdhill.qoltoolbox;

import com.fdhill.qoltoolbox.config.QolConfig;
import com.fdhill.qoltoolbox.deathmarker.DeathMarker;
import com.fdhill.qoltoolbox.fullbright.Fullbright;
import com.fdhill.qoltoolbox.recipeviewer.RecipeViewerScreen;
import com.fdhill.qoltoolbox.trajectory.TrajectoryRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

public class QolClient implements ClientModInitializer {
	private static KeyBinding fullbrightKey;
	private static KeyBinding deathMarkerKey;
	private static KeyBinding recipeViewerKey;

	@Override
	public void onInitializeClient() {
		QolConfig.load();

		fullbrightKey = register("key.qoltoolbox.fullbright", GLFW.GLFW_KEY_B);
		deathMarkerKey = register("key.qoltoolbox.deathmarker", GLFW.GLFW_KEY_J);
		recipeViewerKey = register("key.qoltoolbox.recipeviewer", GLFW.GLFW_KEY_R);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (fullbrightKey.wasPressed()) {
				Fullbright.toggle();
			}
			while (deathMarkerKey.wasPressed()) {
				DeathMarker.toggle();
			}
			while (recipeViewerKey.wasPressed()) {
				openRecipeViewer(client);
			}
		});

		WorldRenderEvents.AFTER_ENTITIES.register(TrajectoryRenderer::render);
		WorldRenderEvents.AFTER_ENTITIES.register(DeathMarker::renderWorld);
		HudRenderCallback.EVENT.register(DeathMarker::renderHud);

		if (QolConfig.get().fullbright.enabled) {
			Fullbright.set(true);
		}
	}

	private static KeyBinding register(String name, int defaultKey) {
		return KeyBindingHelper.registerKeyBinding(new KeyBinding(
				name, InputUtil.Type.KEYSYM, defaultKey, "category.qoltoolbox"));
	}

	private static void openRecipeViewer(net.minecraft.client.MinecraftClient client) {
		ClientPlayerEntity player = client.player;
		if (client.currentScreen != null || client.world == null || player == null
				|| !QolConfig.get().recipeviewer.enabled) {
			return;
		}
		BlockPos pos = player.getBlockPos();
		for (BlockPos bp : BlockPos.iterate(pos.add(-4, -4, -4), pos.add(4, 4, 4))) {
			if (player.getWorld().getBlockState(bp).getBlock() instanceof CraftingTableBlock) {
				client.setScreen(new RecipeViewerScreen());
				return;
			}
		}
	}
}
