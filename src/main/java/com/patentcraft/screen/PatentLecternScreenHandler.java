package com.patentcraft.screen;

import com.patentcraft.PatentCraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

public class PatentLecternScreenHandler extends ScreenHandler {
	public PatentLecternScreenHandler(int syncId, PlayerInventory inventory) {
		super(PatentCraft.PATENT_LECTERN_SCREEN_HANDLER, syncId);
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return true;
	}
}
