package com.appiemon.wonso.screen;

import com.appiemon.wonso.client.WonsoClient;
import com.appiemon.wonso.network.WonsoPackets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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
		int y = this.height - 44;
		this.addRenderableWidget(Button.builder(Component.literal("스킬1"), b -> cmd("skill:1")).bounds(8, y, 56, 18).build());
		this.addRenderableWidget(Button.builder(Component.literal("스킬2"), b -> cmd("skill:2")).bounds(68, y, 56, 18).build());
		this.addRenderableWidget(Button.builder(Component.literal("스킬3"), b -> cmd("skill:3")).bounds(128, y, 56, 18).build());
		this.addRenderableWidget(Button.builder(Component.literal("스킬4"), b -> cmd("skill:4")).bounds(188, y, 56, 18).build());
		this.addRenderableWidget(Button.builder(Component.literal("교체"), b -> switchNext()).bounds(248, y, 44, 18).build());
		this.addRenderableWidget(Button.builder(Component.literal("H2O"), b -> cmd("tool:water")).bounds(296, y, 40, 18).build());
		this.addRenderableWidget(Button.builder(Component.literal("턴종료"), b -> cmd("pass")).bounds(340, y, 50, 18).build());
		this.addRenderableWidget(Button.builder(Component.literal("도주"), b -> cmd("flee")).bounds(394, y, 40, 18).build());
		this.addRenderableWidget(Button.builder(Component.literal("신전 시작"), b -> cmd("start")).bounds(this.width - 80, 8, 72, 16).build());
	}

	private void cmd(String command) {
		ClientPlayNetworking.send(new WonsoPackets.BattleCommandC2S(command));
	}

	private void switchNext() {
		JsonObject state = parse();
		if (state == null || !state.has("party")) {
			return;
		}
		JsonArray party = state.getAsJsonArray("party");
		for (int i = 0; i < party.size(); i++) {
			JsonObject f = party.get(i).getAsJsonObject();
			if (f.has("hp") && f.get("hp").getAsInt() > 0) {
				cmd("switch:" + f.get("z").getAsInt());
				return;
			}
		}
	}

	private JsonObject parse() {
		String raw = WonsoClient.battleJson;
		if (raw == null || raw.isBlank()) {
			return null;
		}
		try {
			return JsonParser.parseString(raw).getAsJsonObject();
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		graphics.text(this.font, Component.literal("⚡ " + (int) WonsoClient.clientEnergy + "   원소 대전 — 플레이어는 명령만"), 8, 8, 0xFFE8C86A);
		JsonObject state = parse();
		if (state == null) {
			graphics.centeredText(this.font, Component.literal("신전을 우클릭하거나 [신전 시작]"), this.width / 2, this.height / 2, 0xFFADB5BD);
			return;
		}
		drawFighter(graphics, state.has("foe") ? state.getAsJsonObject("foe") : null, this.width / 2 + 40, 14, "상대");
		drawFighter(graphics, state.has("mine") ? state.getAsJsonObject("mine") : null, 16, 14, "내 원소령");
		graphics.text(this.font, Component.literal("웨이브 " + state.get("wave").getAsInt() + "/" + state.get("waves").getAsInt()
				+ (state.get("done").getAsBoolean() ? "  " + state.get("result").getAsString() : "")), 8, 88, 0xFF80FFEA);
		int y = 104;
		if (state.has("log")) {
			for (var line : state.getAsJsonArray("log")) {
				graphics.text(this.font, Component.literal(line.getAsString()), 8, y, 0xFFDEE2E6);
				y += 10;
			}
		}
	}

	private void drawFighter(GuiGraphicsExtractor graphics, JsonObject o, int x, int y, String label) {
		if (o == null || !o.has("symbol")) {
			graphics.text(this.font, Component.literal(label + " —"), x, y, 0xFF868E96);
			return;
		}
		String symbol = o.get("symbol").getAsString();
		int hp = o.has("hp") ? o.get("hp").getAsInt() : 0;
		int max = o.has("maxHp") ? Math.max(1, o.get("maxHp").getAsInt()) : 1;
		graphics.fill(x, y, x + 160, y + 68, 0xCC0B1320);
		graphics.text(this.font, Component.literal(label), x + 6, y + 4, 0xFFADB5BD);
		graphics.text(this.font, Component.literal(symbol + "  " + (o.has("nameKo") ? o.get("nameKo").getAsString() : "")),
				x + 6, y + 16, 0xFFFFFFFF);
		int bar = 140 * hp / max;
		graphics.fill(x + 6, y + 32, x + 146, y + 40, 0xFF212529);
		graphics.fill(x + 6, y + 32, x + 6 + bar, y + 40, 0xFF51CF66);
		graphics.text(this.font, Component.literal("HP " + hp + "/" + max + "  " + (o.has("category") ? o.get("category").getAsString() : "")),
				x + 6, y + 46, 0xFFCED4DA);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
