package com.appiemon.wonso.screen;

import com.appiemon.wonso.client.WonsoGui;
import com.appiemon.wonso.data.MoleculeRecord;
import com.appiemon.wonso.data.WonsoData;
import com.appiemon.wonso.network.WonsoPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MoleculeDexScreen extends WonsoScreen {
	private String sort = "difficulty";
	private int scroll;
	private Btn[] sorts = new Btn[0];

	public MoleculeDexScreen() {
		super(Component.translatable("gui.wonso.molecule_dex"));
	}

	@Override
	protected void init() {
		int x = 8;
		List<Btn> list = new ArrayList<>();
		for (String s : List.of("difficulty", "name", "energy", "formula", "category")) {
			String key = s;
			list.add(new Btn(s, x, 24, 70, () -> {
				this.sort = key;
				this.scroll = 0;
			}));
			x += 74;
		}
		list.add(new Btn("주기율표", this.width - 80, 4, 72, () -> this.minecraft.gui.setScreen(new PeriodicTableScreen())));
		list.add(new Btn("▲", this.width - 28, 44, 18, () -> scroll = Math.max(0, scroll - 8)));
		list.add(new Btn("▼", this.width - 28, this.height - 28, 18, () -> scroll = Math.min(maxScroll(), scroll + 8)));
		this.sorts = list.toArray(Btn[]::new);
	}

	private int visible() {
		return Math.max(6, (this.height - 56) / 14);
	}

	private int maxScroll() {
		return Math.max(0, sorted().size() - visible());
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
		WonsoGui.header(graphics, this.font, this.title, WonsoData.molecules().size() + "종  ·  정렬 " + sort, this.width);
		for (Btn b : sorts) {
			boolean hover = WonsoGui.hit(mouseX, mouseY, b.x, b.y, b.w, 16);
			WonsoGui.button(graphics, this.font, b.label, b.x, b.y, b.w, 16, hover, b.label.equals(sort));
		}
		List<MoleculeRecord> list = sorted();
		int vis = visible();
		int y = 44;
		for (int i = scroll; i < Math.min(list.size(), scroll + vis); i++) {
			MoleculeRecord m = list.get(i);
			boolean hover = WonsoGui.hit(mouseX, mouseY, 12, y, Math.min(400, this.width - 48), 13);
			WonsoGui.button(graphics, this.font,
					m.formulaDisplay() + "  " + m.nameKo() + "  ⚡" + m.energy() + "  lv" + m.difficulty(),
					12, y, Math.min(400, this.width - 48), 13, hover, false);
			y += 14;
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		int mx = (int) event.x();
		int my = (int) event.y();
		for (Btn b : sorts) {
			if (WonsoGui.hit(mx, my, b.x, b.y, b.w, 16)) {
				b.run.run();
				return true;
			}
		}
		List<MoleculeRecord> list = sorted();
		int vis = visible();
		int y = 44;
		for (int i = scroll; i < Math.min(list.size(), scroll + vis); i++) {
			if (WonsoGui.hit(mx, my, 12, y, Math.min(400, this.width - 48), 13)) {
				ClientPlayNetworking.send(new WonsoPackets.SynthC2S(list.get(i).id()));
				return true;
			}
			y += 14;
		}
		return super.mouseClicked(event, doubled);
	}

	private record Btn(String label, int x, int y, int w, Runnable run) {
	}
}
