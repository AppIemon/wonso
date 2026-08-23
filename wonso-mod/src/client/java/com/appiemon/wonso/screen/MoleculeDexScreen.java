package com.appiemon.wonso.screen;

import com.appiemon.wonso.data.MoleculeRecord;
import com.appiemon.wonso.data.WonsoData;
import com.appiemon.wonso.network.WonsoPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MoleculeDexScreen extends Screen {
	private String sort = "difficulty";

	public MoleculeDexScreen() {
		super(Component.translatable("gui.wonso.molecule_dex"));
	}

	@Override
	protected void init() {
		int x = 8;
		for (String s : List.of("difficulty", "name", "energy", "formula", "category")) {
			String key = s;
			this.addRenderableWidget(Button.builder(Component.literal(s), b -> this.sort = key).bounds(x, 22, 70, 16).build());
			x += 74;
		}
		this.addRenderableWidget(Button.builder(Component.literal("주기율표"), b -> this.minecraft.gui.setScreen(new PeriodicTableScreen())).bounds(this.width - 80, 8, 72, 16).build());
		int y = 44;
		for (MoleculeRecord m : sorted()) {
			if (y > this.height - 24) {
				break;
			}
			this.addRenderableWidget(Button.builder(
					Component.literal(m.formulaDisplay() + " " + m.nameKo() + " ⚡" + m.energy()),
					b -> ClientPlayNetworking.send(new WonsoPackets.SynthC2S(m.id()))
			).bounds(16, y, Math.min(360, this.width - 32), 14).build());
			y += 16;
		}
	}

	private List<MoleculeRecord> sorted() {
		List<MoleculeRecord> list = new ArrayList<>(WonsoData.molecules());
		switch (sort) {
			case "name" -> list.sort(Comparator.comparing(MoleculeRecord::nameKo));
			case "energy" -> list.sort(Comparator.comparingInt(MoleculeRecord::energy).reversed());
			case "formula" -> list.sort(Comparator.comparing(MoleculeRecord::formula));
			case "category" -> list.sort(Comparator.comparing(MoleculeRecord::category));
			default -> list.sort(Comparator.comparingInt(MoleculeRecord::difficulty));
		}
		return list;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		graphics.text(this.font, Component.literal("분자 도감  " + WonsoData.molecules().size() + "  정렬:" + sort), 8, 8, 0xFFE9ECEF);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
