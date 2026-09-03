package com.fdhill.qoltoolbox.dynamiclights;

import com.fdhill.qoltoolbox.config.QolConfig;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

import static java.util.Map.entry;

// ponytail: place light block at player's feet when holding a light-emitting item.
// only updates on item/position change to minimize block updates.
public class DynamicLights {
	private static final Map<Item, Block> ITEM_TO_BLOCK = Map.ofEntries(
		entry(Items.TORCH, Blocks.TORCH),
		entry(Items.SOUL_TORCH, Blocks.SOUL_TORCH),
		entry(Items.LANTERN, Blocks.LANTERN),
		entry(Items.SOUL_LANTERN, Blocks.SOUL_LANTERN),
		entry(Items.GLOWSTONE, Blocks.GLOWSTONE),
		entry(Items.SEA_LANTERN, Blocks.SEA_LANTERN),
		entry(Items.JACK_O_LANTERN, Blocks.JACK_O_LANTERN),
		entry(Items.SHROOMLIGHT, Blocks.SHROOMLIGHT),
		entry(Items.CRYING_OBSIDIAN, Blocks.CRYING_OBSIDIAN),
		entry(Items.END_ROD, Blocks.END_ROD),
		entry(Items.REDSTONE_LAMP, Blocks.REDSTONE_LAMP)
	);

	private static Block placedBlock = null;
	private static BlockPos placedPos = null;

	public static void tick(ClientPlayerEntity player) {
		if (!QolConfig.get().dynamiclights.enabled || player == null) {
			cleanup(player);
			return;
		}

		Block target = ITEM_TO_BLOCK.get(player.getMainHandStack().getItem());
		BlockPos pos = player.getBlockPos();

		// skip if nothing changed
		if (target == placedBlock && pos.equals(placedPos)) {
			return;
		}

		// remove old block
		cleanup(player);

		// place new block only in air to avoid destroying existing blocks
		if (target != null && player.getWorld().isAir(pos)) {
			player.getWorld().setBlockState(pos, target.getDefaultState(), Block.NOTIFY_ALL);
			placedBlock = target;
			placedPos = pos;
		}
	}

	public static void cleanup(ClientPlayerEntity player) {
		if (placedPos != null && player != null && player.getWorld() != null) {
			if (player.getWorld().getBlockState(placedPos).getBlock() == placedBlock) {
				player.getWorld().setBlockState(placedPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
			}
		}
		placedBlock = null;
		placedPos = null;
	}
}
