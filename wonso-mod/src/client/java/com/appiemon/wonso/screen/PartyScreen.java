package com.appiemon.wonso.screen;

import com.appiemon.wonso.client.WonsoClient;
import com.appiemon.wonso.client.WonsoGui;
import com.appiemon.wonso.data.ElementRecord;
import com.appiemon.wonso.data.WonsoData;
import com.appiemon.wonso.network.WonsoPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class PartyScreen extends WonsoScreen {
	private final List<Integer> selected = new ArrayList<>(WonsoClient.party);
	private Btn[] nav = new Btn[0];

	public PartyScreen() {
		super(Component.translatable("gui.wonso.party"));
	}

	@Override
	protected void init() {
		this.nav = new Btn[]{
				new Btn("파티 저장", this.width / 2 - 110, this.height - 22, 72, this::save),
				new Btn("대전", this.width / 2 - 32, this.height - 22, 52, () -> {
					save();
					this.minecraft.gui.setScreen(new BattleScreen());
					ClientPlayNetworking.send(new WonsoPackets.BattleCommandC2S("start"));
				}),
				new Btn("닫기", this.width / 2 + 28, this.height - 22, 52, this::onClose),
		};
	}

	private void toggle(int z) {
		if (selected.contains(z)) {
			selected.remove((Integer) z);
		} else if (selected.size() < 6) {
			selected.add(z);
		}
	}

	private void save() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < selected.size(); i++) {
			if (i > 0) {
				sb.append(',');
			}
			sb.append(selected.get(i));
		}
		WonsoClient.party.clear();
		WonsoClient.party.addAll(selected);
		ClientPlayNetworking.send(new WonsoPackets.PartyC2S(sb.toString()));
	}

	private int cell() {
		return Math.max(14, Math.min(18, (this.width - 24) / 18));
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		WonsoGui.header(graphics, this.font, this.title, selected.size() + "/6  ·  포켓몬식 — 플레이어는 싸우지 않음", this.width);

		int cell = cell();
		int startX = 12;
		int startY = 30;
		int i = 0;
		for (ElementRecord el : WonsoData.elements()) {
			int x = startX + (i % 18) * cell;
			int y = startY + (i / 18) * cell;
			WonsoGui.cell(graphics, this.font, el, x, y, cell - 1, selected.contains(el.z()));
			i++;
		}

		StringBuilder sb = new StringBuilder("선택: ");
		for (int z : selected) {
			var el = WonsoData.byZ(z);
			if (el != null) {
				sb.append(el.symbol()).append(' ');
			}
		}
		graphics.text(this.font, Component.literal(sb.toString()), 12, this.height - 42, WonsoGui.GOLD);

		for (Btn b : nav) {
			boolean hover = WonsoGui.hit(mouseX, mouseY, b.x, b.y, b.w, 18);
			WonsoGui.button(graphics, this.font, b.label, b.x, b.y, b.w, 18, hover, false);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		int mx = (int) event.x();
		int my = (int) event.y();
		int cell = cell();
		int startX = 12;
		int startY = 30;
		int i = 0;
		for (ElementRecord el : WonsoData.elements()) {
			int x = startX + (i % 18) * cell;
			int y = startY + (i / 18) * cell;
			if (WonsoGui.hit(mx, my, x, y, cell - 1, cell - 1)) {
				toggle(el.z());
				return true;
			}
			i++;
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
