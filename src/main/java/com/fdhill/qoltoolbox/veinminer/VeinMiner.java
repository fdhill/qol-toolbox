package com.fdhill.qoltoolbox.veinminer;

import com.fdhill.qoltoolbox.config.QolConfig;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VeinMiner {
	private static final ThreadLocal<Boolean> PROCESSING = ThreadLocal.withInitial(() -> false);

	public static void register() {
		PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
			if (!(world instanceof ServerWorld serverWorld) || PROCESSING.get()) return true;
			QolConfig.VeinMiner cfg = QolConfig.get().veinminer;
			if (!cfg.enabled || !player.isSneaking()) return true;
			if (!isWhitelisted(state.getBlock())) return true;

			List<BlockPos> vein = findConnected(serverWorld, pos, state, cfg.maxBlocks);
			if (vein.size() <= 1) return true;

			ItemStack tool = player.getMainHandStack();
			PROCESSING.set(true);
			try {
				// vanilla breaks the original block; we break the extras
				for (BlockPos bp : vein) {
					if (bp.equals(pos)) continue;
					serverWorld.breakBlock(bp, true);
					if (!tool.isEmpty()) tool.damage(1, player, EquipmentSlot.MAINHAND);
				}
			} finally {
				PROCESSING.set(false);
			}
			return true;
		});
	}

	private static List<BlockPos> findConnected(ServerWorld world, BlockPos origin,
			BlockState originState, int max) {
		Block target = originState.getBlock();
		List<BlockPos> result = new ArrayList<>();
		result.add(origin);

		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		queue.add(origin);

		Set<BlockPos> visited = new HashSet<>();
		visited.add(origin);

		while (!queue.isEmpty() && result.size() < max) {
			BlockPos current = queue.poll();
			for (BlockPos neighbor : BlockPos.iterate(
					current.add(-1, -1, -1), current.add(1, 1, 1))) {
				if (visited.add(neighbor) && world.getBlockState(neighbor).getBlock() == target) {
					result.add(neighbor);
					queue.add(neighbor);
					if (result.size() >= max) break;
				}
			}
		}
		return result;
	}

	private static boolean isWhitelisted(Block block) {
		Identifier id = Registries.BLOCK.getId(block);
		for (String entry : QolConfig.get().veinminer.whitelist) {
			if (entry.equals(id.toString())) return true;
		}
		return false;
	}
}
