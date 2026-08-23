package com.appiemon.wonso.gameplay;

import com.appiemon.wonso.data.WonsoData;
import com.appiemon.wonso.registry.WonsoItems;
import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public final class SynthesisRecipeManager {
	private SynthesisRecipeManager() {
	}

	public static void initialize() {
	}

	public static boolean tryCraft(ServerPlayer player, String moleculeId) {
		var mol = WonsoData.molecule(moleculeId);
		if (mol == null) {
			return false;
		}
		Map<String, Integer> need = new HashMap<>();
		for (var part : mol.elements()) {
			need.merge(part.symbol(), part.count(), Integer::sum);
		}
		if (!consumeCards(player, need, true)) {
			ElementEnergyManager.addEnergy(player, 1);
			player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("message.wonso.reaction_fail"));
			return false;
		}
		consumeCards(player, need, false);
		player.addItem(new ItemStack(WonsoItems.moleculeCard(mol.id())));
		DiscoveryManager.discoverMolecule(player, mol.id());
		player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("message.wonso.reaction_ok", mol.formulaDisplay()));
		return true;
	}

	private static boolean consumeCards(ServerPlayer player, Map<String, Integer> need, boolean simulate) {
		Map<String, Integer> remaining = new HashMap<>(need);
		var inv = player.getInventory();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack stack = inv.getItem(i);
			String symbol = WonsoItems.symbolOfCard(stack);
			if (symbol == null || !remaining.containsKey(symbol)) {
				continue;
			}
			int take = Math.min(stack.getCount(), remaining.get(symbol));
			if (!simulate) {
				stack.shrink(take);
			}
			remaining.compute(symbol, (k, v) -> v - take);
		}
		return remaining.values().stream().allMatch(v -> v <= 0);
	}
}
