package com.mojang.minecraft.gui;

import org.lwjgl.input.Keyboard;

import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.util.GeneralUtils;

public final class ErrorScreen extends GuiScreen {

	private String title;
	private String message;

	public ErrorScreen(String title, String message) {
		this.title = title;
		this.message = message;
	}

	public final void onOpen() {
		this.clearWidgets();
		this.attachWidget(new Button(0, this.getWidth() / 2 - 100, this.getHeight() / 6 + 120 + 12, this, "Main Menu"));
	}

	public final void onButtonClick(Button button) {
		if (button.getId() == 0) {
			GeneralUtils.getMinecraft().stopGame(true);
		}
	}

	public final void render() {
		RenderHelper.getHelper().drawDirtBG();
		RenderHelper.getHelper().renderText(this.title, this.getWidth() / 2, 90);
		RenderHelper.getHelper().renderText(this.message, this.getWidth() / 2, 110);
		super.render();
	}

	public final void onKeyPress(char c, int key) {
		if(key != Keyboard.KEY_ESCAPE) {
			super.onKeyPress(c, key);
		}
	}
}
