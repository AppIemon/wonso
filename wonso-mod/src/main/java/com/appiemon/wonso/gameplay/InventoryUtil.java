package com.appiemon.wonso.gameplay;

import com.appiemon.wonso.WonsoMod;
import com.appiemon.wonso.registry.WonsoItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class InventoryUtil {
	private InventoryUtil() {
	}

	public static Item resolveId(String id) {
		if (id == null || id.isBlank()) {
			return null;
		}
		if (id.contains(":")) {
			return BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
		}
		if ("refining_catalyst".equals(id)) {
			return WonsoItems.REFINING_CATALYST;
		}
		if ("stellar_core".equals(id)) {
			return WonsoItems.STELLAR_CORE;
		}
		if ("neutron_star_fragment".equals(id)) {
			return WonsoItems.NEUTRON_STAR_FRAGMENT;
		}
		if ("black_hole_essence".equals(id)) {
			return WonsoItems.BLACK_HOLE_ESSENCE;
		}
		if (id.startsWith("impure_")) {
			return WonsoItems.impure(normalizeSymbol(id.substring("impure_".length())));
		}
		Item intermediate = WonsoItems.intermediate(id);
		if (intermediate != null) {
			return intermediate;
		}
		Item lower = WonsoItems.intermediate(id.toLowerCase());
		if (lower != null) {
			return lower;
		}
		return BuiltInRegistries.ITEM.getValue(WonsoMod.id(id.toLowerCase()));
	}

	public static String normalizeSymbol(String raw) {
		if (raw == null || raw.isEmpty()) {
			return raw;
		}
		if (raw.length() == 1) {
			return raw.toUpperCase();
		}
		return Character.toUpperCase(raw.charAt(0)) + raw.substring(1).toLowerCase();
	}

	public static int count(ServerPlayer player, Item item) {
		if (item == null) {
			return 0;
		}
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

	public static void remove(ServerPlayer player, Item item, int amount) {
		if (item == null || amount <= 0) {
			return;
		}
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
		if (item == Items.WATER_BUCKET) {
			player.addItem(new ItemStack(Items.BUCKET, amount));
		}
	}

	public static void give(ServerPlayer player, Item item, int count) {
		if (item == null || count <= 0) {
			return;
		}
		player.addItem(new ItemStack(item, count));
	}

	public static boolean consumeCards(ServerPlayer player, String symbol, int amount) {
		Item card = WonsoItems.elementCard(symbol);
		if (card == null || count(player, card) < amount) {
			return false;
		}
		remove(player, card, amount);
		return true;
	}
}
