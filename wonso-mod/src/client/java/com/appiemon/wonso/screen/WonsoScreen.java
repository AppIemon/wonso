package com.appiemon.wonso.screen;

import com.appiemon.wonso.client.WonsoGui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class WonsoScreen extends Screen {
	protected WonsoScreen(Component title) {
		super(title);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		WonsoGui.backdrop(graphics, this.width, this.height);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
