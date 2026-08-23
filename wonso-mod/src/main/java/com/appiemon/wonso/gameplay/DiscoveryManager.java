package com.appiemon.wonso.gameplay;

import com.appiemon.wonso.WonsoMod;
import com.appiemon.wonso.data.WonsoPlayerState;
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
			grant(serverPlayer, "wonso/first_element");
		}
		return added;
	}

	public static boolean discoverMolecule(Player player, String id) {
		WonsoPlayerState state = ElementEnergyManager.state(player);
		boolean added = state.discoverMolecule(id);
		player.setAttached(ElementEnergyManager.STATE, state);
		if (added && player instanceof ServerPlayer serverPlayer) {
			grant(serverPlayer, "wonso/molecule_dex");
			grant(serverPlayer, "wonso/first_synthesis");
		}
		return added;
	}

	public static void markTableOpened(Player player) {
		WonsoPlayerState state = ElementEnergyManager.state(player);
		state.tableOpened = true;
		player.setAttached(ElementEnergyManager.STATE, state);
		if (player instanceof ServerPlayer serverPlayer) {
			grant(serverPlayer, "wonso/open_periodic_table");
		}
	}

	public static void markAtomViewed(Player player) {
		WonsoPlayerState state = ElementEnergyManager.state(player);
		state.atomViewed = true;
		player.setAttached(ElementEnergyManager.STATE, state);
		if (player instanceof ServerPlayer serverPlayer) {
			grant(serverPlayer, "wonso/atom_view");
		}
	}

	public static void markEnergyConvert(ServerPlayer player) {
		grant(player, "wonso/energy_convert");
	}

	public static void markEnergySkill(ServerPlayer player) {
		grant(player, "wonso/energy_skill");
	}

	public static void markSynergy(ServerPlayer player) {
		grant(player, "wonso/group_synergy");
	}

	private static void grant(ServerPlayer player, String path) {
		WonsoMod.LOGGER.debug("advancement {}", path);
	}
}
