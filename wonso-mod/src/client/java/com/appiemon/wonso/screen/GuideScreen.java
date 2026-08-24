package com.appiemon.wonso.screen;

import com.appiemon.wonso.client.WonsoGui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class GuideScreen extends WonsoScreen {
	private static final String[] LINES = {
			"명령어 없이 플레이합니다. /wonso 는 OP(권한 2) 전용입니다.",
			"",
			"화학 도감 우클릭 또는 키 P  — 주기율표 3모드",
			"원소 카드 우클릭  — 해당 원소로 주기율표",
			"분자 카드 우클릭  — 분자 도감 / 웅크리기 = 에너지",
			"가이드북 우클릭  — 이 안내",
			"",
			"분쇄기 · 반응통 · 정제기 · 전기분해기 우클릭  — 4단 정제",
			"핵융합 챔버  — H+H→He, 3He→C, U+C→Og / 별",
			"원소 신전  — 턴제 대전 (파티 6, 플레이어는 명령만)",
			"연구대  — 파티 편성",
			"",
			"에너지 스킬  Z 치료 · X 공격 · C 에너지+ · V 카드 뽑기",
	};

	public GuideScreen() {
		super(Component.translatable("gui.wonso.guide"));
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		WonsoGui.header(graphics, this.font, this.title, "아이템 · GUI 로 모든 기능", this.width);
		WonsoGui.panel(graphics, 16, 30, this.width - 32, this.height - 64);
		int y = 40;
		for (String line : LINES) {
			graphics.text(this.font, Component.literal(line.isEmpty() ? " " : line), 28, y, line.isEmpty() ? WonsoGui.MUTED : WonsoGui.TEXT);
			y += 12;
		}
		boolean hover = WonsoGui.hit(mouseX, mouseY, this.width / 2 - 40, this.height - 22, 80, 18);
		WonsoGui.button(graphics, this.font, "주기율표", this.width / 2 - 40, this.height - 22, 80, 18, hover, false);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		if (WonsoGui.hit(event.x(), event.y(), this.width / 2 - 40, this.height - 22, 80, 18)) {
			this.minecraft.gui.setScreen(new PeriodicTableScreen());
			return true;
		}
		return super.mouseClicked(event, doubled);
	}
}
