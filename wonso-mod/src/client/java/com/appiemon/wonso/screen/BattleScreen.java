package com.appiemon.wonso.screen;

import com.appiemon.wonso.client.WonsoClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class BattleScreen extends Screen {
	public BattleScreen() {
		super(Component.translatable("gui.wonso.battle"));
	}

	@Override
	protected void init() {
		int y = this.height - 40;
		this.addRenderableWidget(Button.builder(Component.literal("스킬1"), b -> {}).bounds(8, y, 60, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("스킬2"), b -> {}).bounds(72, y, 60, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("스킬3"), b -> {}).bounds(136, y, 60, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("스킬4"), b -> {}).bounds(200, y, 60, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("교체"), b -> {}).bounds(264, y, 50, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("분자"), b -> {}).bounds(318, y, 50, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("턴종료"), b -> this.onClose()).bounds(372, y, 60, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		graphics.text(this.font, Component.literal("⚡ " + (int) WonsoClient.clientEnergy + "   턴제 원소 대전 — 플레이어는 명령만"), 8, 8, 0xFFE8C86A);
		graphics.centeredText(this.font, Component.literal("원소령 필드 (PvE 0.4 프로토타입)"), this.width / 2, this.height / 2 - 20, 0xFFDEE2E6);
		graphics.centeredText(this.font, Component.literal("상대 파티 vs 내 파티 6슬롯"), this.width / 2, this.height / 2, 0xFFADB5BD);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
