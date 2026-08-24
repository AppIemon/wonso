package com.appiemon.wonso.client;

import com.appiemon.wonso.WonsoMod;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class WonsoHudOverlay {
	private WonsoHudOverlay() {
	}

	public static void initialize() {
		HudElementRegistry.addLast(WonsoMod.id("energy_hud"), (graphics, delta) -> {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null) {
				return;
			}
			String text = "⚡ " + (int) WonsoClient.clientEnergy
					+ "   원소 " + WonsoClient.discoveredElements + "/118"
					+ "   분자 " + WonsoClient.discoveredMolecules;
			int w = mc.font.width(text) + 12;
			graphics.fill(4, 4, 4 + w, 20, 0xC00B1320);
			graphics.fill(4, 4, 4 + w, 5, 0xFFE8C86A);
			graphics.text(mc.font, Component.literal(text), 10, 8, 0xFFE8C86A, false);
		});
	}
}
