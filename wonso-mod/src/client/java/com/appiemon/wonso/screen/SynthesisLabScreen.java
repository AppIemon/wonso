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
import java.util.List;

public class SynthesisLabScreen extends WonsoScreen {
	private String selected = "water";
	private int scroll;
	private Btn[] nav = new Btn[0];

	public SynthesisLabScreen() {
		super(Component.translatable("gui.wonso.synthesis"));
	}

	@Override
	protected void init() {
		this.nav = new Btn[]{
				new Btn("▲", 12, this.height - 22, 22, () -> scroll = Math.max(0, scroll - 8)),
				new Btn("▼", 36, this.height - 22, 22, () -> scroll = Math.min(maxScroll(), scroll + 8)),
				new Btn("배합", this.width / 2 - 40, this.height - 22, 80, () ->
						ClientPlayNetworking.send(new WonsoPackets.SynthC2S(this.selected))),
				new Btn("닫기", this.width / 2 + 48, this.height - 22, 50, this::onClose),
		};
	}

	private int rowH() {
		return 14;
	}

	private int visible() {
		return Math.max(4, (this.height - 80) / rowH());
	}

	private int maxScroll() {
		return Math.max(0, WonsoData.molecules().size() - visible());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		WonsoGui.header(graphics, this.font, this.title, "원소 카드를 인벤에 넣고 배합", this.width);

		List<MoleculeRecord> all = WonsoData.molecules();
		int vis = visible();
		int y = 30;
		for (int i = scroll; i < Math.min(all.size(), scroll + vis); i++) {
			MoleculeRecord m = all.get(i);
			boolean on = m.id().equals(selected);
			boolean hover = WonsoGui.hit(mouseX, mouseY, 12, y, 120, rowH());
			WonsoGui.button(graphics, this.font, m.formulaDisplay(), 12, y, 120, rowH(), hover, on);
			y += rowH();
		}

		MoleculeRecord m = WonsoData.molecule(selected);
		if (m != null) {
			WonsoGui.panel(graphics, 150, 30, Math.max(180, this.width - 170), this.height - 64);
			graphics.text(this.font, Component.literal(m.nameKo() + "  " + m.formulaDisplay()), 162, 40, WonsoGui.ACCENT);
			graphics.text(this.font, Component.literal("난이도 " + m.difficulty() + "  ·  ⚡" + m.energy() + "  ·  " + m.category()),
					162, 56, WonsoGui.GOLD);
			StringBuilder parts = new StringBuilder("재료: ");
			for (MoleculeRecord.Part p : m.elements()) {
				parts.append(p.symbol()).append(p.count() > 1 ? p.count() : "").append(" ");
			}
			graphics.text(this.font, Component.literal(parts.toString()), 162, 72, WonsoGui.TEXT);
			graphics.textWithWordWrap(this.font, Component.literal(m.descriptionKo()), 162, 92, Math.max(160, this.width - 190), WonsoGui.MUTED);
			graphics.text(this.font, Component.literal("실패 시 ⚡+1"), 162, this.height - 72, WonsoGui.MUTED);
		}

		for (Btn b : nav) {
			boolean hover = WonsoGui.hit(mouseX, mouseY, b.x, b.y, b.w, 18);
			WonsoGui.button(graphics, this.font, b.label, b.x, b.y, b.w, 18, hover, false);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		int mx = (int) event.x();
		int my = (int) event.y();
		List<MoleculeRecord> all = WonsoData.molecules();
		int vis = visible();
		int y = 30;
		for (int i = scroll; i < Math.min(all.size(), scroll + vis); i++) {
			if (WonsoGui.hit(mx, my, 12, y, 120, rowH())) {
				this.selected = all.get(i).id();
				return true;
			}
			y += rowH();
		}
		for (Btn b : nav) {
			if (WonsoGui.hit(mx, my, b.x, b.y, b.w, 18)) {
				b.run.run();
				return true;
			}
		}
		return super.mouseClicked(event, doubled);
	}

	private record Btn(String label, int x, int y, int w, Runnable run) {
	}
}
