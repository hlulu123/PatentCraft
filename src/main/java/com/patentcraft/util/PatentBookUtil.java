package com.patentcraft.util;

import com.patentcraft.PatentCraft;
import com.patentcraft.server.PatentRecord;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.UUID;
import java.util.function.Consumer;

public final class PatentBookUtil {
	private static final String ROOT = PatentCraft.MOD_ID;
	private static final String TYPE = "type";
	private static final String PATENT_BOOK = "patent_book";
	private static final String AUTHORIZED_ITEM = "authorized_item";
	private static final String ITEM_ID = "item_id";
	private static final String OWNER_UUID = "owner_uuid";
	private static final String OWNER_NAME = "owner_name";

	private PatentBookUtil() {
	}

	public static ItemStack createPatentBook(PatentRecord record) {
		ItemStack stack = new ItemStack(PatentCraft.PATENT_BOOK);
		setRoot(stack, data -> {
			data.putString(TYPE, PATENT_BOOK);
			data.putString(ITEM_ID, record.itemId());
			data.putUuid(OWNER_UUID, record.ownerUuid());
			data.putString(OWNER_NAME, record.ownerName());
		});
		Identifier id = Identifier.tryParse(record.itemId());
		if (id != null) {
			stack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("item.patentcraft.patent_book.bound", Text.translatable(Registries.ITEM.get(id).getTranslationKey())));
		}
		return stack;
	}

	public static boolean isPatentBook(ItemStack stack) {
		return stack.isOf(PatentCraft.PATENT_BOOK) && PATENT_BOOK.equals(getRoot(stack).getString(TYPE));
	}

	public static String getPatentItemId(ItemStack stack) {
		return getRoot(stack).getString(ITEM_ID);
	}

	public static UUID getOwnerUuid(ItemStack stack) {
		NbtCompound data = getRoot(stack);
		return data.containsUuid(OWNER_UUID) ? data.getUuid(OWNER_UUID) : null;
	}

	public static String getOwnerName(ItemStack stack) {
		return getRoot(stack).getString(OWNER_NAME);
	}

	public static boolean matchesPatent(ItemStack patentBook, String itemId) {
		return isPatentBook(patentBook) && getPatentItemId(patentBook).equals(itemId);
	}

	public static void authorizeItem(ItemStack stack, PatentRecord record) {
		setRoot(stack, data -> {
			data.putString(TYPE, AUTHORIZED_ITEM);
			data.putString(ITEM_ID, record.itemId());
			data.putUuid(OWNER_UUID, record.ownerUuid());
			data.putString(OWNER_NAME, record.ownerName());
		});
	}

	public static boolean isAuthorizedFor(ItemStack stack, String itemId) {
		NbtCompound data = getRoot(stack);
		return AUTHORIZED_ITEM.equals(data.getString(TYPE)) && itemId.equals(data.getString(ITEM_ID));
	}

	public static String getItemId(ItemStack stack) {
		Identifier id = Registries.ITEM.getId(stack.getItem());
		return id.toString();
	}

	private static NbtCompound getRoot(ItemStack stack) {
		NbtComponent component = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
		NbtCompound root = component.copyNbt();
		return root.getCompound(ROOT);
	}

	private static void setRoot(ItemStack stack, Consumer<NbtCompound> writer) {
		NbtComponent component = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
		NbtCompound root = component.copyNbt();
		NbtCompound data = root.getCompound(ROOT);
		writer.accept(data);
		root.put(ROOT, data);
		stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(root));
	}
}
