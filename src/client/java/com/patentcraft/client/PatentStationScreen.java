package com.patentcraft.client;

import com.patentcraft.screen.PatentStationScreenHandler;
import com.patentcraft.server.PatentConfig;
import com.patentcraft.util.PatentBookUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class PatentStationScreen extends HandledScreen<PatentStationScreenHandler> {
	public PatentStationScreen(PatentStationScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		backgroundWidth = 176;
		backgroundHeight = 166;
	}

	@Override
	protected void init() {
		super.init();
		addDrawableChild(ButtonWidget.builder(Text.translatable("patentcraft.screen.add_whitelist"), button -> click(PatentStationScreenHandler.ADD_BUTTON))
			.dimensions(x + 104, y + 28, 58, 20)
			.build());
		addDrawableChild(ButtonWidget.builder(Text.translatable("patentcraft.screen.remove_whitelist"), button -> click(PatentStationScreenHandler.REMOVE_BUTTON))
			.dimensions(x + 104, y + 52, 58, 20)
			.build());
		addDrawableChild(ButtonWidget.builder(Text.translatable("patentcraft.screen.open_source"), button -> click(PatentStationScreenHandler.OPEN_SOURCE_BUTTON))
			.dimensions(x + 42, y + 55, 58, 20)
			.build());
	}

	@Override
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
		context.fill(x, y, x + backgroundWidth, y + backgroundHeight, 0xFF2F332E);
		context.fill(x + 8, y + 18, x + 168, y + 78, 0xFFCBD6C2);
		context.fill(x + 8, y + 80, x + 168, y + 158, 0xFFB5BFAE);
		context.drawBorder(x, y, backgroundWidth, backgroundHeight, 0xFF5A6655);
		context.drawBorder(x + 79, y + 34, 18, 18, 0xFF46513F);
	}

	@Override
	protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
		context.drawText(textRenderer, title, 8, 6, 0xFFE8F2DD, false);
		context.drawText(textRenderer, playerInventoryTitle, 8, 72, 0xFF2F332E, false);

		ItemStack stack = handler.getSlot(0).getStack();
		if (stack.isEmpty()) {
			context.drawText(textRenderer, Text.translatable("patentcraft.screen.station_hint").formatted(Formatting.GRAY), 16, 36, 0xFF4A5542, false);
			return;
		}

		String itemId = PatentBookUtil.getItemId(stack);
		Text state = PatentConfig.isProtected(itemId)
			? (PatentConfig.isOpenSource(itemId)
				? Text.translatable("patentcraft.screen.open_source").formatted(Formatting.GOLD)
				: Text.translatable("patentcraft.screen.in_whitelist").formatted(Formatting.GREEN))
			: Text.translatable("patentcraft.screen.not_in_whitelist").formatted(Formatting.RED);
		context.drawTextWrapped(textRenderer, stack.getName().copy().formatted(Formatting.WHITE), 16, 22, 84, 0xFF2F332E);
		context.drawText(textRenderer, state, 16, 56, 0xFF2F332E, false);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context, mouseX, mouseY, delta);
		super.render(context, mouseX, mouseY, delta);
		drawMouseoverTooltip(context, mouseX, mouseY);
	}

	private void click(int id) {
		if (client != null && client.interactionManager != null) {
			client.interactionManager.clickButton(handler.syncId, id);
		}
	}
}
