package com.fdhill.qoltoolbox;

import com.fdhill.qoltoolbox.config.QolConfig;
import com.fdhill.qoltoolbox.deathmarker.DeathMarker;
import com.fdhill.qoltoolbox.dynamiclights.DynamicLights;
import com.fdhill.qoltoolbox.fullbright.Fullbright;
import com.fdhill.qoltoolbox.recipeviewer.RecipeViewerScreen;
import com.fdhill.qoltoolbox.settings.SettingsScreen;
import com.fdhill.qoltoolbox.trajectory.TrajectoryRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class QolClient implements ClientModInitializer {
	private static KeyBinding fullbrightKey;
	private static KeyBinding deathMarkerKey;
	private static KeyBinding recipeViewerKey;
	private static KeyBinding settingsKey;
	private static boolean restoredFullbright;
	private static boolean deathScreenShown;

	@Override
	public void onInitializeClient() {
		QolConfig.load();

		fullbrightKey = register("key.qoltoolbox.fullbright", GLFW.GLFW_KEY_B);
		deathMarkerKey = register("key.qoltoolbox.deathmarker", GLFW.GLFW_KEY_J);
		recipeViewerKey = register("key.qoltoolbox.recipeviewer", GLFW.GLFW_KEY_R);
		settingsKey = register("key.qoltoolbox.settings", GLFW.GLFW_KEY_K);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			// ponytail: defer fullbright restore to first tick — options is null during entrypoint
			if (!restoredFullbright && QolConfig.get().fullbright.enabled) {
				Fullbright.set(true);
				restoredFullbright = true;
			}
			while (fullbrightKey.wasPressed()) {
				Fullbright.toggle();
			}
			while (deathMarkerKey.wasPressed()) {
				DeathMarker.toggle();
			}
			while (recipeViewerKey.wasPressed()) {
				openRecipeViewer(client);
			}
			while (settingsKey.wasPressed()) {
				openSettings(client);
			}
			// Dynamic lights: update light block at player's feet
			if (client.player != null) {
				DynamicLights.tick(client.player);
			}
			// Death marker: detect death screen shown
			if (client.currentScreen instanceof DeathScreen) {
				if (!deathScreenShown && client.player != null) {
					deathScreenShown = true;
					DeathMarker.record(client.player);
				}
			} else {
				deathScreenShown = false;
			}
		});

		// Cleanup dynamic lights block when disconnecting
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			if (client.player != null) {
				DynamicLights.cleanup(client.player);
			}
		});

		WorldRenderEvents.AFTER_ENTITIES.register(TrajectoryRenderer::render);
		WorldRenderEvents.AFTER_ENTITIES.register(DeathMarker::renderWorld);
		HudRenderCallback.EVENT.register(DeathMarker::renderHud);
	}

	private static KeyBinding register(String name, int defaultKey) {
		return KeyBindingHelper.registerKeyBinding(new KeyBinding(
				name, InputUtil.Type.KEYSYM, defaultKey, "category.qoltoolbox"));
	}

	private static void openRecipeViewer(net.minecraft.client.MinecraftClient client) {
		if (client.currentScreen != null || client.world == null || client.player == null
				|| !QolConfig.get().recipeviewer.enabled) {
			return;
		}
		client.setScreen(new RecipeViewerScreen());
	}

	private static void openSettings(net.minecraft.client.MinecraftClient client) {
		if (client.currentScreen != null) {
			return;
		}
		client.setScreen(new SettingsScreen());
	}
}
