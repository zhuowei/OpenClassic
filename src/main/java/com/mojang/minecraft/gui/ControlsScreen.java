package com.mojang.minecraft.gui;

import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ToggleButton;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.util.GeneralUtils;

import com.mojang.minecraft.GameSettings;

public final class ControlsScreen extends GuiScreen {

	private GuiScreen parent;
	private String title = "Controls";
	private GameSettings settings;
	private int binding = -1;

	public ControlsScreen(GuiScreen parent, GameSettings settings) {
		this.parent = parent;
		this.settings = settings;
	}

	public final void onOpen() {
		this.clearWidgets();
		for (int binding = 0; binding < this.settings.bindings.length; binding++) {
			this.attachWidget(new ToggleButton(binding, this.getWidth() / 2 - 155 + binding % 2 * 160, this.getHeight() / 6 + 24 * (binding >> 1), 155, 20, this, true, this.settings.getBinding(binding)));
		}

		this.attachWidget(new Button(200, this.getWidth() / 2 - 100, this.getHeight() / 6 + 168, this, true, "Done"));
	}

	public final void onButtonClick(Button button) {
		for (int binding = 0; binding < this.settings.bindings.length; binding++) {
			this.getWidget(binding, Button.class).setText(this.settings.getBinding(binding));
		}

		if (button.getId() == 200) {
			GeneralUtils.getMinecraft().setCurrentScreen(this.parent);
		} else {
			this.binding = button.getId();
			button.setText("> " + this.settings.getBinding(button.getId()) + " <");
		}
	}

	public final void onKeyPress(char c, int key) {
		if (this.binding >= 0) {
			this.settings.setBinding(this.binding, key);
			this.getWidget(this.binding, Button.class).setText(this.settings.getBinding(this.binding));
			this.binding = -1;
		} else {
			super.onKeyPress(c, key);
		}
	}

	public final void render() {
		if(GeneralUtils.getMinecraft().ingame) {
			RenderHelper.getHelper().color(0, 0, this.getWidth(), this.getHeight(), 1610941696, -1607454624);
		} else {
			RenderHelper.getHelper().drawDirtBG();
		}
		
		RenderHelper.getHelper().renderText(this.title, this.getWidth() / 2, 20);
		super.render();
	}
}
