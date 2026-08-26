package com.fdhill.qoltoolbox.mixin;

import com.fdhill.qoltoolbox.QolMod;
import com.fdhill.qoltoolbox.config.QolConfig;
import com.fdhill.qoltoolbox.veinminer.VeinMiner;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Block.class)
public abstract class BlockBreakMixin {

	@Inject(at = @At("HEAD"), method = "Lnet/minecraft/block/Block;onBreak(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/block/BlockState;")
	public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfoReturnable<BlockState> cir) {
		if (world.isClient()) return;
		if (!(world instanceof ServerWorld serverWorld)) return;
		if (VeinMiner.processing) return;
		if (!player.isSneaking()) return;

		QolConfig.VeinMiner cfg = QolConfig.get().veinminer;
		if (!cfg.enabled) return;
		if (!VeinMiner.isWhitelisted(state.getBlock())) return;

		List<BlockPos> vein = VeinMiner.findConnected(serverWorld, pos, state, cfg.maxBlocks);
		if (vein.size() <= 1) return;

		QolMod.LOGGER.info("[VeinMiner] Mine {} connected {} blocks from {}", vein.size(),
				Registries.BLOCK.getId(state.getBlock()), pos);

		VeinMiner.processing = true;
		try {
			ItemStack tool = player.getMainHandStack();
			boolean isCreative = player.isCreative();
			int broken = 0;

			for (BlockPos bp : vein) {
				if (bp.equals(pos)) continue; // vanilla already breaks origin

				BlockState bs = serverWorld.getBlockState(bp);
				if (bs.isAir()) continue;

				// ponytail: breakBlock with player entity for proper loot + client sync
				serverWorld.breakBlock(bp, true, player);

				if (!tool.isEmpty() && !isCreative) {
					tool.damage(1, player, EquipmentSlot.MAINHAND);
					if (tool.getDamage() >= tool.getMaxDamage()) break;
				}
				broken++;
			}
			QolMod.LOGGER.info("[VeinMiner] Broke {} extra blocks", broken);
		} finally {
			VeinMiner.processing = false;
		}
	}
}
