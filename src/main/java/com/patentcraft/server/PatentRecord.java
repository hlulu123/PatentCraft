package com.patentcraft.server;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public record PatentRecord(
	String itemId,
	UUID ownerUuid,
	String ownerName,
	long createdAt,
	boolean openSource,
	Map<UUID, String> makers
) {
	public PatentRecord(String itemId, UUID ownerUuid, String ownerName, long createdAt) {
		this(itemId, ownerUuid, ownerName, createdAt, false, new LinkedHashMap<>());
	}

	public boolean canMakeBook(UUID playerUuid) {
		return ownerUuid.equals(playerUuid) || makers.containsKey(playerUuid);
	}

	public boolean isOwner(UUID playerUuid) {
		return ownerUuid.equals(playerUuid);
	}

	public NbtCompound toNbt() {
		NbtCompound nbt = new NbtCompound();
		nbt.putString("item_id", itemId);
		nbt.putUuid("owner_uuid", ownerUuid);
		nbt.putString("owner_name", ownerName);
		nbt.putLong("created_at", createdAt);
		nbt.putBoolean("open_source", openSource);

		NbtList makersNbt = new NbtList();
		for (Map.Entry<UUID, String> entry : makers.entrySet()) {
			NbtCompound maker = new NbtCompound();
			maker.putUuid("uuid", entry.getKey());
			maker.putString("name", entry.getValue());
			makersNbt.add(maker);
		}
		nbt.put("makers", makersNbt);
		return nbt;
	}

	public static PatentRecord fromNbt(NbtCompound nbt) {
		Map<UUID, String> makers = new LinkedHashMap<>();
		NbtList makersNbt = nbt.getList("makers", NbtCompound.COMPOUND_TYPE);
		for (int i = 0; i < makersNbt.size(); i++) {
			NbtCompound maker = makersNbt.getCompound(i);
			if (maker.containsUuid("uuid")) {
				makers.put(maker.getUuid("uuid"), maker.getString("name"));
			}
		}

		return new PatentRecord(
			nbt.getString("item_id"),
			nbt.getUuid("owner_uuid"),
			nbt.getString("owner_name"),
			nbt.getLong("created_at"),
			nbt.getBoolean("open_source"),
			makers
		);
	}
}
