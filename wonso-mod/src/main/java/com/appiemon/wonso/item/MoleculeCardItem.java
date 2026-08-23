package com.appiemon.wonso.item;

import com.appiemon.wonso.data.MoleculeRecord;
import com.appiemon.wonso.gameplay.DiscoveryManager;
import com.appiemon.wonso.gameplay.ElementEnergyManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MoleculeCardItem extends Item {
	private final MoleculeRecord molecule;

	public MoleculeCardItem(Properties properties, MoleculeRecord molecule) {
		super(properties);
		this.molecule = molecule;
	}

	public MoleculeRecord molecule() {
		return molecule;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
			ItemStack stack = player.getItemInHand(hand);
			if (player.isShiftKeyDown()) {
				stack.shrink(1);
				ElementEnergyManager.convertMolecule(serverPlayer, molecule.id());
			} else {
				DiscoveryManager.discoverMolecule(player, molecule.id());
			}
		}
		return InteractionResult.SUCCESS;
	}
}
