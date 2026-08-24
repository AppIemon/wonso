package com.appiemon.wonso.gameplay;

import com.appiemon.wonso.WonsoMod;
import com.appiemon.wonso.data.WonsoData;
import com.appiemon.wonso.data.WonsoPlayerState;
import com.appiemon.wonso.network.WonsoPackets;
import com.appiemon.wonso.registry.WonsoItems;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public final class ElementEnergyManager {
	public static final AttachmentType<WonsoPlayerState> STATE = AttachmentRegistry.create(
			WonsoMod.id("player_state"),
			builder -> builder.persistent(WonsoPlayerState.CODEC).initializer(WonsoPlayerState::new).copyOnDeath()
	);

	private ElementEnergyManager() {
	}

	public static void initialize() {
	}

	public static WonsoPlayerState state(Player player) {
		return player.getAttachedOrCreate(STATE);
	}

	public static float energy(Player player) {
		return state(player).energy;
	}

	public static void addEnergy(Player player, float amount) {
		WonsoPlayerState s = state(player);
		s.addEnergy(amount);
		player.setAttached(STATE, s);
		sync(player);
	}

	public static boolean spend(Player player, float cost) {
		WonsoPlayerState s = state(player);
		if (s.energy < cost) {
			return false;
		}
		s.addEnergy(-cost);
		player.setAttached(STATE, s);
		sync(player);
		return true;
	}

	public static boolean useSkill(ServerPlayer player, String skillId) {
		for (var el : WonsoData.energyAbilities()) {
			JsonObject o = el.getAsJsonObject();
			if (!skillId.equals(o.get("id").getAsString())) {
				continue;
			}
			int cost = o.get("cost").getAsInt();
			if (!spend(player, cost)) {
				return false;
			}
			String effect = o.get("effect").getAsString();
			switch (effect) {
				case "heal_player" -> player.heal(o.get("value").getAsFloat());
				case "strength" -> player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, o.get("seconds").getAsInt() * 20, 0));
				case "energy" -> addEnergy(player, o.get("value").getAsFloat());
				case "random_element_card" -> {
					var list = WonsoData.elements();
					var pick = list.get(ThreadLocalRandom.current().nextInt(Math.min(20, list.size())));
					player.addItem(new ItemStack(WonsoItems.elementCard(pick.symbol())));
					DiscoveryManager.discoverElement(player, pick.symbol());
				}
				default -> {
				}
			}
			DiscoveryManager.markEnergySkill(player);
			return true;
		}
		return false;
	}

	public static void convertMolecule(ServerPlayer player, String moleculeId) {
		var mol = WonsoData.molecule(moleculeId);
		if (mol == null) {
			return;
		}
		addEnergy(player, mol.energy());
		DiscoveryManager.markEnergyConvert(player);
	}

	private static void sync(Player player) {
		if (player instanceof ServerPlayer serverPlayer) {
			WonsoPackets.syncState(serverPlayer);
		}
	}
}
