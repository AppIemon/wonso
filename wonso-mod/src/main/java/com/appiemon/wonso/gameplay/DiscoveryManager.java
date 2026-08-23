package com.appiemon.wonso.gameplay;

import com.appiemon.wonso.WonsoMod;
import com.appiemon.wonso.data.WonsoPlayerState;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class DiscoveryManager {
	private DiscoveryManager() {
	}

	public static void initialize() {
	}

	public static boolean discoverElement(Player player, String symbol) {
		WonsoPlayerState state = ElementEnergyManager.state(player);
		boolean added = state.discoverElement(symbol);
		player.setAttached(ElementEnergyManager.STATE, state);
		if (added && player instanceof ServerPlayer serverPlayer) {
			grant(serverPlayer, "first_element");
		}
		return added;
	}

	public static boolean discoverMolecule(Player player, String id) {
		WonsoPlayerState state = ElementEnergyManager.state(player);
		boolean added = state.discoverMolecule(id);
		player.setAttached(ElementEnergyManager.STATE, state);
		if (added && player instanceof ServerPlayer serverPlayer) {
			grant(serverPlayer, "molecule_dex");
			grant(serverPlayer, "first_synthesis");
		}
		return added;
	}

	public static void markTableOpened(Player player) {
		WonsoPlayerState state = ElementEnergyManager.state(player);
		state.tableOpened = true;
		player.setAttached(ElementEnergyManager.STATE, state);
		if (player instanceof ServerPlayer serverPlayer) {
			grant(serverPlayer, "open_periodic_table");
		}
	}

	public static void markAtomViewed(Player player) {
		WonsoPlayerState state = ElementEnergyManager.state(player);
		state.atomViewed = true;
		player.setAttached(ElementEnergyManager.STATE, state);
		if (player instanceof ServerPlayer serverPlayer) {
			grant(serverPlayer, "atom_view");
		}
	}

	public static void markEnergyConvert(ServerPlayer player) {
		grant(player, "energy_convert");
	}

	public static void markEnergySkill(ServerPlayer player) {
		grant(player, "energy_skill");
	}

	public static void markSynergy(ServerPlayer player) {
		grant(player, "group_synergy");
	}

	public static void grant(ServerPlayer player, String path) {
		if (player == null || path == null || path.isBlank()) {
			return;
		}
		String idPath = path.contains(":") ? path.substring(path.indexOf(':') + 1) : path.replace("wonso/", "");
		Identifier id = WonsoMod.id(idPath);
		try {
			var server = player.level().getServer();
			if (server == null) {
				return;
			}
			AdvancementHolder holder = server.getAdvancements().get(id);
			if (holder != null) {
				player.getAdvancements().award(holder, "wonso");
			}
		} catch (Exception e) {
			WonsoMod.LOGGER.debug("advancement {} skipped: {}", id, e.toString());
		}
	}
}
