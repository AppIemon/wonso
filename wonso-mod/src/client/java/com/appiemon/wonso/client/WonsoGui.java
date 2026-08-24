package com.appiemon.wonso.client;

import com.appiemon.wonso.data.ElementRecord;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class WonsoGui {
	private WonsoGui() {
	}

	public static final int BG = 0xF2080C14;
	public static final int HEADER = 0xF00B1320;
	public static final int PANEL = 0xE0141C2A;
	public static final int PANEL_INNER = 0xE00E1622;
	public static final int GOLD = 0xFFE8C86A;
	public static final int MUTED = 0xFF8B95A3;
	public static final int TEXT = 0xFFE9ECEF;
	public static final int ACCENT = 0xFF80FFEA;
	public static final int DANGER = 0xFFE03131;
	public static final int OK = 0xFF51CF66;
	public static final int BTN = 0xFF1A2433;
	public static final int BTN_HOVER = 0xFF2A3A4E;
	public static final int BTN_ON = 0xFF3D5A73;

	public static int categoryColor(String category) {
		if (category == null) {
			return 0xFF4A5560;
		}
		return switch (category) {
			case "alkali_metal" -> 0xFFB85C38;
			case "alkaline_earth_metal" -> 0xFFC9A227;
			case "transition_metal" -> 0xFF3D7EA6;
			case "post_transition_metal" -> 0xFF5C6B73;
			case "metalloid" -> 0xFF2A9D8F;
			case "reactive_nonmetal" -> 0xFF40916C;
			case "halogen" -> 0xFF9B5DE5;
			case "noble_gas" -> 0xFF48CAE4;
			case "lanthanide" -> 0xFFE07A5F;
			case "actinide" -> 0xFF9B2226;
			default -> 0xFF4A5560;
		};
	}

	public static void backdrop(GuiGraphicsExtractor graphics, int width, int height) {
		graphics.fill(0, 0, width, height, BG);
		graphics.fill(0, 0, width, 22, HEADER);
		graphics.fill(0, height - 26, width, height, HEADER);
		graphics.fill(0, 22, width, 23, GOLD);
		graphics.fill(0, height - 26, width, height - 25, 0x663D5A73);
	}

	public static void header(GuiGraphicsExtractor graphics, Font font, Component title, String right, int width) {
		graphics.text(font, title, 10, 7, GOLD);
		if (right != null && !right.isBlank()) {
			graphics.text(font, Component.literal(right), 160, 7, ACCENT);
		}
	}

	public static void panel(GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
		graphics.fill(x, y, x + w, y + h, PANEL);
		graphics.fill(x, y, x + w, y + 1, GOLD);
		graphics.fill(x, y, x + 1, y + h, 0x66E8C86A);
	}

	public static void inner(GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
		graphics.fill(x, y, x + w, y + h, PANEL_INNER);
	}

	public static void cell(GuiGraphicsExtractor graphics, Font font, ElementRecord el, int x, int y, int size, boolean selected) {
		int color = categoryColor(el.category());
		graphics.fill(x, y, x + size, y + size, color);
		if (selected) {
			graphics.fill(x, y, x + size, y + 1, GOLD);
			graphics.fill(x, y + size - 1, x + size, y + size, GOLD);
			graphics.fill(x, y, x + 1, y + size, GOLD);
			graphics.fill(x + size - 1, y, x + size, y + size, GOLD);
		}
		graphics.centeredText(font, Component.literal(el.symbol()), x + size / 2, y + Math.max(1, size / 2 - 4), 0xFFFFFFFF);
	}

	public static void hpBar(GuiGraphicsExtractor graphics, int x, int y, int w, int hp, int max) {
		int m = Math.max(1, max);
		int fill = Math.max(0, w * Math.max(0, hp) / m);
		graphics.fill(x, y, x + w, y + 7, 0xFF212529);
		graphics.fill(x, y, x + fill, y + 7, hp > m / 4 ? OK : DANGER);
	}

	public static void button(GuiGraphicsExtractor graphics, Font font, String label, int x, int y, int w, int h, boolean hover, boolean selected) {
		int fill = selected ? BTN_ON : (hover ? BTN_HOVER : BTN);
		graphics.fill(x, y, x + w, y + h, fill);
		graphics.fill(x, y, x + w, y + 1, selected || hover ? GOLD : 0xFF3D5A73);
		graphics.centeredText(font, Component.literal(label), x + w / 2, y + Math.max(2, h / 2 - 4), selected ? GOLD : TEXT);
	}

	public static boolean hit(double mx, double my, int x, int y, int w, int h) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}
}
