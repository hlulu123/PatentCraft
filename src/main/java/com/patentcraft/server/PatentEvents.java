package com.patentcraft.server;

import com.patentcraft.network.PatentActionPayload;
import com.patentcraft.screen.PatentLecternScreenHandler;
import com.patentcraft.util.PatentBookUtil;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public final class PatentEvents {
	private PatentEvents() {
	}

	public static void register() {
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (!world.isClient && world.getBlockState(hitResult.getBlockPos()).isOf(Blocks.LECTERN) && player.getStackInHand(hand).isOf(Items.BOOK)) {
				player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
					(syncId, inventory, p) -> new PatentLecternScreenHandler(syncId, inventory),
					Text.translatable("patentcraft.screen.title")
				));
				PatentNetworking.sendPatentList((ServerPlayerEntity) player);
				return ActionResult.SUCCESS;
			}

			if (isBlocked(player, player.getStackInHand(hand), world)) {
				return ActionResult.FAIL;
			}
			return ActionResult.PASS;
		});

		UseItemCallback.EVENT.register((player, world, hand) -> {
			ItemStack stack = player.getStackInHand(hand);
			if (isBlocked(player, stack, world)) {
				return TypedActionResult.fail(stack);
			}
			return TypedActionResult.pass(stack);
		});

		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (isBlocked(player, player.getStackInHand(hand), world)) {
				return ActionResult.FAIL;
			}
			return ActionResult.PASS;
		});

		PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> !isBlocked(player, player.getMainHandStack(), world));

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % 20 == 0) {
				server.getPlayerManager().getPlayerList().forEach(PatentEvents::removeBlockedArmor);
			}
		});
	}

	public static void handleCrafted(ServerPlayerEntity player, ItemStack crafted) {
		String itemId = PatentBookUtil.getItemId(crafted);
		if (!PatentConfig.isProtected(itemId)) {
			return;
		}

		PatentState state = PatentState.get(player.getServer());
		if (state.getPatent(itemId).isPresent()) {
			return;
		}

		PatentRecord record = state.createPatent(itemId, player.getUuid(), player.getName().getString());
		PatentBookUtil.authorizeItem(crafted, record);
		ItemStack book = PatentBookUtil.createPatentBook(record);
		if (!player.getInventory().insertStack(book)) {
			player.dropItem(book, false);
		}
		player.getServer().getPlayerManager().broadcast(
			Text.translatable("patentcraft.message.patent_created", player.getName(), Text.literal(itemId)),
			false
		);
		PatentNetworking.sendPatentList(player);
	}

	private static boolean isBlocked(PlayerEntity player, ItemStack stack, World world) {
		if (stack.isEmpty()) {
			return false;
		}

		String itemId = PatentBookUtil.getItemId(stack);
		if (!PatentConfig.isProtected(itemId)) {
			return false;
		}

		PatentState state = world.getServer() == null ? null : PatentState.get(world.getServer());
		if (state == null || state.getPatent(itemId).isEmpty() || state.isOpenSource(itemId)) {
			return false;
		}

		if (PatentBookUtil.isAuthorizedFor(stack, itemId)) {
			return false;
		}

		if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
			PatentNetworking.sendStatus(serverPlayer, "patentcraft.message.not_authorized", "");
		}
		return true;
	}

	public static boolean shouldBlock(PlayerEntity player, ItemStack stack, World world) {
		return isBlocked(player, stack, world);
	}

	private static void removeBlockedArmor(ServerPlayerEntity player) {
		for (int i = 0; i < player.getInventory().armor.size(); i++) {
			ItemStack stack = player.getInventory().armor.get(i);
			if (!stack.isEmpty() && isBlocked(player, stack, player.getWorld())) {
				player.getInventory().armor.set(i, ItemStack.EMPTY);
				if (!player.getInventory().insertStack(stack)) {
					player.dropItem(stack, false);
				}
			}
		}
	}
}
