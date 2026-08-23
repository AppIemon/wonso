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

public class EnergyPanelScreen extends Screen {
	public EnergyPanelScreen() {
		super(Component.translatable("gui.wonso.energy"));
	}

	@Override
	protected void init() {
		int y = 50;
		for (var raw : WonsoData.energyAbilities()) {
			JsonObject o = raw.getAsJsonObject();
			String id = o.get("id").getAsString();
			String label = o.get("nameKo").getAsString() + "  ⚡" + o.get("cost").getAsInt();
			this.addRenderableWidget(Button.builder(Component.literal(label), b ->
					ClientPlayNetworking.send(new WonsoPackets.SkillC2S(id))
			).bounds(this.width / 2 - 80, y, 160, 20).build());
			y += 26;
		}
		this.addRenderableWidget(Button.builder(Component.literal("맷돌 분쇄 시도"), b ->
				ClientPlayNetworking.send(new WonsoPackets.RefineC2S("crush"))
		).bounds(this.width / 2 - 80, y + 10, 160, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		graphics.centeredText(this.font, Component.literal("⚡ 원소 에너지  " + (int) WonsoClient.clientEnergy), this.width / 2, 20, 0xFFE8C86A);
		graphics.centeredText(this.font, Component.literal("분자 카드 웅크리기 사용 = 분해→에너지"), this.width / 2, this.height - 24, 0xFFADB5BD);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
