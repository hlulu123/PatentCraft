package com.patentcraft.screen;

import com.patentcraft.PatentCraft;
import com.patentcraft.server.PatentConfig;
import com.patentcraft.server.PatentNetworking;
import com.patentcraft.util.PatentBookUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;

public class PatentStationScreenHandler extends ScreenHandler {
	public static final int ADD_BUTTON = 0;
	public static final int REMOVE_BUTTON = 1;
	public static final int OPEN_SOURCE_BUTTON = 2;

	private final Inventory inventory;

	public PatentStationScreenHandler(int syncId, PlayerInventory playerInventory) {
		this(syncId, playerInventory, new SimpleInventory(1));
	}

	public PatentStationScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
		super(PatentCraft.PATENT_STATION_SCREEN_HANDLER, syncId);
		this.inventory = inventory;
		checkSize(inventory, 1);
		inventory.onOpen(playerInventory.player);

		addSlot(new Slot(inventory, 0, 80, 35));
		addPlayerInventory(playerInventory);
	}

	public boolean hasItem() {
		return !inventory.getStack(0).isEmpty();
	}

	public boolean insertedItemIsProtected() {
		ItemStack stack = inventory.getStack(0);
		return !stack.isEmpty() && PatentConfig.isProtected(PatentBookUtil.getItemId(stack));
	}

	@Override
	public boolean onButtonClick(PlayerEntity player, int id) {
		if (!(player instanceof ServerPlayerEntity serverPlayer)) {
			return false;
		}
		if (!player.getAbilities().creativeMode) {
			PatentNetworking.sendStatus(serverPlayer, "patentcraft.message.creative_only", "");
			return true;
		}

		ItemStack stack = inventory.getStack(0);
		if (stack.isEmpty()) {
			PatentNetworking.sendStatus(serverPlayer, "patentcraft.message.station_empty", "");
			return true;
		}

		String itemId = PatentBookUtil.getItemId(stack);
		if (id == ADD_BUTTON) {
			PatentConfig.addProtectedItem(itemId);
			PatentNetworking.broadcastWhitelist(serverPlayer.getServer());
			PatentNetworking.sendStatus(serverPlayer, "patentcraft.message.whitelist_added", stack.getName().getString());
			return true;
		}
		if (id == REMOVE_BUTTON) {
			PatentConfig.removeProtectedItem(itemId);
			PatentNetworking.broadcastWhitelist(serverPlayer.getServer());
			PatentNetworking.sendStatus(serverPlayer, "patentcraft.message.whitelist_removed", stack.getName().getString());
			return true;
		}
		if (id == OPEN_SOURCE_BUTTON) {
			PatentConfig.addProtectedItem(itemId);
			com.patentcraft.server.PatentState.get(serverPlayer.getServer()).forceOpenSource(itemId, player.getUuid(), player.getName().getString());
			PatentNetworking.broadcastWhitelist(serverPlayer.getServer());
			PatentNetworking.sendStatus(serverPlayer, "patentcraft.message.open_sourced", stack.getName().getString());
			return true;
		}
		return false;
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int slotIndex) {
		ItemStack result = ItemStack.EMPTY;
		Slot slot = slots.get(slotIndex);
		if (slot != null && slot.hasStack()) {
			ItemStack stack = slot.getStack();
			result = stack.copy();
			if (slotIndex == 0) {
				if (!insertItem(stack, 1, 37, true)) {
					return ItemStack.EMPTY;
				}
			} else if (!insertItem(stack, 0, 1, false)) {
				return ItemStack.EMPTY;
			}

			if (stack.isEmpty()) {
				slot.setStack(ItemStack.EMPTY);
			} else {
				slot.markDirty();
			}
		}
		return result;
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return inventory.canPlayerUse(player);
	}

	@Override
	public void onClosed(PlayerEntity player) {
		super.onClosed(player);
		inventory.onClose(player);
		if (!player.getWorld().isClient) {
			dropInventory(player, inventory);
		}
	}

	private void addPlayerInventory(PlayerInventory playerInventory) {
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
			}
		}

		for (int column = 0; column < 9; column++) {
			addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
		}
	}
}
