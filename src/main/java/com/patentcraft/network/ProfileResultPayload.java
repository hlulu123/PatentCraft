package com.patentcraft.network;

import com.patentcraft.PatentCraft;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.UUID;

public record ProfileResultPayload(boolean found, String name, UUID uuid) implements CustomPayload {
	public static final CustomPayload.Id<ProfileResultPayload> ID = new CustomPayload.Id<>(PatentCraft.id("profile_result"));
	public static final PacketCodec<RegistryByteBuf, ProfileResultPayload> CODEC = PacketCodec.of(ProfileResultPayload::write, ProfileResultPayload::read);

	private void write(RegistryByteBuf buf) {
		buf.writeBoolean(found);
		buf.writeString(name == null ? "" : name);
		buf.writeUuid(uuid == null ? new UUID(0L, 0L) : uuid);
	}

	private static ProfileResultPayload read(RegistryByteBuf buf) {
		boolean found = buf.readBoolean();
		String name = buf.readString();
		UUID uuid = buf.readUuid();
		return new ProfileResultPayload(found, name, uuid);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
