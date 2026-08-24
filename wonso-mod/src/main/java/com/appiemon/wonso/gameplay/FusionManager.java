package com.appiemon.wonso.gameplay;

import com.appiemon.wonso.data.WonsoData;
import com.appiemon.wonso.registry.WonsoItems;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public final class FusionManager {
	private FusionManager() {
	}

	public static boolean fuse(ServerPlayer player, String recipeId) {
		for (var raw : WonsoData.fusion()) {
			JsonObject o = raw.getAsJsonObject();
			if (!recipeId.equals(o.get("id").getAsString()) && !recipeId.equals(o.get("output").getAsString())) {
				continue;
			}
			Map<String, Integer> need = new HashMap<>();
			for (var in : o.getAsJsonArray("inputs")) {
				need.merge(in.getAsString(), 1, Integer::sum);
			}
			for (var e : need.entrySet()) {
				if (InventoryUtil.count(player, WonsoItems.elementCard(e.getKey())) < e.getValue()) {
					player.sendOverlayMessage(Component.translatable("message.wonso.fusion_missing"));
					return false;
				}
			}
			if (!ElementEnergyManager.spend(player, o.get("energyCost").getAsFloat())) {
				player.sendOverlayMessage(Component.translatable("message.wonso.need_energy"));
				return false;
			}
			for (var e : need.entrySet()) {
				InventoryUtil.remove(player, WonsoItems.elementCard(e.getKey()), e.getValue());
			}
			String out = o.get("output").getAsString();
			InventoryUtil.give(player, WonsoItems.elementCard(out), 1);
			DiscoveryManager.discoverElement(player, out);
			DiscoveryManager.grant(player, "fusion_intro");
			player.sendOverlayMessage(Component.translatable("message.wonso.fusion_ok", out));
			return true;
		}
		return false;
	}
}
