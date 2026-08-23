package com.appiemon.wonso.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class MachineBlock extends Block {
	private final String machineId;

	public MachineBlock(Properties properties, String machineId) {
		super(properties);
		this.machineId = machineId;
	}

	public String machineId() {
		return machineId;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		return InteractionResult.SUCCESS;
	}
}
