package com.patentcraft.server;

import com.patentcraft.PatentCraft;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PatentState extends PersistentState {
	private static final Type<PatentState> TYPE = new Type<>(PatentState::new, PatentState::fromNbt, null);
	private final Map<String, PatentRecord> patents = new LinkedHashMap<>();

	public static PatentState get(MinecraftServer server) {
		PersistentStateManager manager = server.getOverworld().getPersistentStateManager();
		return manager.getOrCreate(TYPE, PatentCraft.MOD_ID);
	}

	public Optional<PatentRecord> getPatent(String itemId) {
		return Optional.ofNullable(patents.get(itemId));
	}

	public Collection<PatentRecord> allPatents() {
		return patents.values();
	}

	public PatentRecord createPatent(String itemId, UUID ownerUuid, String ownerName) {
		PatentRecord record = new PatentRecord(itemId, ownerUuid, ownerName, System.currentTimeMillis());
		patents.put(itemId, record);
		markDirty();
		return record;
	}

	public List<PatentRecord> listMakeable(UUID playerUuid) {
		List<PatentRecord> records = new ArrayList<>();
		for (PatentRecord record : patents.values()) {
			if (record.canMakeBook(playerUuid)) {
				records.add(record);
			}
		}
		return records;
	}

	public boolean grantMaker(String itemId, UUID ownerUuid, UUID targetUuid, String targetName) {
		PatentRecord record = patents.get(itemId);
		if (record == null || !record.isOwner(ownerUuid)) {
			return false;
		}
		record.makers().put(targetUuid, targetName);
		markDirty();
		return true;
	}

	public boolean revokeMaker(String itemId, UUID ownerUuid, UUID targetUuid) {
		PatentRecord record = patents.get(itemId);
		if (record == null || !record.isOwner(ownerUuid)) {
			return false;
		}
		record.makers().remove(targetUuid);
		markDirty();
		return true;
	}

	public boolean openSourcePatent(String itemId, UUID ownerUuid) {
		PatentRecord record = patents.get(itemId);
		if (record == null || !record.isOwner(ownerUuid)) {
			return false;
		}
		patents.put(itemId, new PatentRecord(record.itemId(), record.ownerUuid(), record.ownerName(), record.createdAt(), true, record.makers()));
		markDirty();
		return true;
	}

	public void forceOpenSource(String itemId, UUID actorUuid, String actorName) {
		PatentRecord record = patents.get(itemId);
		if (record == null) {
			record = new PatentRecord(itemId, actorUuid, actorName, System.currentTimeMillis(), true, new LinkedHashMap<>());
		} else {
			record = new PatentRecord(record.itemId(), record.ownerUuid(), record.ownerName(), record.createdAt(), true, record.makers());
		}
		patents.put(itemId, record);
		markDirty();
	}

	public boolean isOpenSource(String itemId) {
		PatentRecord record = patents.get(itemId);
		return record != null && record.openSource();
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		NbtList list = new NbtList();
		for (PatentRecord record : patents.values()) {
			list.add(record.toNbt());
		}
		nbt.put("patents", list);
		return nbt;
	}

	private static PatentState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		PatentState state = new PatentState();
		NbtList list = nbt.getList("patents", NbtCompound.COMPOUND_TYPE);
		for (int i = 0; i < list.size(); i++) {
			PatentRecord record = PatentRecord.fromNbt(list.getCompound(i));
			state.patents.put(record.itemId(), record);
		}
		return state;
	}
}
