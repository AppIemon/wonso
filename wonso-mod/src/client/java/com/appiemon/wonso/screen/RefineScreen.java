package com.appiemon.wonso.screen;

import com.appiemon.wonso.client.WonsoClient;
import com.appiemon.wonso.client.WonsoGui;
import com.appiemon.wonso.network.WonsoPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class RefineScreen extends WonsoScreen {
	private final String machineId;
	private Action[] actions = new Action[0];

	public RefineScreen(String machineId) {
		super(Component.translatable("gui.wonso.refine"));
		this.machineId = machineId;
	}

	@Override
	protected void init() {
		Action[] all = actionsFor(machineId);
		int cx = this.width / 2 - 110;
		int y = 56;
		this.actions = new Action[all.length + 1];
		for (int i = 0; i < all.length; i++) {
			this.actions[i] = new Action(all[i].label, all[i].command, cx, y, 220);
			y += 28;
		}
		this.actions[all.length] = new Action("닫기", null, cx, this.height - 22, 220);
	}

	private static Action[] actionsFor(String machine) {
		return switch (machine) {
			case "crusher", "evaporation_pan" -> new Action[]{
					new Action("분쇄 (바닐라/중간물)", "crush", 0, 0, 0)
			};
			case "reaction_vat", "distiller" -> new Action[]{
					new Action("용해 / 반응", "dissolve", 0, 0, 0)
			};
			case "electrolyzer" -> new Action[]{
					new Action("전기분해 / 용해", "dissolve", 0, 0, 0)
			};
			case "element_refinery", "reduction_furnace" -> new Action[]{
					new Action("거친 정제", "crude", 0, 0, 0),
					new Action("순화 (원소 카드)", "purify", 0, 0, 0)
			};
			default -> new Action[]{
					new Action("분쇄", "crush", 0, 0, 0),
					new Action("용해", "dissolve", 0, 0, 0),
					new Action("거친 정제", "crude", 0, 0, 0),
					new Action("순화", "purify", 0, 0, 0)
			};
		};
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		WonsoGui.header(graphics, this.font, this.title, machineLabel() + "  ·  ⚡" + (int) WonsoClient.clientEnergy, this.width);
		WonsoGui.panel(graphics, this.width / 2 - 160, 32, 320, 20);
		graphics.centeredText(this.font, Component.literal("재료를 인벤토리에 넣고 단계를 실행합니다"), this.width / 2, 37, WonsoGui.MUTED);
		for (Action action : actions) {
			boolean hover = WonsoGui.hit(mouseX, mouseY, action.x, action.y, action.w, 22);
			WonsoGui.button(graphics, this.font, action.label, action.x, action.y, action.w, 22, hover, false);
		}
	}

	private String machineLabel() {
		return switch (machineId) {
			case "crusher" -> "분쇄기";
			case "reaction_vat" -> "반응통";
			case "evaporation_pan" -> "증발천";
			case "electrolyzer" -> "전기분해기";
			case "distiller" -> "증류기";
			case "reduction_furnace" -> "환원로";
			case "element_refinery" -> "원소 정제기";
			default -> machineId;
		};
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		int mx = (int) event.x();
		int my = (int) event.y();
		for (Action action : actions) {
			if (WonsoGui.hit(mx, my, action.x, action.y, action.w, 22)) {
				if (action.command == null) {
					this.onClose();
				} else {
					ClientPlayNetworking.send(new WonsoPackets.RefineC2S(action.command));
				}
				return true;
			}
		}
		return super.mouseClicked(event, doubled);
	}

	private record Action(String label, String command, int x, int y, int w) {
	}
}
