package com.appiemon.wonso.screen;

import com.appiemon.wonso.data.MoleculeRecord;
import com.appiemon.wonso.data.WonsoData;
import com.appiemon.wonso.network.WonsoPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SynthesisLabScreen extends Screen {
	private String selected = "water";

	public SynthesisLabScreen() {
		super(Component.translatable("gui.wonso.synthesis"));
	}

	@Override
	protected void init() {
		int y = 40;
		for (MoleculeRecord m : WonsoData.molecules()) {
			if (y > this.height - 50) {
				break;
			}
			String id = m.id();
			this.addRenderableWidget(Button.builder(Component.literal(m.formulaDisplay()), b -> this.selected = id)
					.bounds(12, y, 70, 16).build());
			y += 18;
		}
		this.addRenderableWidget(Button.builder(Component.literal("배합"), b ->
				ClientPlayNetworking.send(new WonsoPackets.SynthC2S(this.selected))
		).bounds(this.width / 2 - 40, this.height - 28, 80, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("닫기"), b -> this.onClose())
				.bounds(this.width / 2 + 50, this.height - 28, 50, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		graphics.centeredText(this.font, this.title, this.width / 2, 10, 0xFFE9ECEF);
		MoleculeRecord m = WonsoData.molecule(selected);
		if (m != null) {
			graphics.text(this.font, Component.literal(m.nameKo() + "  " + m.formulaDisplay()), this.width / 2 - 40, 40, 0xFF80FFEA);
			graphics.text(this.font, Component.literal("재료: " + m.elements()), this.width / 2 - 40, 56, 0xFFDEE2E6);
			graphics.text(this.font, Component.literal("에너지 값 ⚡" + m.energy()), this.width / 2 - 40, 72, 0xFFE8C86A);
			graphics.textWithWordWrap(this.font, Component.literal(m.descriptionKo()), this.width / 2 - 40, 92, 200, 0xFFADB5BD);
		}
		graphics.text(this.font, Component.literal("원소 카드를 인벤에 넣고 배합. 실패 시 ⚡+1"), 12, this.height - 44, 0xFF868E96);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
