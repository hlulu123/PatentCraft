package com.patentcraft.mixin;

import com.patentcraft.server.PatentEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
	@Inject(method = "attack", at = @At("HEAD"), cancellable = true)
	private void patentcraft$blockAttack(Entity target, CallbackInfo ci) {
		PlayerEntity player = (PlayerEntity) (Object) this;
		if (player instanceof ServerPlayerEntity serverPlayer && PatentEvents.shouldBlock(serverPlayer, serverPlayer.getMainHandStack(), serverPlayer.getWorld())) {
			ci.cancel();
		}
	}
}
