package com.appiemon.wonso.screen;

import com.appiemon.wonso.client.WonsoClient;
import com.appiemon.wonso.network.WonsoPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class RefineScreen extends Screen {
	private final String machineId;

	public RefineScreen(String machineId) {
		super(Component.translatable("gui.wonso.refine"));
		this.machineId = machineId;
	}

	@Override
	protected void init() {
		int cx = this.width / 2 - 80;
		int y = 48;
		for (Action action : actionsFor(machineId)) {
			String cmd = action.command;
			this.addRenderableWidget(Button.builder(Component.literal(action.label), b ->
					ClientPlayNetworking.send(new WonsoPackets.RefineC2S(cmd))
			).bounds(cx, y, 160, 20).build());
			y += 24;
		}
		this.addRenderableWidget(Button.builder(Component.literal("닫기"), b -> this.onClose())
				.bounds(cx, this.height - 28, 160, 20).build());
	}

	private static Action[] actionsFor(String machine) {
		return switch (machine) {
			case "crusher", "evaporation_pan" -> new Action[]{
					new Action("분쇄 (바닐라/중간물)", "crush")
			};
			case "reaction_vat", "distiller" -> new Action[]{
					new Action("용해 / 반응", "dissolve")
			};
			case "electrolyzer" -> new Action[]{
					new Action("전기분해 / 용해", "dissolve")
			};
			case "element_refinery", "reduction_furnace" -> new Action[]{
					new Action("거친 정제", "crude"),
					new Action("순화 (카드)", "purify")
			};
			default -> new Action[]{
					new Action("분쇄", "crush"),
					new Action("용해", "dissolve"),
					new Action("거친 정제", "crude"),
					new Action("순화", "purify")
			};
		};
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		graphics.centeredText(this.font, Component.literal("정제 — " + machineId + "   ⚡" + (int) WonsoClient.clientEnergy),
				this.width / 2, 16, 0xFFE8C86A);
		graphics.centeredText(this.font, Component.literal("재료를 인벤에 넣고 단계를 실행. 물 양동이는 빈 양동이로 돌아온다."),
				this.width / 2, 32, 0xFFADB5BD);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private record Action(String label, String command) {
	}
}
