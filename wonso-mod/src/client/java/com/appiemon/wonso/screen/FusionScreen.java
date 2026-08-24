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

public class FusionScreen extends WonsoScreen {
	private Tile[] tiles = new Tile[0];

	public FusionScreen() {
		super(Component.translatable("gui.wonso.fusion"));
	}

	@Override
	protected void init() {
		int n = WonsoData.fusion().size() + WonsoData.stars().size() + 1;
		this.tiles = new Tile[n];
		int y = 44;
		int i = 0;
		for (var raw : WonsoData.fusion()) {
			JsonObject o = raw.getAsJsonObject();
			String id = o.get("id").getAsString();
			String inputs = o.get("inputs").toString().replace("\"", "").replace("[", "").replace("]", "");
			String label = inputs + "  →  " + o.get("output").getAsString() + "   ⚡" + o.get("energyCost").getAsInt();
			this.tiles[i++] = new Tile(label, "fusion:" + id, this.width / 2 - 140, y, 280);
			y += 26;
		}
		y += 8;
		for (var raw : WonsoData.stars()) {
			JsonObject o = raw.getAsJsonObject();
			String id = o.get("id").getAsString();
			String label = "★ " + o.get("nameKo").getAsString() + "   ⚡" + o.get("costEnergy").getAsInt();
			this.tiles[i++] = new Tile(label, "star:" + id, this.width / 2 - 140, y, 280);
			y += 26;
		}
		this.tiles[i] = new Tile("닫기", null, this.width / 2 - 40, this.height - 22, 80);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		WonsoGui.header(graphics, this.font, this.title, "⚡" + (int) WonsoClient.clientEnergy, this.width);
		graphics.centeredText(this.font, Component.literal("H+H→He, 3He→C, U+C→Og. 별은 카드 드로우와 디버프."),
				this.width / 2, 28, WonsoGui.MUTED);
		for (Tile tile : tiles) {
			boolean hover = WonsoGui.hit(mouseX, mouseY, tile.x, tile.y, tile.w, 22);
			WonsoGui.button(graphics, this.font, tile.label, tile.x, tile.y, tile.w, 22, hover, false);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		int mx = (int) event.x();
		int my = (int) event.y();
		for (Tile tile : tiles) {
			if (WonsoGui.hit(mx, my, tile.x, tile.y, tile.w, 22)) {
				if (tile.action == null) {
					this.onClose();
				} else {
					ClientPlayNetworking.send(new WonsoPackets.RefineC2S(tile.action));
				}
				return true;
			}
		}
		return super.mouseClicked(event, doubled);
	}

	private record Tile(String label, String action, int x, int y, int w) {
	}
}
