package com.appiemon.wonso.screen;

import com.appiemon.wonso.client.WonsoClient;
import com.appiemon.wonso.data.ElementRecord;
import com.appiemon.wonso.data.WonsoData;
import com.appiemon.wonso.network.WonsoPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class PartyScreen extends Screen {
	private final List<Integer> selected = new ArrayList<>(WonsoClient.party);

	public PartyScreen() {
		super(Component.translatable("gui.wonso.party"));
	}

	@Override
	protected void init() {
		int cell = 22;
		int startX = 12;
		int startY = 36;
		int i = 0;
		for (ElementRecord el : WonsoData.elements()) {
			if (el.z() > 36) {
				break;
			}
			int x = startX + (i % 12) * (cell + 2);
			int y = startY + (i / 12) * (cell + 2);
			int z = el.z();
			this.addRenderableWidget(Button.builder(Component.literal(el.symbol()), b -> toggle(z))
					.bounds(x, y, cell, cell).build());
			i++;
		}
		this.addRenderableWidget(Button.builder(Component.literal("파티 저장"), b -> save())
				.bounds(this.width / 2 - 90, this.height - 28, 80, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("대전"), b -> {
			save();
			this.minecraft.gui.setScreen(new BattleScreen());
			ClientPlayNetworking.send(new WonsoPackets.BattleCommandC2S("start"));
		}).bounds(this.width / 2 - 4, this.height - 28, 50, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("닫기"), b -> this.onClose())
				.bounds(this.width / 2 + 52, this.height - 28, 50, 20).build());
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

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		graphics.centeredText(this.font, Component.literal("원소 파티 6  (포켓몬식 — 플레이어는 싸우지 않음)"),
				this.width / 2, 8, 0xFFE9ECEF);
		StringBuilder sb = new StringBuilder("선택: ");
		for (int z : selected) {
			var el = WonsoData.byZ(z);
			if (el != null) {
				sb.append(el.symbol()).append(' ');
			}
		}
		graphics.text(this.font, Component.literal(sb + "  " + selected.size() + "/6"), 12, this.height - 48, 0xFFE8C86A);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
