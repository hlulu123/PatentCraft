package com.patentcraft.network;

import com.patentcraft.PatentCraft;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.LinkedHashSet;
import java.util.Set;

public record WhitelistPayload(Set<String> itemIds, Set<String> openSourceItemIds) implements CustomPayload {
	public static final CustomPayload.Id<WhitelistPayload> ID = new CustomPayload.Id<>(PatentCraft.id("whitelist"));
	public static final PacketCodec<RegistryByteBuf, WhitelistPayload> CODEC = PacketCodec.of(WhitelistPayload::write, WhitelistPayload::read);

	private void write(RegistryByteBuf buf) {
		buf.writeVarInt(itemIds.size());
		for (String itemId : itemIds) {
			buf.writeString(itemId);
		}
		buf.writeVarInt(openSourceItemIds.size());
		for (String itemId : openSourceItemIds) {
			buf.writeString(itemId);
		}
	}

	private static WhitelistPayload read(RegistryByteBuf buf) {
		int size = buf.readVarInt();
		Set<String> itemIds = new LinkedHashSet<>();
		for (int i = 0; i < size; i++) {
			itemIds.add(buf.readString());
		}
		int openSize = buf.readVarInt();
		Set<String> openSourceItemIds = new LinkedHashSet<>();
		for (int i = 0; i < openSize; i++) {
			openSourceItemIds.add(buf.readString());
		}
		return new WhitelistPayload(itemIds, openSourceItemIds);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
