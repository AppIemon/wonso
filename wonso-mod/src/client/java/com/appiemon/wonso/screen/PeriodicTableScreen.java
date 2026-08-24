package com.appiemon.wonso.screen;

import com.appiemon.wonso.client.AtomModelRenderer;
import com.appiemon.wonso.client.WonsoClient;
import com.appiemon.wonso.client.WonsoGui;
import com.appiemon.wonso.data.ElementRecord;
import com.appiemon.wonso.data.MoleculeRecord;
import com.appiemon.wonso.data.WonsoData;
import com.appiemon.wonso.network.WonsoPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class PeriodicTableScreen extends WonsoScreen {
	private static final String[][] LEGEND = {
			{"alkali_metal", "알칼리"},
			{"alkaline_earth_metal", "알칼리토"},
			{"transition_metal", "전이금속"},
			{"post_transition_metal", "전이후"},
			{"metalloid", "준금속"},
			{"reactive_nonmetal", "비금속"},
			{"halogen", "할로젠"},
			{"noble_gas", "비활성"},
			{"lanthanide", "란타넘"},
			{"actinide", "악티늄"},
	};

	private int cell = 18;
	private int startX;
	private int startY;
	private Nav[] nav = new Nav[0];

	public PeriodicTableScreen() {
		super(Component.translatable("gui.wonso.periodic_table"));
	}

	@Override
	protected void init() {
		this.cell = Math.max(14, Math.min(22, (this.width - 120) / 18));
		this.startX = 10;
		this.startY = 28;
		int y = this.height - 22;
		int x = 8;
		this.nav = new Nav[]{
				tab("원소", x, y, 40, () -> WonsoClient.mode = "element"),
				tab("원자", x += 42, y, 40, () -> {
					WonsoClient.mode = "atom";
					ClientPlayNetworking.send(new WonsoPackets.OpenUiC2S("atom"));
				}),
				tab("분자", x += 42, y, 40, () -> WonsoClient.mode = "molecule"),
				tab("도감", x += 42, y, 40, () -> this.minecraft.gui.setScreen(new MoleculeDexScreen())),
				tab("합성", x += 42, y, 40, () -> this.minecraft.gui.setScreen(new SynthesisLabScreen())),
				tab("에너지", x += 42, y, 48, () -> this.minecraft.gui.setScreen(new EnergyPanelScreen())),
				tab("파티", x += 50, y, 40, () -> this.minecraft.gui.setScreen(new PartyScreen())),
				tab("대전", x += 42, y, 40, () -> this.minecraft.gui.setScreen(new BattleScreen())),
				tab("정제", x += 42, y, 40, () -> this.minecraft.gui.setScreen(new RefineScreen("element_refinery"))),
				tab("융합", x += 42, y, 40, () -> this.minecraft.gui.setScreen(new FusionScreen())),
				tab("안내", x + 42, y, 40, () -> this.minecraft.gui.setScreen(new GuideScreen())),
		};
	}

	private static Nav tab(String label, int x, int y, int w, Runnable run) {
		return new Nav(label, x, y, w, run);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		WonsoGui.header(graphics, this.font, this.title,
				"⚡" + (int) WonsoClient.clientEnergy + "  ·  " + modeLabel()
						+ "  ·  원소 " + WonsoClient.discoveredElements + "/118",
				this.width);

		for (ElementRecord el : WonsoData.elements()) {
			int x = startX + (el.displayColumn() - 1) * cell;
			int y = startY + (el.displayRow() - 1) * cell;
			WonsoGui.cell(graphics, this.font, el, x, y, cell - 1, el.z() == WonsoClient.selectedZ);
		}

		int lx = startX + cell * 18 + 8;
		int ly = startY;
		graphics.text(this.font, Component.literal("족"), lx, ly, WonsoGui.MUTED);
		ly += 12;
		for (String[] row : LEGEND) {
			int color = WonsoGui.categoryColor(row[0]);
			graphics.fill(lx, ly, lx + 8, ly + 8, color);
			graphics.text(this.font, Component.literal(row[1]), lx + 12, ly, WonsoGui.TEXT);
			ly += 11;
		}

		ElementRecord el = WonsoData.byZ(WonsoClient.selectedZ);
		if (el != null) {
			int panelY = this.height - 118;
			WonsoGui.panel(graphics, 8, panelY, Math.min(420, this.width - 16), 88);
			graphics.text(this.font, Component.literal(el.symbol() + "  " + el.nameKo() + " / " + el.nameEn() + "  Z=" + el.z()),
					16, panelY + 8, WonsoGui.TEXT);
			graphics.text(this.font, Component.literal(el.category() + "  ·  " + el.stateAtRoomTemp()),
					16, panelY + 20, WonsoGui.MUTED);
			if ("atom".equals(WonsoClient.mode)) {
				var atom = WonsoData.atom(el.z());
				if (atom != null) {
					AtomModelRenderer.render(graphics, this.font, atom, this.width / 2 + 80, startY + cell * 4);
				}
			} else if ("molecule".equals(WonsoClient.mode)) {
				int y = panelY + 34;
				for (String id : el.relatedMoleculeIds()) {
					MoleculeRecord m = WonsoData.molecule(id);
					if (m == null) {
						continue;
					}
					graphics.text(this.font, Component.literal(m.formulaDisplay() + "  " + m.nameKo()), 16, y, WonsoGui.ACCENT);
					y += 10;
					if (y > this.height - 40) {
						break;
					}
				}
			} else {
				graphics.textWithWordWrap(this.font, Component.literal(el.descriptionKo() == null ? "" : el.descriptionKo()),
						16, panelY + 34, Math.min(400, this.width - 40), WonsoGui.MUTED);
			}
		}

		for (Nav n : nav) {
			boolean hover = WonsoGui.hit(mouseX, mouseY, n.x, n.y, n.w, 18);
			boolean on = switch (n.label) {
				case "원소" -> "element".equals(WonsoClient.mode);
				case "원자" -> "atom".equals(WonsoClient.mode);
				case "분자" -> "molecule".equals(WonsoClient.mode);
				default -> false;
			};
			WonsoGui.button(graphics, this.font, n.label, n.x, n.y, n.w, 18, hover, on);
		}
	}

	private static String modeLabel() {
		return switch (WonsoClient.mode) {
			case "atom" -> "원자 모드";
			case "molecule" -> "분자 모드";
			default -> "원소 모드";
		};
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		int mx = (int) event.x();
		int my = (int) event.y();
		for (ElementRecord el : WonsoData.elements()) {
			int x = startX + (el.displayColumn() - 1) * cell;
			int y = startY + (el.displayRow() - 1) * cell;
			if (WonsoGui.hit(mx, my, x, y, cell - 1, cell - 1)) {
				WonsoClient.selectedZ = el.z();
				return true;
			}
		}
		for (Nav n : nav) {
			if (WonsoGui.hit(mx, my, n.x, n.y, n.w, 18)) {
				n.run.run();
				return true;
			}
		}
		return super.mouseClicked(event, doubled);
	}

	private record Nav(String label, int x, int y, int w, Runnable run) {
	}
}
