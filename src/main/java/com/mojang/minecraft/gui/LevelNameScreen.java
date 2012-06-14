package com.mojang.minecraft.gui;

import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.TextBox;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.util.GeneralUtils;

import com.mojang.minecraft.Minecraft;

import org.lwjgl.input.Keyboard;

public final class LevelNameScreen extends GuiScreen {

	private GuiScreen parent;
	private TextBox widget;

	public LevelNameScreen(GuiScreen parent) {
		this.parent = parent;
	}

	public final void onOpen() {
		Keyboard.enableRepeatEvents(true);
		
		this.widget = new TextBox(0, this.getWidth() / 2 - 100, this.getHeight() / 2 - 30, this, true, true, 30);
		
		this.clearWidgets();
		this.attachWidget(new Button(0, this.getWidth() / 2 - 100, this.getHeight() / 4 + 72, this, true, "Small"));
		this.attachWidget(new Button(1, this.getWidth() / 2 - 100, this.getHeight() / 4 + 96, this, true, "Normal"));
		this.attachWidget(new Button(2, this.getWidth() / 2 - 100, this.getHeight() / 4 + 120, this, true, "Huge"));
		this.attachWidget(new Button(3, this.getWidth() / 2 - 100, this.getHeight() / 4 + 144, this, true, "Cancel"));
		this.attachWidget(this.widget);
		
		this.getWidget(0, Button.class).setActive(false);
		this.getWidget(1, Button.class).setActive(false);
		this.getWidget(2, Button.class).setActive(false);
	}

	public final void onClose() {
		Keyboard.enableRepeatEvents(false);
	}

	public final void onButtonClick(Button button) {
		if (button.isActive()) {
			Minecraft mc = GeneralUtils.getMinecraft();
			
			if ((button.getId() == 0 || button.getId() == 1 || button.getId() == 2) && this.widget.getText().trim().length() > 0) {
				mc.levelName = this.widget.getText();
				mc.levelSize = button.getId();
				mc.initGame();
				mc.levelIo.save(mc.level);
				mc.setCurrentScreen(null);
				mc.grabMouse();
			}

			if (button.getId() == 3) {
				mc.setCurrentScreen(this.parent);
			}
		}
	}

	public final void onKeyPress(char c, int key) {
		super.onKeyPress(c, key);
		this.getWidget(0, Button.class).setActive(this.widget.getText().trim().length() > 0);
		this.getWidget(1, Button.class).setActive(this.widget.getText().trim().length() > 0);
		this.getWidget(2, Button.class).setActive(this.widget.getText().trim().length() > 0);
	}

	public final void render() {
		RenderHelper.getHelper().drawDirtBG();
		RenderHelper.getHelper().renderText("Enter level name:", this.getWidth() / 2, 40);

		super.render();
	}
}
