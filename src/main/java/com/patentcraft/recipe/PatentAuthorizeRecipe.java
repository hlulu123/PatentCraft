package com.patentcraft.recipe;

import com.patentcraft.PatentCraft;
import com.patentcraft.server.PatentConfig;
import com.patentcraft.server.PatentRecord;
import com.patentcraft.server.PatentState;
import com.patentcraft.util.PatentBookUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.util.Optional;

public class PatentAuthorizeRecipe extends SpecialCraftingRecipe {
	public PatentAuthorizeRecipe(CraftingRecipeCategory category) {
		super(category);
	}

	@Override
	public boolean matches(CraftingRecipeInput input, World world) {
		return find(input, world).valid();
	}

	@Override
	public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
		RecipeMatch match = find(input, null);
		if (!match.valid()) {
			return ItemStack.EMPTY;
		}
		ItemStack copy = match.item().copyWithCount(1);
		PatentBookUtil.authorizeItem(copy, match.record());
		return copy;
	}

	@Override
	public boolean fits(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return PatentCraft.AUTHORIZE_RECIPE_SERIALIZER;
	}

	private RecipeMatch find(CraftingRecipeInput input, World world) {
		ItemStack protectedItem = ItemStack.EMPTY;
		ItemStack patentBook = ItemStack.EMPTY;
		int nonEmpty = 0;

		for (int i = 0; i < input.getSize(); i++) {
			ItemStack stack = input.getStackInSlot(i);
			if (stack.isEmpty()) {
				continue;
			}
			nonEmpty++;
			if (stack.isOf(PatentCraft.PATENT_BOOK)) {
				if (!patentBook.isEmpty()) {
					return RecipeMatch.invalid();
				}
				patentBook = stack;
			} else {
				if (!protectedItem.isEmpty()) {
					return RecipeMatch.invalid();
				}
				protectedItem = stack;
			}
		}

		if (nonEmpty != 2 || protectedItem.isEmpty() || patentBook.isEmpty()) {
			return RecipeMatch.invalid();
		}

		String itemId = PatentBookUtil.getItemId(protectedItem);
		if (!PatentConfig.isProtected(itemId) || !PatentBookUtil.matchesPatent(patentBook, itemId)) {
			return RecipeMatch.invalid();
		}

		PatentRecord record = null;
		if (world instanceof ServerWorld serverWorld) {
			Optional<PatentRecord> patent = PatentState.get(serverWorld.getServer()).getPatent(itemId);
			if (patent.isPresent()) {
				record = patent.get();
			}
		}
		if (record == null) {
			record = new PatentRecord(itemId, PatentBookUtil.getOwnerUuid(patentBook), PatentBookUtil.getOwnerName(patentBook), 0L);
		}
		return new RecipeMatch(protectedItem, record);
	}

	private record RecipeMatch(ItemStack item, PatentRecord record) {
		private static RecipeMatch invalid() {
			return new RecipeMatch(ItemStack.EMPTY, null);
		}

		private boolean valid() {
			return !item.isEmpty() && record != null;
		}
	}
}
