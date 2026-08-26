package com.fdhill.qoltoolbox.veinminer;

import com.fdhill.qoltoolbox.config.QolConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VeinMiner {
	public static boolean processing;

	public static List<BlockPos> findConnected(ServerWorld world, BlockPos origin,
			BlockState originState, int max) {
		Block target = originState.getBlock();
		List<BlockPos> result = new ArrayList<>();
		result.add(new BlockPos(origin)); // ponytail: copy origin to be safe

		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		queue.add(new BlockPos(origin));

		Set<Long> visited = new HashSet<>();
		visited.add(origin.asLong());

		while (!queue.isEmpty() && result.size() < max) {
			BlockPos current = queue.poll();
			for (BlockPos neighbor : BlockPos.iterate(
					current.add(-1, -1, -1), current.add(1, 1, 1))) {
				long key = neighbor.asLong();
				if (visited.add(key) && world.getBlockState(neighbor).getBlock() == target) {
					result.add(new BlockPos(neighbor)); // ponytail: copy mutable to immutable
					queue.add(new BlockPos(neighbor));
					if (result.size() >= max) break;
				}
			}
		}
		return result;
	}

	public static boolean isWhitelisted(Block block) {
		Identifier id = Registries.BLOCK.getId(block);
		for (String entry : QolConfig.get().veinminer.whitelist) {
			if (entry.equals(id.toString())) return true;
		}
		return false;
	}
}
