package com.fdhill.qoltoolbox.mixin;

import com.fdhill.qoltoolbox.deathmarker.DeathMarker;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {
	@Inject(method = "onDeath", at = @At("TAIL"))
	private void qoltoolbox$onDeath(DamageSource source, CallbackInfo ci) {
		DeathMarker.record((ClientPlayerEntity) (Object) this);
	}
}
