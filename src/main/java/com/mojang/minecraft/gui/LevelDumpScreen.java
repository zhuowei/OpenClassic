package com.mojang.minecraft.gui;

import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.TextBox;
import ch.spacebase.openclassic.api.gui.widget.ToggleButton;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.util.GeneralUtils;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.level.LevelIO;

import org.lwjgl.input.Keyboard;

public final class LevelDumpScreen extends GuiScreen {

	private GuiScreen parent;
	private TextBox widget;

	public LevelDumpScreen(GuiScreen parent) {
		this.parent = parent;
	}

	public final void onOpen() {
		Keyboard.enableRepeatEvents(true);
		
		this.widget = new TextBox(0, this.getWidth() / 2 - 100, this.getHeight() / 2 - 30, this, true, true, 30);
		
		this.clearWidgets();
		this.attachWidget(this.widget);
		this.attachWidget(new ToggleButton(1, this.getWidth() / 2 - 100, this.getHeight() / 4 + 96, this, true, "Format: OpenClassic"));
		this.attachWidget(new Button(2, this.getWidth() / 2 - 100, this.getHeight() / 4 + 120, this, true, "Dump"));
		this.attachWidget(new Button(3, this.getWidth() / 2 - 100, this.getHeight() / 4 + 144, this, true, "Cancel"));
		
		this.getWidget(2, Button.class).setActive(false);
	}

	public final void onClose() {
		Keyboard.enableRepeatEvents(false);
	}

	public final void onButtonClick(Button button) {
		if (button.isActive()) {
			Minecraft mc = GeneralUtils.getMinecraft();
			
			if(button.getId() == 1) {
				button.setText(button.getText().contains("Minecraft") ? "Format: OpenClassic" : "Format: Minecraft");
			}
			
			if (button.getId() == 2 && this.widget.getText().trim().length() > 0) {
				mc.level.name = this.widget.getText();
				if(this.getWidget(1, Button.class).getText().contains("Minecraft")) {
					LevelIO.saveOld(mc.level);
				} else {
					mc.levelIo.save(mc.level);
				}
				
				mc.setCurrentScreen(this.parent);
			}

			if (button.getId() == 3) {
				mc.setCurrentScreen(this.parent);
			}
		}
	}

	public final void onKeyPress(char c, int key) {
		super.onKeyPress(c, key);
		this.getWidget(2, Button.class).setActive(this.widget.getText().trim().length() > 0);
	}

	public final void render() {
		RenderHelper.getHelper().drawDirtBG();
		RenderHelper.getHelper().renderText("Enter level name:", this.getWidth() / 2, 40);

		super.render();
	}
}
