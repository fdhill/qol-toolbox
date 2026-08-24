package com.fdhill.qoltoolbox;

import com.fdhill.qoltoolbox.config.QolConfig;
import com.fdhill.qoltoolbox.fullbright.Fullbright;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
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

		if (QolConfig.get().fullbright.enabled) {
			Fullbright.set(true);
		}
	}
}
