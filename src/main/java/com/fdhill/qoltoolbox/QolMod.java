package com.fdhill.qoltoolbox;

import com.fdhill.qoltoolbox.config.QolConfig;
import com.fdhill.qoltoolbox.veinminer.VeinMiner;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QolMod implements ModInitializer {
	public static final String MOD_ID = "qoltoolbox";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		QolConfig.load();
		VeinMiner.register();
		registerCommands();
		LOGGER.info("QoL Toolbox initialized");
	}

	private static void registerCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("qol").then(CommandManager.literal("veinminer")
				.then(CommandManager.literal("add").then(CommandManager.argument("block", StringArgumentType.word()).executes(ctx -> {
					String block = StringArgumentType.getString(ctx, "block");
					QolConfig.VeinMiner cfg = QolConfig.get().veinminer;
					if (cfg.whitelist.contains(block)) {
						ctx.getSource().sendFeedback(() -> Text.literal(block + " sudah ada di whitelist"), false);
						return 0;
					}
					cfg.whitelist.add(block);
					QolConfig.save();
					ctx.getSource().sendFeedback(() -> Text.literal("Ditambahkan: " + block), false);
					return 1;
				})))
				.then(CommandManager.literal("remove").then(CommandManager.argument("block", StringArgumentType.word()).executes(ctx -> {
					String block = StringArgumentType.getString(ctx, "block");
					QolConfig.VeinMiner cfg = QolConfig.get().veinminer;
					if (cfg.whitelist.remove(block)) {
						QolConfig.save();
						ctx.getSource().sendFeedback(() -> Text.literal("Dihapus: " + block), false);
					} else {
						ctx.getSource().sendFeedback(() -> Text.literal(block + " tidak ditemukan"), false);
					}
					return 1;
				})))
				.then(CommandManager.literal("list").executes(ctx -> {
					QolConfig.VeinMiner cfg = QolConfig.get().veinminer;
					ctx.getSource().sendFeedback(() -> Text.literal("Whitelist (" + cfg.whitelist.size() + "): " + String.join(", ", cfg.whitelist)), false);
					return cfg.whitelist.size();
				}))
				.then(CommandManager.literal("max").then(CommandManager.argument("count", IntegerArgumentType.integer(1, 128)).executes(ctx -> {
					int count = IntegerArgumentType.getInteger(ctx, "count");
					QolConfig.get().veinminer.maxBlocks = count;
					QolConfig.save();
					ctx.getSource().sendFeedback(() -> Text.literal("Max blocks: " + count), false);
					return 1;
				})))
				.then(CommandManager.literal("toggle").executes(ctx -> {
					QolConfig.VeinMiner cfg = QolConfig.get().veinminer;
					cfg.enabled = !cfg.enabled;
					QolConfig.save();
					ctx.getSource().sendFeedback(() -> Text.literal("Vein Miner: " + (cfg.enabled ? "ON" : "OFF")), false);
					return 1;
				}))
			));
		});
	}
}
