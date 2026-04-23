package com.patentcraft.mixin;

import com.patentcraft.server.PatentEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.SmithingScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SmithingScreenHandler.class)
public class SmithingScreenHandlerMixin {
	@Inject(method = "onTakeOutput", at = @At("RETURN"))
	private void patentcraft$onTakeOutput(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
		if (player instanceof ServerPlayerEntity serverPlayer) {
			PatentEvents.handleCrafted(serverPlayer, stack);
		}
	}
}
