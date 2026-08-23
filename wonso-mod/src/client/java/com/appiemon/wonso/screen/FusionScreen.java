package com.appiemon.wonso.screen;

import com.appiemon.wonso.client.WonsoClient;
import com.appiemon.wonso.data.WonsoData;
import com.appiemon.wonso.network.WonsoPackets;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class FusionScreen extends Screen {
	public FusionScreen() {
		super(Component.translatable("gui.wonso.fusion"));
	}

	@Override
	protected void init() {
		int y = 44;
		for (var raw : WonsoData.fusion()) {
			JsonObject o = raw.getAsJsonObject();
			String id = o.get("id").getAsString();
			String label = id + "  → " + o.get("output").getAsString() + "  ⚡" + o.get("energyCost").getAsInt();
			this.addRenderableWidget(Button.builder(Component.literal(label), b ->
					ClientPlayNetworking.send(new WonsoPackets.RefineC2S("fusion:" + id))
			).bounds(this.width / 2 - 120, y, 240, 20).build());
			y += 24;
		}
		y += 8;
		for (var raw : WonsoData.stars()) {
			JsonObject o = raw.getAsJsonObject();
			String id = o.get("id").getAsString();
			String label = o.get("nameKo").getAsString() + "  ⚡" + o.get("costEnergy").getAsInt();
			this.addRenderableWidget(Button.builder(Component.literal(label), b ->
					ClientPlayNetworking.send(new WonsoPackets.RefineC2S("star:" + id))
			).bounds(this.width / 2 - 120, y, 240, 20).build());
			y += 24;
		}
		this.addRenderableWidget(Button.builder(Component.literal("닫기"), b -> this.onClose())
				.bounds(this.width / 2 - 40, this.height - 28, 80, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		graphics.centeredText(this.font, Component.literal("핵융합 / 별   ⚡" + (int) WonsoClient.clientEnergy),
				this.width / 2, 12, 0xFFE8C86A);
		graphics.centeredText(this.font, Component.literal("H+H→He, 3He→C, U+C→Og. 별은 카드 드로우와 디버프."),
				this.width / 2, 28, 0xFFADB5BD);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
