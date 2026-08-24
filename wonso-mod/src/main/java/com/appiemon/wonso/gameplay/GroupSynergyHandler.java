package com.appiemon.wonso.gameplay;

import com.appiemon.wonso.data.WonsoData;
import com.appiemon.wonso.registry.WonsoItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public final class GroupSynergyHandler {
	private GroupSynergyHandler() {
	}

	public static void initialize() {
	}

	public static boolean check(ServerPlayer player) {
		Map<String, Integer> counts = new HashMap<>();
		var inv = player.getInventory();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack stack = inv.getItem(i);
			String symbol = WonsoItems.symbolOfCard(stack);
			if (symbol == null) {
				continue;
			}
			var el = WonsoData.bySymbol(symbol);
			if (el != null) {
				counts.merge(el.category(), stack.getCount(), Integer::sum);
			}
		}
		for (var raw : WonsoData.synergy()) {
			var o = raw.getAsJsonObject();
			String cat = o.get("category").getAsString();
			int min = o.get("minCards").getAsInt();
			if (counts.getOrDefault(cat, 0) >= min) {
				DiscoveryManager.markSynergy(player);
				return true;
			}
		}
		return false;
	}
}
