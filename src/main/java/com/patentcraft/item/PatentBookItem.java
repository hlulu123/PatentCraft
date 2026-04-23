package com.patentcraft.item;

import com.patentcraft.util.PatentBookUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class PatentBookItem extends Item {
	public PatentBookItem(Settings settings) {
		super(settings);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		String itemId = PatentBookUtil.getPatentItemId(stack);
		String owner = PatentBookUtil.getOwnerName(stack);
		if (itemId.isBlank()) {
			tooltip.add(Text.translatable("item.patentcraft.patent_book.empty").formatted(Formatting.GRAY));
			return;
		}

		tooltip.add(Text.literal(itemId).formatted(Formatting.GOLD));
		if (!owner.isBlank()) {
			tooltip.add(Text.literal(owner).formatted(Formatting.AQUA));
		}
	}
}
