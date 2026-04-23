package com.patentcraft;

import com.patentcraft.block.PatentStationBlock;
import com.patentcraft.item.PatentBookItem;
import com.patentcraft.recipe.PatentAuthorizeRecipe;
import com.patentcraft.screen.PatentLecternScreenHandler;
import com.patentcraft.screen.PatentStationScreenHandler;
import com.patentcraft.server.PatentConfig;
import com.patentcraft.server.PatentEvents;
import com.patentcraft.server.PatentNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class PatentCraft implements ModInitializer {
	public static final String MOD_ID = "patentcraft";
	private static final RegistryKey<ItemGroup> OPERATOR_GROUP = RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier.ofVanilla("op_blocks"));

	public static final Block PATENT_STATION = Registry.register(
		Registries.BLOCK,
		id("patent_station"),
		new PatentStationBlock(AbstractBlock.Settings.copy(Blocks.CRAFTING_TABLE).strength(3.0F))
	);

	public static final Item PATENT_BOOK = Registry.register(
		Registries.ITEM,
		id("patent_book"),
		new PatentBookItem(new Item.Settings().maxCount(1))
	);

	public static final Item PATENT_STATION_ITEM = Registry.register(
		Registries.ITEM,
		id("patent_station"),
		new BlockItem(PATENT_STATION, new Item.Settings())
	);

	public static final RecipeSerializer<PatentAuthorizeRecipe> AUTHORIZE_RECIPE_SERIALIZER = Registry.register(
		Registries.RECIPE_SERIALIZER,
		id("authorize"),
		new SpecialRecipeSerializer<>(PatentAuthorizeRecipe::new)
	);

	public static final ScreenHandlerType<PatentLecternScreenHandler> PATENT_LECTERN_SCREEN_HANDLER = Registry.register(
		Registries.SCREEN_HANDLER,
		id("patent_lectern"),
		new ScreenHandlerType<>(PatentLecternScreenHandler::new, FeatureSet.empty())
	);

	public static final ScreenHandlerType<PatentStationScreenHandler> PATENT_STATION_SCREEN_HANDLER = Registry.register(
		Registries.SCREEN_HANDLER,
		id("patent_station"),
		new ScreenHandlerType<>(PatentStationScreenHandler::new, FeatureSet.empty())
	);

	@Override
	public void onInitialize() {
		PatentConfig.load();
		PatentNetworking.registerServer();
		PatentEvents.register();
		ItemGroupEvents.modifyEntriesEvent(OPERATOR_GROUP).register(entries -> {
			entries.add(PATENT_STATION_ITEM);
			entries.add(PATENT_BOOK);
		});
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
