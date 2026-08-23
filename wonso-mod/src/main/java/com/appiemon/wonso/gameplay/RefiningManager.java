package com.appiemon.wonso.gameplay;

import com.appiemon.wonso.data.WonsoData;
import com.appiemon.wonso.registry.WonsoItems;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public final class RefiningManager {
	private RefiningManager() {
	}

	public static void initialize() {
	}

	public static boolean crushHeld(ServerPlayer player) {
		for (var raw : WonsoData.crush()) {
			JsonObject o = raw.getAsJsonObject();
			JsonObject input = o.getAsJsonObject("input");
			if (!input.has("item")) {
				continue;
			}
			Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(input.get("item").getAsString()));
			int need = input.get("count").getAsInt();
			if (count(player, item) < need) {
				continue;
			}
			remove(player, item, need);
			for (var out : o.getAsJsonArray("outputs")) {
				JsonObject oo = out.getAsJsonObject();
				if (oo.has("chance") && ThreadLocalRandom.current().nextDouble() > oo.get("chance").getAsDouble()) {
					continue;
				}
				giveIntermediate(player, oo.get("id").getAsString(), oo.get("count").getAsInt());
			}
			player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("message.wonso.crushed"));
			return true;
		}
		return false;
	}

	public static boolean purifyToCard(ServerPlayer player, String symbol) {
		Item impure = WonsoItems.impure(symbol);
		if (impure == null || count(player, impure) < 2) {
			return false;
		}
		if (!ElementEnergyManager.spend(player, 5)) {
			return false;
		}
		remove(player, impure, 2);
		player.addItem(new ItemStack(WonsoItems.elementCard(symbol)));
		DiscoveryManager.discoverElement(player, symbol);
		return true;
	}

	private static int count(ServerPlayer player, Item item) {
		int n = 0;
		var inv = player.getInventory();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack s = inv.getItem(i);
			if (s.is(item)) {
				n += s.getCount();
			}
		}
		return n;
	}

	private static void remove(ServerPlayer player, Item item, int amount) {
		var inv = player.getInventory();
		int left = amount;
		for (int i = 0; i < inv.getContainerSize() && left > 0; i++) {
			ItemStack s = inv.getItem(i);
			if (!s.is(item)) {
				continue;
			}
			int take = Math.min(left, s.getCount());
			s.shrink(take);
			left -= take;
		}
	}

	private static void giveIntermediate(ServerPlayer player, String id, int count) {
		Item item = WonsoItems.intermediate(id);
		if (item != null) {
			player.addItem(new ItemStack(item, count));
		}
	}
}
