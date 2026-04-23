package com.patentcraft.network;

import com.patentcraft.PatentCraft;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record PatentActionPayload(Action action, String itemId, String targetName) implements CustomPayload {
	public static final CustomPayload.Id<PatentActionPayload> ID = new CustomPayload.Id<>(PatentCraft.id("patent_action"));
	public static final PacketCodec<RegistryByteBuf, PatentActionPayload> CODEC = PacketCodec.of(PatentActionPayload::write, PatentActionPayload::read);

	private void write(RegistryByteBuf buf) {
		buf.writeEnumConstant(action);
		buf.writeString(itemId == null ? "" : itemId);
		buf.writeString(targetName == null ? "" : targetName);
	}

	private static PatentActionPayload read(RegistryByteBuf buf) {
		return new PatentActionPayload(buf.readEnumConstant(Action.class), buf.readString(), buf.readString());
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

	public enum Action {
		MAKE_BOOK,
		LOOKUP,
		GRANT,
		REVOKE,
		OPEN_SOURCE
	}
}
