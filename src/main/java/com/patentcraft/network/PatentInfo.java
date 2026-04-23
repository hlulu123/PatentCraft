package com.patentcraft.network;

import com.patentcraft.server.PatentRecord;
import net.minecraft.network.RegistryByteBuf;

import java.util.UUID;

public record PatentInfo(String itemId, UUID ownerUuid, String ownerName, boolean owner, boolean openSource) {
	public PatentInfo(PatentRecord record, UUID viewerUuid) {
		this(record.itemId(), record.ownerUuid(), record.ownerName(), record.ownerUuid().equals(viewerUuid), record.openSource());
	}

	public void write(RegistryByteBuf buf) {
		buf.writeString(itemId);
		buf.writeUuid(ownerUuid);
		buf.writeString(ownerName);
		buf.writeBoolean(owner);
		buf.writeBoolean(openSource);
	}

	public static PatentInfo read(RegistryByteBuf buf) {
		return new PatentInfo(buf.readString(), buf.readUuid(), buf.readString(), buf.readBoolean(), buf.readBoolean());
	}
}
