package com.patentcraft.mixin;

import com.patentcraft.server.PatentEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class ItemStackMixin {
	@Inject(method = "onCraftByPlayer", at = @At("HEAD"))
	private void patentcraft$onCraftByPlayer(World world, PlayerEntity player, int amount, CallbackInfo ci) {
		if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
			PatentEvents.handleCrafted(serverPlayer, (ItemStack) (Object) this);
		}
	}
}
