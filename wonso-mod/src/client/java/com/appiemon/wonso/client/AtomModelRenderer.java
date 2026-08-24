package com.appiemon.wonso.client;

import com.appiemon.wonso.data.AtomRecord;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class AtomModelRenderer {
	private AtomModelRenderer() {
	}

	public static void render(GuiGraphicsExtractor g, Font font, AtomRecord atom, int cx, int cy) {
		g.fill(cx - 110, cy - 110, cx + 110, cy + 110, 0xCC101820);
		g.fill(cx - 8, cy - 8, cx + 8, cy + 8, 0xFFFF6B6B);
		g.centeredText(font, Component.literal("p⁺" + atom.defaultProtons() + " n⁰" + atom.defaultNeutrons()), cx, cy + 12, 0xFFFFC9C9);

		int[] shells = atom.shellDistribution().stream().mapToInt(Integer::intValue).toArray();
		for (int i = 0; i < shells.length; i++) {
			int r = 28 + i * 22;
			drawCircle(g, cx, cy, r, 0xFF4CC9F0);
			int count = shells[i];
			for (int e = 0; e < count; e++) {
				double ang = (Math.PI * 2 * e) / Math.max(count, 1);
				int ex = cx + (int) (Math.cos(ang) * r);
				int ey = cy + (int) (Math.sin(ang) * r);
				g.fill(ex - 2, ey - 2, ex + 2, ey + 2, 0xFF80FFEA);
			}
		}
		g.centeredText(font, Component.literal("e⁻ " + atom.defaultElectrons() + "  원자가 " + atom.valenceElectrons()), cx, cy + 92, 0xFFB8F2E6);
	}

	private static void drawCircle(GuiGraphicsExtractor g, int cx, int cy, int r, int color) {
		for (int a = 0; a < 360; a += 6) {
			double rad = Math.toRadians(a);
			int x = cx + (int) (Math.cos(rad) * r);
			int y = cy + (int) (Math.sin(rad) * r);
			g.fill(x, y, x + 1, y + 1, color);
		}
	}
}
