package com.appiemon.wonso.item;

import com.appiemon.wonso.gameplay.DiscoveryManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class ChemistryJournalItem extends Item {
	public ChemistryJournalItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!level.isClientSide()) {
			DiscoveryManager.markTableOpened(player);
		}
		return InteractionResult.SUCCESS;
	}
}
