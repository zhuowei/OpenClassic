package com.mojang.minecraft.gui;

import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.StateButton;
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
			this.attachWidget(new StateButton(binding, this.getWidth() / 2 - 155 + binding % 2 * 160, this.getHeight() / 6 + 24 * (binding >> 1), 155, 20, this, this.settings.getBindingName(binding)));
			this.getWidget(binding, StateButton.class).setState(this.settings.getBindingValue(binding));
		}

		this.attachWidget(new Button(200, this.getWidth() / 2 - 100, this.getHeight() / 6 + 168, this, "Done"));
	}

	public final void onButtonClick(Button button) {
		for (int binding = 0; binding < this.settings.bindings.length; binding++) {
			this.getWidget(binding, StateButton.class).setState(this.settings.getBindingValue(binding));
		}

		if (button.getId() == 200) {
			GeneralUtils.getMinecraft().setCurrentScreen(this.parent);
		} else {
			this.binding = button.getId();
			button.setText("> " + this.settings.getBindingName(button.getId()));
			this.getWidget(binding, StateButton.class).setState(this.settings.getBindingValue(binding) + " <");
		}
	}

	public final void onKeyPress(char c, int key) {
		if (this.binding >= 0) {
			this.settings.setBinding(this.binding, key);
			this.getWidget(this.binding, Button.class).setText(this.settings.getBindingName(this.binding));
			this.getWidget(this.binding, StateButton.class).setState(this.settings.getBindingValue(this.binding));
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
