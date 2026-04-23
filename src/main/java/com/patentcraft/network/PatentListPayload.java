package com.patentcraft.network;

import com.patentcraft.PatentCraft;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.ArrayList;
import java.util.List;

public record PatentListPayload(List<PatentInfo> patents) implements CustomPayload {
	public static final CustomPayload.Id<PatentListPayload> ID = new CustomPayload.Id<>(PatentCraft.id("patent_list"));
	public static final PacketCodec<RegistryByteBuf, PatentListPayload> CODEC = PacketCodec.of(PatentListPayload::write, PatentListPayload::read);

	private void write(RegistryByteBuf buf) {
		buf.writeVarInt(patents.size());
		for (PatentInfo patent : patents) {
			patent.write(buf);
		}
	}

	private static PatentListPayload read(RegistryByteBuf buf) {
		int size = buf.readVarInt();
		List<PatentInfo> patents = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			patents.add(PatentInfo.read(buf));
		}
		return new PatentListPayload(patents);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
