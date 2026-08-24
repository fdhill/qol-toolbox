package com.fdhill.qoltoolbox.fullbright;

import com.fdhill.qoltoolbox.config.QolConfig;
import com.fdhill.qoltoolbox.mixin.SimpleOptionAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;

// ponytail: raw gamma=15 while on; dragging the vanilla brightness slider mid-fullbright
// overrides it until the next toggle — acceptable, matches most gamma mods.
public class Fullbright {
	private static final double GAMMA_ON = 15.0;
	private static boolean on;
	private static double originalGamma = 1.0;

	public static boolean isOn() {
		return on;
	}

	public static void set(boolean value) {
		if (value == on) {
			return;
		}
		SimpleOption<Double> gamma = MinecraftClient.getInstance().options.getGamma();
		if (value) {
			originalGamma = gamma.getValue();
			((SimpleOptionAccessor<Double>) (Object) gamma).set(GAMMA_ON);
		} else {
			((SimpleOptionAccessor<Double>) (Object) gamma).set(originalGamma);
		}
		on = value;
		QolConfig.get().fullbright.enabled = value;
		QolConfig.save();
	}

	public static void toggle() {
		set(!on);
	}
}
