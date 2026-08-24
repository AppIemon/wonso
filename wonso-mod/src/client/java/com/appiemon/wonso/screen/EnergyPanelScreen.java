package com.appiemon.wonso.screen;

import com.appiemon.wonso.client.WonsoClient;
import com.appiemon.wonso.client.WonsoGui;
import com.appiemon.wonso.data.WonsoData;
import com.appiemon.wonso.network.WonsoPackets;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class EnergyPanelScreen extends WonsoScreen {
	private Tile[] tiles = new Tile[0];

	public EnergyPanelScreen() {
		super(Component.translatable("gui.wonso.energy"));
	}

	@Override
	protected void init() {
		int n = WonsoData.energyAbilities().size();
		this.tiles = new Tile[n];
		int y = 48;
		for (int i = 0; i < n; i++) {
			JsonObject o = WonsoData.energyAbilities().get(i).getAsJsonObject();
			String id = o.get("id").getAsString();
			String key = switch (id) {
				case "heal_all" -> "Z";
				case "attack_up" -> "X";
				case "energy_up" -> "C";
				case "draw_card" -> "V";
				default -> "·";
			};
			String label = "[" + key + "]  " + o.get("nameKo").getAsString() + "   ⚡" + o.get("cost").getAsInt();
			this.tiles[i] = new Tile(label, id, this.width / 2 - 130, y, 260);
			y += 32;
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		WonsoGui.header(graphics, this.font, this.title, "⚡ " + (int) WonsoClient.clientEnergy, this.width);
		WonsoGui.panel(graphics, this.width / 2 - 150, 28, 300, 16);
		graphics.centeredText(this.font, Component.literal("분자 카드 웅크리기 사용 = 분해 → 에너지"), this.width / 2, 32, WonsoGui.MUTED);
		for (Tile tile : tiles) {
			boolean hover = WonsoGui.hit(mouseX, mouseY, tile.x, tile.y, tile.w, 26);
			WonsoGui.button(graphics, this.font, tile.label, tile.x, tile.y, tile.w, 26, hover, false);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		int mx = (int) event.x();
		int my = (int) event.y();
		for (Tile tile : tiles) {
			if (WonsoGui.hit(mx, my, tile.x, tile.y, tile.w, 26)) {
				ClientPlayNetworking.send(new WonsoPackets.SkillC2S(tile.id));
				return true;
			}
		}
		return super.mouseClicked(event, doubled);
	}

	private record Tile(String label, String id, int x, int y, int w) {
	}
}
