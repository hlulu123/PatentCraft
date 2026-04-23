package com.patentcraft.server;

import com.mojang.authlib.GameProfile;
import com.patentcraft.PatentCraft;
import com.patentcraft.network.PatentActionPayload;
import com.patentcraft.network.PatentInfo;
import com.patentcraft.network.PatentListPayload;
import com.patentcraft.network.ProfileResultPayload;
import com.patentcraft.network.StatusPayload;
import com.patentcraft.network.WhitelistPayload;
import com.patentcraft.util.PatentBookUtil;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;

public final class PatentNetworking {
	private PatentNetworking() {
	}

	public static void registerServer() {
		PayloadTypeRegistry.playS2C().register(PatentListPayload.ID, PatentListPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ProfileResultPayload.ID, ProfileResultPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(StatusPayload.ID, StatusPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(WhitelistPayload.ID, WhitelistPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(PatentActionPayload.ID, PatentActionPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(PatentActionPayload.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			context.server().execute(() -> {
				switch (payload.action()) {
					case MAKE_BOOK -> makeBook(player, payload.itemId());
				case LOOKUP -> lookup(player, payload.targetName());
				case GRANT -> grantOrRevoke(player, payload.itemId(), payload.targetName(), true);
				case REVOKE -> grantOrRevoke(player, payload.itemId(), payload.targetName(), false);
				case OPEN_SOURCE -> openSource(player, payload.itemId());
				}
			});
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sendWhitelist(handler.player));
	}

	public static void sendPatentList(ServerPlayerEntity player) {
		PatentState state = PatentState.get(player.getServer());
		List<PatentInfo> patents = state.listMakeable(player.getUuid()).stream()
			.map(record -> new PatentInfo(record, player.getUuid()))
			.toList();
		ServerPlayNetworking.send(player, new PatentListPayload(patents));
	}

	public static void sendStatus(ServerPlayerEntity player, String translationKey, String arg) {
		ServerPlayNetworking.send(player, new StatusPayload(translationKey, arg));
		player.sendMessage(arg == null || arg.isBlank() ? Text.translatable(translationKey) : Text.translatable(translationKey, arg), true);
	}

	public static void sendWhitelist(ServerPlayerEntity player) {
		PatentState state = PatentState.get(player.getServer());
		var openSourceItems = state.allPatents().stream()
			.filter(PatentRecord::openSource)
			.map(PatentRecord::itemId)
			.collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
		ServerPlayNetworking.send(player, new WhitelistPayload(PatentConfig.protectedItems(), openSourceItems));
	}

	public static void broadcastWhitelist(net.minecraft.server.MinecraftServer server) {
		server.getPlayerManager().getPlayerList().forEach(PatentNetworking::sendWhitelist);
	}

	private static void makeBook(ServerPlayerEntity player, String itemId) {
		ItemStack hand = player.getMainHandStack();
		if (!hand.isOf(Items.BOOK)) {
			hand = player.getOffHandStack();
		}
		if (!hand.isOf(Items.BOOK)) {
			sendStatus(player, "patentcraft.message.no_book", "");
			return;
		}

		Optional<PatentRecord> patent = PatentState.get(player.getServer()).getPatent(itemId);
		if (patent.isEmpty() || !patent.get().canMakeBook(player.getUuid())) {
			sendStatus(player, "patentcraft.message.owner_only", "");
			return;
		}

		hand.decrement(1);
		ItemStack book = PatentBookUtil.createPatentBook(patent.get());
		if (!player.getInventory().insertStack(book)) {
			player.dropItem(book, false);
		}
		sendStatus(player, "patentcraft.message.patent_book_created", itemId);
	}

	private static void lookup(ServerPlayerEntity player, String name) {
		Optional<GameProfile> profile = findProfile(player, name);
		if (profile.isPresent()) {
			GameProfile gameProfile = profile.get();
			ServerPlayNetworking.send(player, new ProfileResultPayload(true, gameProfile.getName(), gameProfile.getId()));
		} else {
			ServerPlayNetworking.send(player, new ProfileResultPayload(false, name, null));
			sendStatus(player, "patentcraft.message.profile_not_found", "");
		}
	}

	private static void grantOrRevoke(ServerPlayerEntity player, String itemId, String name, boolean grant) {
		Optional<GameProfile> profile = findProfile(player, name);
		if (profile.isEmpty()) {
			sendStatus(player, "patentcraft.message.profile_not_found", "");
			return;
		}

		GameProfile target = profile.get();
		PatentState state = PatentState.get(player.getServer());
		boolean changed = grant
			? state.grantMaker(itemId, player.getUuid(), target.getId(), target.getName())
			: state.revokeMaker(itemId, player.getUuid(), target.getId());

		if (!changed) {
			sendStatus(player, "patentcraft.message.owner_only", "");
			return;
		}

		sendPatentList(player);
		sendStatus(player, grant ? "patentcraft.message.granted" : "patentcraft.message.revoked", target.getName());
	}

	private static void openSource(ServerPlayerEntity player, String itemId) {
		PatentState state = PatentState.get(player.getServer());
		if (!state.openSourcePatent(itemId, player.getUuid())) {
			sendStatus(player, "patentcraft.message.owner_only", "");
			return;
		}
		sendPatentList(player);
		broadcastWhitelist(player.getServer());
		sendStatus(player, "patentcraft.message.open_sourced", itemId);
	}

	private static Optional<GameProfile> findProfile(ServerPlayerEntity player, String name) {
		if (name == null || name.isBlank()) {
			return Optional.empty();
		}
		return player.getServer().getUserCache().findByName(name.trim());
	}
}
