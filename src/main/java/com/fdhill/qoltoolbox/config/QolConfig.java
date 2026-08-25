package com.fdhill.qoltoolbox.config;

import com.fdhill.qoltoolbox.QolMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class QolConfig {
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("qoltoolbox.json");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public Fullbright fullbright = new Fullbright();
	public Trajectory trajectory = new Trajectory();
	public DeathMarker deathmarker = new DeathMarker();
	public RecipeViewer recipeviewer = new RecipeViewer();
	// Later phases append their sections here (veinminer).

	public static class Fullbright {
		public boolean enabled = false;
	}

	public static class Trajectory {
		public boolean enabled = true;
	}

	public static class DeathMarker {
		public boolean enabled = true;
		public Double x;
		public Double y;
		public Double z;
		public String dimension;
	}

	public static class RecipeViewer {
		public boolean enabled = true;
	}

	private static QolConfig instance;

	public static QolConfig get() {
		return instance;
	}

	public static void load() {
		if (instance != null) {
			return;
		}
		try {
			if (Files.exists(PATH)) {
				instance = GSON.fromJson(Files.readString(PATH), QolConfig.class);
			}
		} catch (Exception e) {
			QolMod.LOGGER.error("Failed to read config, using defaults", e);
		}
		if (instance == null) {
			instance = new QolConfig();
			save();
		}
	}

	public static void save() {
		try {
			Files.createDirectories(PATH.getParent());
			Files.writeString(PATH, GSON.toJson(instance));
		} catch (Exception e) {
			QolMod.LOGGER.error("Failed to save config", e);
		}
	}
}
