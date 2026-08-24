package com.appiemon.wonso.screen;

import com.appiemon.wonso.client.WonsoClient;
import com.appiemon.wonso.client.WonsoGui;
import com.appiemon.wonso.data.WonsoData;
import com.appiemon.wonso.network.WonsoPackets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class BattleScreen extends WonsoScreen {
	private Btn[] actions = new Btn[0];

	public BattleScreen() {
		super(Component.translatable("gui.wonso.battle"));
	}

	@Override
	protected void init() {
		int y = this.height - 22;
		this.actions = new Btn[]{
				new Btn("스킬1", 8, y, 52, () -> cmd("skill:1")),
				new Btn("스킬2", 62, y, 52, () -> cmd("skill:2")),
				new Btn("스킬3", 116, y, 52, () -> cmd("skill:3")),
				new Btn("스킬4", 170, y, 52, () -> cmd("skill:4")),
				new Btn("H₂O", 226, y, 36, () -> cmd("tool:water")),
				new Btn("NaCl", 264, y, 36, () -> cmd("tool:sodium-chloride")),
				new Btn("H₂O₂", 302, y, 40, () -> cmd("tool:hydrogen-peroxide")),
				new Btn("포도당", 344, y, 44, () -> cmd("tool:glucose")),
				new Btn("턴종료", 392, y, 48, () -> cmd("pass")),
				new Btn("도주", 442, y, 36, () -> cmd("flee")),
				new Btn("신전 시작", this.width - 78, 4, 70, () -> cmd("start")),
		};
	}

	private void cmd(String command) {
		ClientPlayNetworking.send(new WonsoPackets.BattleCommandC2S(command));
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
		WonsoGui.header(graphics, this.font, this.title, "⚡" + (int) WonsoClient.clientEnergy + "  ·  플레이어는 명령만", this.width);
		JsonObject state = parse();
		if (state == null) {
			WonsoGui.panel(graphics, this.width / 2 - 140, this.height / 2 - 36, 280, 64);
			graphics.centeredText(this.font, Component.literal("원소 신전을 우클릭하거나"), this.width / 2, this.height / 2 - 16, WonsoGui.MUTED);
			graphics.centeredText(this.font, Component.literal("[신전 시작] 으로 대전"), this.width / 2, this.height / 2, WonsoGui.GOLD);
			drawActions(graphics, mouseX, mouseY);
			return;
		}

		drawFighter(graphics, state.has("mine") ? state.getAsJsonObject("mine") : null, 12, 28, "내 원소령", true);
		drawFighter(graphics, state.has("foe") ? state.getAsJsonObject("foe") : null, this.width / 2 + 8, 28, "상대", false);

		String wave = "웨이브 " + state.get("wave").getAsInt() + "/" + state.get("waves").getAsInt();
		if (state.get("done").getAsBoolean()) {
			wave += "  ·  " + state.get("result").getAsString();
		}
		graphics.text(this.font, Component.literal(wave), 12, 108, WonsoGui.ACCENT);

		if (state.has("party")) {
			JsonArray party = state.getAsJsonArray("party");
			int x = 12;
			for (int i = 0; i < party.size(); i++) {
				JsonObject f = party.get(i).getAsJsonObject();
				int hp = f.has("hp") ? f.get("hp").getAsInt() : 0;
				boolean hover = WonsoGui.hit(mouseX, mouseY, x, 122, 36, 16);
				WonsoGui.button(graphics, this.font, f.get("symbol").getAsString(), x, 122, 36, 16, hover, hp <= 0);
				x += 38;
			}
			graphics.text(this.font, Component.literal("클릭 = 교체"), x + 4, 125, WonsoGui.MUTED);
		}

		WonsoGui.panel(graphics, 12, 144, this.width - 24, Math.max(40, this.height - 178));
		int y = 152;
		if (state.has("log")) {
			for (var line : state.getAsJsonArray("log")) {
				graphics.text(this.font, Component.literal(line.getAsString()), 20, y, WonsoGui.TEXT);
				y += 10;
			}
		}

		if (state.has("mine") && state.getAsJsonObject("mine").has("skills")) {
			JsonArray skills = state.getAsJsonObject("mine").getAsJsonArray("skills");
			int sx = 8;
			for (int i = 0; i < skills.size() && i < 4; i++) {
				JsonObject s = skills.get(i).getAsJsonObject();
				graphics.text(this.font, Component.literal(s.get("name").getAsString() + " ⚡" + s.get("cost").getAsInt()),
						sx, this.height - 36, WonsoGui.MUTED);
				sx += 90;
			}
		}

		drawActions(graphics, mouseX, mouseY);
	}

	private void drawActions(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		for (Btn b : actions) {
			boolean hover = WonsoGui.hit(mouseX, mouseY, b.x, b.y, b.w, 18);
			WonsoGui.button(graphics, this.font, b.label, b.x, b.y, b.w, 18, hover, false);
		}
	}

	private void drawFighter(GuiGraphicsExtractor graphics, JsonObject o, int x, int y, String label, boolean mine) {
		WonsoGui.panel(graphics, x, y, Math.min(220, this.width / 2 - 20), 76);
		if (o == null || !o.has("symbol")) {
			graphics.text(this.font, Component.literal(label + " —"), x + 8, y + 8, WonsoGui.MUTED);
			return;
		}
		String cat = o.has("category") ? o.get("category").getAsString() : "unknown";
		graphics.fill(x + 1, y + 1, x + 7, y + 75, WonsoGui.categoryColor(cat));
		graphics.text(this.font, Component.literal(label), x + 12, y + 6, WonsoGui.MUTED);
		graphics.text(this.font, Component.literal(o.get("symbol").getAsString() + "  "
						+ (o.has("nameKo") ? o.get("nameKo").getAsString() : "")),
				x + 12, y + 18, WonsoGui.TEXT);
		int hp = o.has("hp") ? o.get("hp").getAsInt() : 0;
		int max = o.has("maxHp") ? Math.max(1, o.get("maxHp").getAsInt()) : 1;
		WonsoGui.hpBar(graphics, x + 12, y + 34, 180, hp, max);
		graphics.text(this.font, Component.literal("HP " + hp + "/" + max + "  ATK "
						+ (o.has("attack") ? o.get("attack").getAsInt() : 0)
						+ "  DEF " + (o.has("defense") ? o.get("defense").getAsInt() : 0)),
				x + 12, y + 46, WonsoGui.MUTED);
		graphics.text(this.font, Component.literal(cat), x + 12, y + 58, WonsoGui.ACCENT);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		int mx = (int) event.x();
		int my = (int) event.y();
		for (Btn b : actions) {
			if (WonsoGui.hit(mx, my, b.x, b.y, b.w, 18)) {
				b.run.run();
				return true;
			}
		}
		JsonObject state = parse();
		if (state != null && state.has("party")) {
			JsonArray party = state.getAsJsonArray("party");
			int x = 12;
			for (int i = 0; i < party.size(); i++) {
				if (WonsoGui.hit(mx, my, x, 122, 36, 16)) {
					JsonObject f = party.get(i).getAsJsonObject();
					if (f.has("z")) {
						cmd("switch:" + f.get("z").getAsInt());
					}
					return true;
				}
				x += 38;
			}
		}
		return super.mouseClicked(event, doubled);
	}

	private record Btn(String label, int x, int y, int w, Runnable run) {
	}
}
