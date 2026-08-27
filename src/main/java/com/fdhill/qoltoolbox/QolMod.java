package com.fdhill.qoltoolbox;

import com.fdhill.qoltoolbox.config.QolConfig;
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
		registerCommands();
	}

	private static void registerCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("qol").then(CommandManager.literal("veinminer")
				.then(CommandManager.literal("add").then(CommandManager.argument("block", StringArgumentType.word()).executes(ctx -> {
					String block = StringArgumentType.getString(ctx, "block");
					QolConfig.VeinMiner cfg = QolConfig.get().veinminer;
					if (cfg.whitelist.contains(block)) {
						ctx.getSource().sendFeedback(() -> Text.translatable("commands.qoltoolbox.veinminer.already_added", block), false);
						return 0;
					}
					cfg.whitelist.add(block);
					QolConfig.save();
					ctx.getSource().sendFeedback(() -> Text.translatable("commands.qoltoolbox.veinminer.added", block), false);
					return 1;
				})))
				.then(CommandManager.literal("remove").then(CommandManager.argument("block", StringArgumentType.word()).executes(ctx -> {
					String block = StringArgumentType.getString(ctx, "block");
					QolConfig.VeinMiner cfg = QolConfig.get().veinminer;
					if (cfg.whitelist.remove(block)) {
						QolConfig.save();
						ctx.getSource().sendFeedback(() -> Text.translatable("commands.qoltoolbox.veinminer.removed", block), false);
					} else {
						ctx.getSource().sendFeedback(() -> Text.translatable("commands.qoltoolbox.veinminer.not_found", block), false);
					}
					return 1;
				})))
				.then(CommandManager.literal("list").executes(ctx -> {
					QolConfig.VeinMiner cfg = QolConfig.get().veinminer;
					String list = String.join(", ", cfg.whitelist);
					ctx.getSource().sendFeedback(() -> Text.translatable("commands.qoltoolbox.veinminer.list", cfg.whitelist.size(), list), false);
					return cfg.whitelist.size();
				}))
				.then(CommandManager.literal("max").then(CommandManager.argument("count", IntegerArgumentType.integer(1, 128)).executes(ctx -> {
					int count = IntegerArgumentType.getInteger(ctx, "count");
					QolConfig.get().veinminer.maxBlocks = count;
					QolConfig.save();
					ctx.getSource().sendFeedback(() -> Text.translatable("commands.qoltoolbox.veinminer.max_set", count), false);
					return 1;
				})))
				.then(CommandManager.literal("toggle").executes(ctx -> {
					QolConfig.VeinMiner cfg = QolConfig.get().veinminer;
					cfg.enabled = !cfg.enabled;
					QolConfig.save();
					String state = cfg.enabled ? "ON" : "OFF";
					ctx.getSource().sendFeedback(() -> Text.translatable("commands.qoltoolbox.veinminer.toggle", state), false);
					return 1;
				}))
			));
		});
	}
}
