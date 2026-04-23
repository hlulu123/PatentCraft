package com.patentcraft.block;

import com.mojang.serialization.MapCodec;
import com.patentcraft.screen.PatentStationScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PatentStationBlock extends Block {
	public static final MapCodec<PatentStationBlock> CODEC = createCodec(PatentStationBlock::new);

	public PatentStationBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected MapCodec<? extends Block> getCodec() {
		return CODEC;
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (!world.isClient) {
			player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
				(syncId, inventory, p) -> new PatentStationScreenHandler(syncId, inventory),
				Text.translatable("block.patentcraft.patent_station")
			));
		}
		return ActionResult.SUCCESS;
	}
}
