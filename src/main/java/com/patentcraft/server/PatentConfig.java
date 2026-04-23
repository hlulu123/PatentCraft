package com.patentcraft.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public final class PatentConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static PatentConfigData data;
	private static Path configPath;
	private static Set<String> openSourceItems = new LinkedHashSet<>();

	private PatentConfig() {
	}

	public static void load() {
		configPath = FabricLoader.getInstance().getConfigDir().resolve("patentcraft.json");
		if (Files.notExists(configPath)) {
			data = PatentConfigData.defaults();
			save();
			return;
		}

		try (Reader reader = Files.newBufferedReader(configPath)) {
			data = GSON.fromJson(reader, PatentConfigData.class);
			if (data == null || data.protectedItems == null) {
				data = PatentConfigData.defaults();
			}
		} catch (IOException exception) {
			data = PatentConfigData.defaults();
		}
	}

	public static boolean isProtected(String itemId) {
		return data != null && data.protectedItems.contains(itemId);
	}

	public static boolean isOpenSource(String itemId) {
		return openSourceItems.contains(itemId);
	}

	public static Set<String> protectedItems() {
		return data == null ? Set.of() : Set.copyOf(data.protectedItems);
	}

	public static void replaceProtectedItems(Collection<String> itemIds) {
		if (data == null) {
			data = new PatentConfigData();
		}
		data.protectedItems = new LinkedHashSet<>(itemIds);
	}

	public static void replaceOpenSourceItems(Collection<String> itemIds) {
		openSourceItems = new LinkedHashSet<>(itemIds);
	}

	public static boolean addProtectedItem(String itemId) {
		if (data == null) {
			data = PatentConfigData.defaults();
		}
		boolean changed = data.protectedItems.add(itemId);
		if (changed) {
			save();
		}
		return changed;
	}

	public static boolean removeProtectedItem(String itemId) {
		if (data == null) {
			data = PatentConfigData.defaults();
		}
		boolean changed = data.protectedItems.remove(itemId);
		if (changed) {
			save();
		}
		return changed;
	}

	public static void save() {
		if (configPath == null) {
			configPath = FabricLoader.getInstance().getConfigDir().resolve("patentcraft.json");
		}
		try {
			Files.createDirectories(configPath.getParent());
			try (Writer writer = Files.newBufferedWriter(configPath)) {
				GSON.toJson(data, writer);
			}
		} catch (IOException ignored) {
		}
	}

	private static class PatentConfigData {
		Set<String> protectedItems = new LinkedHashSet<>();

		static PatentConfigData defaults() {
			PatentConfigData data = new PatentConfigData();
			data.protectedItems.add("minecraft:diamond_sword");
			data.protectedItems.add("minecraft:diamond_pickaxe");
			data.protectedItems.add("minecraft:diamond_axe");
			data.protectedItems.add("minecraft:diamond_shovel");
			data.protectedItems.add("minecraft:diamond_hoe");
			data.protectedItems.add("minecraft:diamond_helmet");
			data.protectedItems.add("minecraft:diamond_chestplate");
			data.protectedItems.add("minecraft:diamond_leggings");
			data.protectedItems.add("minecraft:diamond_boots");
			data.protectedItems.add("minecraft:netherite_sword");
			data.protectedItems.add("minecraft:netherite_pickaxe");
			data.protectedItems.add("minecraft:netherite_axe");
			data.protectedItems.add("minecraft:netherite_shovel");
			data.protectedItems.add("minecraft:netherite_hoe");
			data.protectedItems.add("minecraft:netherite_helmet");
			data.protectedItems.add("minecraft:netherite_chestplate");
			data.protectedItems.add("minecraft:netherite_leggings");
			data.protectedItems.add("minecraft:netherite_boots");
			data.protectedItems.add("minecraft:mace");
			return data;
		}
	}
}
