package com.appiemon.wonso;

import com.appiemon.wonso.data.WonsoData;
import com.appiemon.wonso.gameplay.DiscoveryManager;
import com.appiemon.wonso.gameplay.ElementEnergyManager;
import com.appiemon.wonso.gameplay.GroupSynergyHandler;
import com.appiemon.wonso.gameplay.RefiningManager;
import com.appiemon.wonso.gameplay.SynthesisRecipeManager;
import com.appiemon.wonso.network.WonsoPackets;
import com.appiemon.wonso.registry.WonsoBlocks;
import com.appiemon.wonso.registry.WonsoCommands;
import com.appiemon.wonso.registry.WonsoItems;
import com.appiemon.wonso.registry.WonsoScreenHandlers;
import com.appiemon.wonso.world.PerioditeOreFeature;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WonsoMod implements ModInitializer {
	public static final String MOD_ID = "wonso";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		WonsoData.load();
		WonsoBlocks.initialize();
		WonsoItems.initialize();
		WonsoScreenHandlers.initialize();
		ElementEnergyManager.initialize();
		SynthesisRecipeManager.initialize();
		DiscoveryManager.initialize();
		GroupSynergyHandler.initialize();
		RefiningManager.initialize();
		WonsoPackets.initialize();
		WonsoCommands.initialize();
		PerioditeOreFeature.initialize();
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			var player = handler.player;
			if (!player.getInventory().hasAnyMatching(stack -> stack.is(WonsoItems.CHEMISTRY_JOURNAL))) {
				player.addItem(new ItemStack(WonsoItems.CHEMISTRY_JOURNAL));
				player.addItem(new ItemStack(WonsoItems.GUIDE_BOOK));
			}
			WonsoPackets.syncState(player);
		});
		LOGGER.info("wonso ready — {} elements, {} molecules",
				WonsoData.elements().size(), WonsoData.molecules().size());
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
