package com.patentcraft.network;

import com.patentcraft.PatentCraft;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record StatusPayload(String translationKey, String arg) implements CustomPayload {
	public static final CustomPayload.Id<StatusPayload> ID = new CustomPayload.Id<>(PatentCraft.id("status"));
	public static final PacketCodec<RegistryByteBuf, StatusPayload> CODEC = PacketCodec.of(StatusPayload::write, StatusPayload::read);

	private void write(RegistryByteBuf buf) {
		buf.writeString(translationKey);
		buf.writeString(arg == null ? "" : arg);
	}

	private static StatusPayload read(RegistryByteBuf buf) {
		return new StatusPayload(buf.readString(), buf.readString());
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
