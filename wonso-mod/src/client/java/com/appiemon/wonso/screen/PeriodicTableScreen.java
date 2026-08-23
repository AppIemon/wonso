package com.appiemon.wonso.screen;

import com.appiemon.wonso.client.AtomModelRenderer;
import com.appiemon.wonso.client.WonsoClient;
import com.appiemon.wonso.data.ElementRecord;
import com.appiemon.wonso.data.MoleculeRecord;
import com.appiemon.wonso.data.WonsoData;
import com.appiemon.wonso.network.WonsoPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PeriodicTableScreen extends Screen {
	public PeriodicTableScreen() {
		super(Component.translatable("gui.wonso.periodic_table"));
	}

	@Override
	protected void init() {
		int cell = Math.max(14, Math.min(22, (this.width - 40) / 18));
		int startX = (this.width - cell * 18) / 2;
		int startY = 36;
		for (ElementRecord el : WonsoData.elements()) {
			int x = startX + (el.displayColumn() - 1) * cell;
			int y = startY + (el.displayRow() - 1) * cell;
			this.addRenderableWidget(Button.builder(Component.literal(el.symbol()), btn -> WonsoClient.selectedZ = el.z())
					.bounds(x, y, cell - 1, cell - 1).build());
		}
		int by = this.height - 24;
		this.addRenderableWidget(Button.builder(Component.literal("원소"), b -> setMode("element")).bounds(8, by, 50, 18).build());
		this.addRenderableWidget(Button.builder(Component.literal("원자"), b -> setMode("atom")).bounds(62, by, 50, 18).build());
		this.addRenderableWidget(Button.builder(Component.literal("분자"), b -> setMode("molecule")).bounds(116, by, 50, 18).build());
		this.addRenderableWidget(Button.builder(Component.literal("도감"), b -> this.minecraft.gui.setScreen(new MoleculeDexScreen())).bounds(170, by, 50, 18).build());
		this.addRenderableWidget(Button.builder(Component.literal("합성"), b -> this.minecraft.gui.setScreen(new SynthesisLabScreen())).bounds(224, by, 50, 18).build());
		this.addRenderableWidget(Button.builder(Component.literal("에너지"), b -> this.minecraft.gui.setScreen(new EnergyPanelScreen())).bounds(278, by, 54, 18).build());
	}

	private void setMode(String mode) {
		WonsoClient.mode = mode;
		if ("atom".equals(mode)) {
			ClientPlayNetworking.send(new WonsoPackets.OpenUiC2S("atom"));
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		graphics.centeredText(this.font, this.title, this.width / 2, 8, 0xFFE9ECEF);
		graphics.text(this.font, Component.literal("탭: " + WonsoClient.mode + "  ⚡" + (int) WonsoClient.clientEnergy), 8, 10, 0xFFE8C86A);

		ElementRecord el = WonsoData.byZ(WonsoClient.selectedZ);
		if (el == null) {
			return;
		}
		int panelX = 8;
		int panelY = this.height - 120;
		graphics.fill(panelX, panelY, panelX + 260, panelY + 88, 0xCC0B1320);
		graphics.text(this.font, Component.literal(el.symbol() + "  " + el.nameKo() + "  Z=" + el.z()), panelX + 6, panelY + 6, 0xFFFFFFFF);

		if ("atom".equals(WonsoClient.mode)) {
			var atom = WonsoData.atom(el.z());
			if (atom != null) {
				AtomModelRenderer.render(graphics, this.font, atom, this.width / 2, this.height / 2 + 20);
			}
		} else if ("molecule".equals(WonsoClient.mode)) {
			int y = panelY + 22;
			graphics.text(this.font, Component.literal("관련 분자"), panelX + 6, y, 0xFF80FFEA);
			y += 12;
			for (String id : el.relatedMoleculeIds()) {
				MoleculeRecord m = WonsoData.molecule(id);
				if (m == null) {
					continue;
				}
				graphics.text(this.font, Component.literal(m.formulaDisplay() + "  " + m.nameKo() + "  ⚡" + m.energy()), panelX + 6, y, 0xFFCED4DA);
				y += 10;
				if (y > panelY + 80) {
					break;
				}
			}
			if (el.relatedMoleculeIds().isEmpty()) {
				graphics.text(this.font, Component.literal(el.hasKnownMolecules() ? "Tier A 미수록" : "알려진 분자 없음"), panelX + 6, y, 0xFFADB5BD);
			}
		} else {
			graphics.textWithWordWrap(this.font, Component.literal(el.descriptionKo() == null ? "" : el.descriptionKo()), panelX + 6, panelY + 22, 248, 0xFFDEE2E6);
			graphics.text(this.font, Component.literal("주기 " + el.period() + "  족 " + el.group() + "  " + el.category()), panelX + 6, panelY + 70, 0xFFADB5BD);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
