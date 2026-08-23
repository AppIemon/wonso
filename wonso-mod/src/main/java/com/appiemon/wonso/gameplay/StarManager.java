package com.appiemon.wonso.gameplay;

import com.appiemon.wonso.data.WonsoData;
import com.appiemon.wonso.registry.WonsoItems;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public final class StarManager {
	private StarManager() {
	}

	public static boolean ignite(ServerPlayer player, String starId) {
		for (var raw : WonsoData.stars()) {
			JsonObject o = raw.getAsJsonObject();
			if (!starId.equals(o.get("id").getAsString())) {
				continue;
			}
			if (!ElementEnergyManager.spend(player, o.get("costEnergy").getAsFloat())) {
				player.sendOverlayMessage(Component.translatable("message.wonso.need_energy"));
				return false;
			}
			switch (starId) {
				case "neutron_star" -> {
					player.addItem(new ItemStack(WonsoItems.NEUTRON_STAR_FRAGMENT));
					player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20 * 20, 0));
				}
				case "black_hole" -> {
					player.addItem(new ItemStack(WonsoItems.BLACK_HOLE_ESSENCE));
					player.addEffect(new MobEffectInstance(MobEffects.WITHER, 8 * 20, 0));
				}
				default -> player.addItem(new ItemStack(WonsoItems.STELLAR_CORE));
			}
			int draws = o.get("drawBonus").getAsInt();
			var list = WonsoData.elements();
			for (int i = 0; i < draws; i++) {
				var pick = list.get(ThreadLocalRandom.current().nextInt(Math.min(26, list.size())));
				InventoryUtil.give(player, WonsoItems.elementCard(pick.symbol()), 1);
				DiscoveryManager.discoverElement(player, pick.symbol());
			}
			DiscoveryManager.grant(player, "star_system");
			player.sendOverlayMessage(Component.translatable("message.wonso.star_ok", o.get("nameKo").getAsString()));
			return true;
		}
		return false;
	}
}
