package com.appiemon.wonso.gameplay;

import com.appiemon.wonso.data.WonsoData;
import com.appiemon.wonso.data.WonsoPlayerState;
import com.appiemon.wonso.registry.WonsoItems;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

import java.util.concurrent.ThreadLocalRandom;

public final class RefiningManager {
	private RefiningManager() {
	}

	public static void initialize() {
	}

	public static boolean handle(ServerPlayer player, String action) {
		if (action == null || action.isBlank()) {
			return false;
		}
		if (action.startsWith("recipe:")) {
			return runById(player, action.substring("recipe:".length()));
		}
		if (action.startsWith("purify:")) {
			return purifyToCard(player, action.substring("purify:".length()));
		}
		return switch (action) {
			case "crush" -> runFirst(player, WonsoData.crush(), "crush");
			case "dissolve" -> runFirst(player, WonsoData.dissolve(), "dissolve");
			case "crude" -> runFirst(player, WonsoData.crude(), "crude");
			case "purify" -> runFirst(player, WonsoData.purify(), "purify");
			default -> false;
		};
	}

	public static boolean crushHeld(ServerPlayer player) {
		return handle(player, "crush");
	}

	public static boolean purifyToCard(ServerPlayer player, String symbol) {
		Item impure = WonsoItems.impure(symbol);
		if (impure == null || InventoryUtil.count(player, impure) < 2) {
			return false;
		}
		if (!ElementEnergyManager.spend(player, 5)) {
			player.sendOverlayMessage(Component.translatable("message.wonso.need_energy"));
			return false;
		}
		InventoryUtil.remove(player, impure, 2);
		InventoryUtil.give(player, WonsoItems.elementCard(symbol), 1);
		DiscoveryManager.discoverElement(player, symbol);
		noteRefine(player);
		if ("Si".equals(symbol)) {
			DiscoveryManager.grant(player, "first_card_from_sand");
		}
		player.sendOverlayMessage(Component.translatable("message.wonso.purified", symbol));
		return true;
	}

	private static boolean runById(ServerPlayer player, String id) {
		for (JsonArray arr : new JsonArray[]{WonsoData.crush(), WonsoData.dissolve(), WonsoData.crude(), WonsoData.purify()}) {
			for (JsonElement raw : arr) {
				JsonObject o = raw.getAsJsonObject();
				if (id.equals(o.get("id").getAsString()) && tryRun(player, o)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean runFirst(ServerPlayer player, JsonArray recipes, String stage) {
		for (JsonElement raw : recipes) {
			if (tryRun(player, raw.getAsJsonObject())) {
				return true;
			}
		}
		player.sendOverlayMessage(Component.translatable("message.wonso.refine_missing", stage));
		return false;
	}

	private static boolean tryRun(ServerPlayer player, JsonObject recipe) {
		if (!canPay(player, recipe)) {
			return false;
		}
		if (recipe.has("energy") && !ElementEnergyManager.spend(player, recipe.get("energy").getAsFloat())) {
			player.sendOverlayMessage(Component.translatable("message.wonso.need_energy"));
			return false;
		}
		pay(player, recipe);
		double success = recipe.has("success") ? recipe.get("success").getAsDouble() : 1.0;
		boolean ok = ThreadLocalRandom.current().nextDouble() <= success;
		if (ok) {
			giveOutput(player, recipe);
		} else {
			player.sendOverlayMessage(Component.translatable("message.wonso.refine_fail"));
		}
		giveByproducts(player, recipe);
		noteRefine(player);
		markStage(player, recipe);
		return true;
	}

	private static boolean canPay(ServerPlayer player, JsonObject recipe) {
		if (recipe.has("input")) {
			return enough(player, recipe.getAsJsonObject("input"));
		}
		if (recipe.has("inputs")) {
			for (JsonElement e : recipe.getAsJsonArray("inputs")) {
				if (!enough(player, e.getAsJsonObject())) {
					return false;
				}
			}
		}
		return true;
	}

	private static void pay(ServerPlayer player, JsonObject recipe) {
		if (recipe.has("input")) {
			take(player, recipe.getAsJsonObject("input"));
			return;
		}
		if (recipe.has("inputs")) {
			for (JsonElement e : recipe.getAsJsonArray("inputs")) {
				take(player, e.getAsJsonObject());
			}
		}
	}

	private static boolean enough(ServerPlayer player, JsonObject spec) {
		Item item = itemOf(spec);
		int need = spec.has("count") ? spec.get("count").getAsInt() : 1;
		return item != null && InventoryUtil.count(player, item) >= need;
	}

	private static void take(ServerPlayer player, JsonObject spec) {
		Item item = itemOf(spec);
		int need = spec.has("count") ? spec.get("count").getAsInt() : 1;
		InventoryUtil.remove(player, item, need);
	}

	private static Item itemOf(JsonObject spec) {
		if (spec.has("item")) {
			return InventoryUtil.resolveId(spec.get("item").getAsString());
		}
		if (spec.has("id")) {
			return InventoryUtil.resolveId(spec.get("id").getAsString());
		}
		return null;
	}

	private static void giveOutput(ServerPlayer player, JsonObject recipe) {
		if (recipe.has("outputs")) {
			for (JsonElement e : recipe.getAsJsonArray("outputs")) {
				giveOne(player, e.getAsJsonObject());
			}
			return;
		}
		if (recipe.has("output")) {
			giveOne(player, recipe.getAsJsonObject("output"));
		}
	}

	private static void giveByproducts(ServerPlayer player, JsonObject recipe) {
		if (!recipe.has("byproducts")) {
			return;
		}
		for (JsonElement e : recipe.getAsJsonArray("byproducts")) {
			giveOne(player, e.getAsJsonObject());
		}
	}

	private static void giveOne(ServerPlayer player, JsonObject spec) {
		if (spec.has("chance") && ThreadLocalRandom.current().nextDouble() > spec.get("chance").getAsDouble()) {
			return;
		}
		if (spec.has("alt") && spec.getAsJsonObject("alt").has("weight")
				&& ThreadLocalRandom.current().nextDouble() < spec.getAsJsonObject("alt").get("weight").getAsDouble()) {
			giveOne(player, spec.getAsJsonObject("alt"));
			return;
		}
		int count = spec.has("count") ? spec.get("count").getAsInt() : 1;
		if (spec.has("card")) {
			String card = spec.get("card").getAsString();
			if ("RANDOM_Z1_20".equals(card)) {
				var list = WonsoData.elements();
				card = list.get(ThreadLocalRandom.current().nextInt(Math.min(20, list.size()))).symbol();
			}
			InventoryUtil.give(player, WonsoItems.elementCard(card), count);
			DiscoveryManager.discoverElement(player, card);
			if ("Si".equals(card)) {
				DiscoveryManager.grant(player, "first_card_from_sand");
			}
			return;
		}
		if (!spec.has("id")) {
			return;
		}
		String id = spec.get("id").getAsString();
		Item item = InventoryUtil.resolveId(id);
		InventoryUtil.give(player, item, count);
		if (id.contains("sludge") || id.contains("slurry")) {
			DiscoveryManager.grant(player, "first_sludge");
		}
		if (id.startsWith("impure_")) {
			DiscoveryManager.grant(player, "first_impure");
		}
		if ("slag".equals(id) && InventoryUtil.count(player, InventoryUtil.resolveId("slag")) >= 64) {
			DiscoveryManager.grant(player, "slag_hoarder");
		}
	}

	private static void markStage(ServerPlayer player, JsonObject recipe) {
		String id = recipe.has("id") ? recipe.get("id").getAsString() : "";
		if (id.startsWith("crush")) {
			DiscoveryManager.grant(player, "first_crush");
		}
		player.sendOverlayMessage(Component.translatable("message.wonso.refine_ok", id));
	}

	private static void noteRefine(ServerPlayer player) {
		WonsoPlayerState state = ElementEnergyManager.state(player);
		state.refineCount += 1;
		player.setAttached(ElementEnergyManager.STATE, state);
		if (state.refineCount >= 100) {
			DiscoveryManager.grant(player, "refinery_master");
		}
	}
}
