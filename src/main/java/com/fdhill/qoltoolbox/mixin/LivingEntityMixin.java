package com.fdhill.qoltoolbox.mixin;

import com.fdhill.qoltoolbox.deathmarker.DeathMarker;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@Inject(method = "onDeath(Lnet/minecraft/entity/damage/DamageSource;)V", at = @At("TAIL"))
	private void qoltoolbox$onDeath(DamageSource source, CallbackInfo ci) {
		// ponytail: filter only player entities, LivingEntity mixin fires for all entities
		if ((Object) this instanceof ClientPlayerEntity) {
			DeathMarker.record((ClientPlayerEntity) (Object) this);
		}
	}
}
