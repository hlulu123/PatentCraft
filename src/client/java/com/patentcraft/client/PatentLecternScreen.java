package com.patentcraft.client;

import com.patentcraft.network.PatentActionPayload;
import com.patentcraft.network.PatentInfo;
import com.patentcraft.network.ProfileResultPayload;
import com.patentcraft.screen.PatentLecternScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PatentLecternScreen extends HandledScreen<PatentLecternScreenHandler> {
	private static final int VISIBLE_PATENT_ROWS = 6;
	private static PatentLecternScreen active;
	private static List<PatentInfo> pendingPatents = new ArrayList<>();

	private final List<ButtonWidget> patentButtons = new ArrayList<>();
	private List<PatentInfo> patents = new ArrayList<>();
	private PatentInfo selected;
	private TextFieldWidget nameField;
	private ButtonWidget lookupButton;
	private ButtonWidget makeBookButton;
	private ButtonWidget grantButton;
	private ButtonWidget revokeButton;
	private ButtonWidget openSourceButton;
	private ProfileResultPayload profile;
	private int scrollOffset;

	public PatentLecternScreen(PatentLecternScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		backgroundWidth = 248;
		backgroundHeight = 208;
	}

	public static void acceptPatents(List<PatentInfo> patents) {
		pendingPatents = new ArrayList<>(patents);
		if (active != null) {
			active.setPatents(patents);
		}
	}

	public static void acceptProfile(ProfileResultPayload profile) {
		if (active != null) {
			active.profile = profile;
			active.refreshButtons();
		}
	}

	@Override
	protected void init() {
		super.init();
		active = this;
		nameField = new TextFieldWidget(textRenderer, x + 134, y + 82, 92, 18, Text.literal("Player"));
		nameField.setMaxLength(16);
		addDrawableChild(nameField);

		lookupButton = ButtonWidget.builder(Text.translatable("patentcraft.screen.lookup"), button ->
			ClientPlayNetworking.send(new PatentActionPayload(PatentActionPayload.Action.LOOKUP, selectedItemId(), nameField.getText()))
		).dimensions(x + 134, y + 104, 92, 20).build();
		addDrawableChild(lookupButton);

		makeBookButton = ButtonWidget.builder(Text.translatable("patentcraft.screen.make_book"), button ->
			ClientPlayNetworking.send(new PatentActionPayload(PatentActionPayload.Action.MAKE_BOOK, selectedItemId(), ""))
		).dimensions(x + 12, y + 178, 108, 20).build();
		addDrawableChild(makeBookButton);

		openSourceButton = ButtonWidget.builder(Text.translatable("patentcraft.screen.open_source"), button ->
			ClientPlayNetworking.send(new PatentActionPayload(PatentActionPayload.Action.OPEN_SOURCE, selectedItemId(), ""))
		).dimensions(x + 12, y + 154, 108, 20).build();
		addDrawableChild(openSourceButton);

		grantButton = ButtonWidget.builder(Text.translatable("patentcraft.screen.grant"), button ->
			ClientPlayNetworking.send(new PatentActionPayload(PatentActionPayload.Action.GRANT, selectedItemId(), nameField.getText()))
		).dimensions(x + 134, y + 132, 92, 20).build();
		addDrawableChild(grantButton);

		revokeButton = ButtonWidget.builder(Text.translatable("patentcraft.screen.revoke"), button ->
			ClientPlayNetworking.send(new PatentActionPayload(PatentActionPayload.Action.REVOKE, selectedItemId(), nameField.getText()))
		).dimensions(x + 134, y + 154, 92, 20).build();
		addDrawableChild(revokeButton);

		setPatents(pendingPatents);
	}

	@Override
	public void close() {
		super.close();
		active = null;
	}

	@Override
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
		context.fill(x, y, x + backgroundWidth, y + backgroundHeight, 0xFF3B3027);
		context.fill(x + 6, y + 18, x + 126, y + 148, 0xFFE2CFA5);
		context.fill(x + 128, y + 18, x + 238, y + 176, 0xFFCCB98F);
		context.drawBorder(x, y, backgroundWidth, backgroundHeight, 0xFF6A5138);
		context.drawBorder(x + 6, y + 18, 120, 130, 0xFF7C6240);
		context.drawBorder(x + 128, y + 18, 110, 158, 0xFF7C6240);
	}

	@Override
	protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
		context.drawText(textRenderer, title, 8, 6, 0xFFF5E6BD, false);
		if (patents.isEmpty()) {
			context.drawTextWrapped(textRenderer, Text.translatable("patentcraft.screen.no_patents").formatted(Formatting.GRAY), 14, 44, 100, 0xFF5B4630);
		}
		context.drawText(textRenderer, Text.translatable("patentcraft.screen.owner_tools"), 134, 26, 0xFF3F2F1C, false);
		if (selected != null) {
			ItemStack stack = stackFor(selected.itemId());
			context.drawItem(stack, 134, 43);
			context.drawTextWrapped(textRenderer, stack.getName(), 154, 46, 74, 0xFF3F2F1C);
			if (selected.openSource()) {
				context.drawText(textRenderer, Text.translatable("patentcraft.screen.open_source").formatted(Formatting.GOLD), 154, 64, 0xFF3F2F1C, false);
			}
		}
		if (profile != null && profile.found()) {
			drawProfile(context, 134, 62, profile.uuid(), profile.name());
		}

		for (int i = 0; i < visiblePatentRows(); i++) {
			PatentInfo info = patents.get(scrollOffset + i);
			int rowY = 27 + i * 20;
			ItemStack stack = stackFor(info.itemId());
			context.drawItem(stack, 14, rowY);
			String name = textRenderer.trimToWidth(stack.getName().getString(), 76);
			int color = info.equals(selected) ? 0xFF164B1E : 0xFF3F2F1C;
			context.drawText(textRenderer, name, 34, rowY + 5, color, false);
		}
		if (patents.size() > VISIBLE_PATENT_ROWS) {
			int maxOffset = maxScrollOffset();
			int trackTop = 26;
			int trackHeight = 118;
			int thumbHeight = Math.max(16, trackHeight * VISIBLE_PATENT_ROWS / patents.size());
			int thumbTop = trackTop + (trackHeight - thumbHeight) * scrollOffset / maxOffset;
			context.fill(119, trackTop, 122, trackTop + trackHeight, 0xFF6F5B42);
			context.fill(119, thumbTop, 122, thumbTop + thumbHeight, 0xFFF5E6BD);
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context, mouseX, mouseY, delta);
		super.render(context, mouseX, mouseY, delta);
		drawMouseoverTooltip(context, mouseX, mouseY);
	}

	private void setPatents(List<PatentInfo> patents) {
		this.patents = new ArrayList<>(patents);
		clampScrollOffset();
		if (selected == null && !this.patents.isEmpty()) {
			selected = this.patents.getFirst();
		}
		refreshPatentButtons();
		refreshButtons();
	}

	private void refreshPatentButtons() {
		for (ButtonWidget button : patentButtons) {
			remove(button);
		}
		patentButtons.clear();

		int top = y + 26;
		for (int i = 0; i < visiblePatentRows(); i++) {
			PatentInfo info = patents.get(scrollOffset + i);
			ButtonWidget button = ButtonWidget.builder(Text.empty(), b -> {
				selected = info;
				profile = null;
				refreshButtons();
			}).dimensions(x + 12, top + i * 20, 108, 18).build();
			patentButtons.add(button);
			addDrawableChild(button);
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (patents.size() <= VISIBLE_PATENT_ROWS || mouseX < x + 6 || mouseX > x + 126 || mouseY < y + 18 || mouseY > y + 148) {
			return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
		}
		int oldOffset = scrollOffset;
		scrollOffset -= (int) Math.signum(verticalAmount);
		clampScrollOffset();
		if (scrollOffset != oldOffset) {
			refreshPatentButtons();
		}
		return true;
	}

	private void refreshButtons() {
		boolean hasSelection = selected != null;
		boolean owner = hasSelection && selected.owner();
		boolean hasName = nameField != null && !nameField.getText().isBlank();
		makeBookButton.active = hasSelection;
		openSourceButton.active = owner && !selected.openSource();
		lookupButton.active = owner && hasName;
		grantButton.active = owner && hasName;
		revokeButton.active = owner && hasName;
		nameField.active = owner;
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		boolean result = super.charTyped(chr, modifiers);
		refreshButtons();
		return result;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		boolean result = super.keyPressed(keyCode, scanCode, modifiers);
		refreshButtons();
		return result;
	}

	private String selectedItemId() {
		return selected == null ? "" : selected.itemId();
	}

	private int visiblePatentRows() {
		return Math.min(VISIBLE_PATENT_ROWS, Math.max(0, patents.size() - scrollOffset));
	}

	private int maxScrollOffset() {
		return Math.max(0, patents.size() - VISIBLE_PATENT_ROWS);
	}

	private void clampScrollOffset() {
		scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset()));
	}

	private static ItemStack stackFor(String itemId) {
		Identifier id = Identifier.tryParse(itemId);
		if (id == null) {
			return new ItemStack(Items.BARRIER);
		}
		Item item = Registries.ITEM.get(id);
		return item == Items.AIR ? new ItemStack(Items.BARRIER) : new ItemStack(item);
	}

	private void drawProfile(DrawContext context, int left, int top, UUID uuid, String name) {
		Identifier texture = DefaultSkinHelper.getSkinTextures(uuid).texture();
		context.drawTexture(texture, left, top, 8, 8, 16, 16, 64, 64);
		context.drawText(textRenderer, name, left + 22, top + 5, 0xFF3F2F1C, false);
	}
}
