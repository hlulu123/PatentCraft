package com.patentcraft.client;

import com.patentcraft.PatentCraft;
import com.patentcraft.network.PatentListPayload;
import com.patentcraft.network.ProfileResultPayload;
import com.patentcraft.network.StatusPayload;
import com.patentcraft.network.WhitelistPayload;
import com.patentcraft.screen.PatentLecternScreenHandler;
import com.patentcraft.server.PatentConfig;
import com.patentcraft.util.PatentBookUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class PatentCraftClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		HandledScreens.register(PatentCraft.PATENT_LECTERN_SCREEN_HANDLER, PatentLecternScreen::new);
		HandledScreens.register(PatentCraft.PATENT_STATION_SCREEN_HANDLER, PatentStationScreen::new);

		ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
			String itemId = PatentBookUtil.getItemId(stack);
			if (PatentConfig.isProtected(itemId) && !PatentBookUtil.isPatentBook(stack)) {
				if (PatentConfig.isOpenSource(itemId)) {
					lines.add(Text.translatable("patentcraft.tooltip.open_source").formatted(Formatting.GOLD));
				} else if (!PatentBookUtil.isAuthorizedFor(stack, itemId)) {
					lines.add(Text.translatable("patentcraft.tooltip.unauthorized").formatted(Formatting.RED));
				}
			}
		});

		ClientPlayNetworking.registerGlobalReceiver(PatentListPayload.ID, (payload, context) ->
			context.client().execute(() -> PatentLecternScreen.acceptPatents(payload.patents()))
		);
		ClientPlayNetworking.registerGlobalReceiver(ProfileResultPayload.ID, (payload, context) ->
			context.client().execute(() -> PatentLecternScreen.acceptProfile(payload))
		);
		ClientPlayNetworking.registerGlobalReceiver(StatusPayload.ID, (payload, context) ->
			context.client().execute(() -> {
				if (context.client().player != null) {
					if (payload.arg().isBlank()) {
						context.client().player.sendMessage(Text.translatable(payload.translationKey()), true);
					} else {
						context.client().player.sendMessage(Text.translatable(payload.translationKey(), payload.arg()), true);
					}
				}
			})
		);
		ClientPlayNetworking.registerGlobalReceiver(WhitelistPayload.ID, (payload, context) ->
			context.client().execute(() -> {
				PatentConfig.replaceProtectedItems(payload.itemIds());
				PatentConfig.replaceOpenSourceItems(payload.openSourceItemIds());
			})
		);
	}
}
