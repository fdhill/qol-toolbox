package com.fdhill.qoltoolbox;

import com.fdhill.qoltoolbox.config.QolConfig;
import com.fdhill.qoltoolbox.fullbright.Fullbright;
import com.fdhill.qoltoolbox.trajectory.TrajectoryRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class QolClient implements ClientModInitializer {
	private static KeyBinding fullbrightKey;

	@Override
	public void onInitializeClient() {
		QolConfig.load();

		fullbrightKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.qoltoolbox.fullbright",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_B,
				"category.qoltoolbox"));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (fullbrightKey.wasPressed()) {
				Fullbright.toggle();
			}
		});

		if (QolConfig.get().trajectory.enabled) {
			WorldRenderEvents.AFTER_ENTITIES.register(TrajectoryRenderer::render);
		}

		if (QolConfig.get().fullbright.enabled) {
			Fullbright.set(true);
		}
	}
}
