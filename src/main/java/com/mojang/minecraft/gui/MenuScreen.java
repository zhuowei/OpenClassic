package com.mojang.minecraft.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.event.EventFactory;
import ch.spacebase.openclassic.api.event.level.LevelUnloadEvent;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.util.GeneralUtils;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.gui.OptionsScreen;

public final class MenuScreen extends GuiScreen {

	public final void onOpen() {
		this.clearWidgets();
		this.attachWidget(new Button(0, this.getWidth() / 2 - 100, this.getHeight() / 2 - 48, this, "Options..."));
		this.attachWidget(new Button(1, this.getWidth() / 2 - 100, this.getHeight() / 2 - 24, this, "Dump Level"));
		this.attachWidget(new Button(2, this.getWidth() / 2 - 100, this.getHeight() / 2, this, "Main Menu"));
		this.attachWidget(new Button(3, this.getWidth() / 2 - 100, this.getHeight() / 2 + 60, this, "Back to game"));
	
		if(GeneralUtils.getMinecraft().netManager == null) {
			this.getWidget(1, Button.class).setActive(false);
		}
	}

	public final void onButtonClick(Button button) {
		Minecraft mc = GeneralUtils.getMinecraft();
		
		if (button.getId() == 0) {
			mc.setCurrentScreen(new OptionsScreen(this, mc.settings));
		}
		
		if(button.getId() == 1) {
			mc.setCurrentScreen(new LevelDumpScreen(this));
		}

		if (button.getId() == 2) {
			if(mc.netManager == null) {
				if(EventFactory.callEvent(new LevelUnloadEvent(OpenClassic.getClient().getLevel())).isCancelled()) {
					return;
				}
				
				mc.progressBar.setTitle("Saving level...");
				mc.progressBar.setText("");
				mc.progressBar.setProgress(0);
				mc.levelIo.save(mc.level);
			}
			
			mc.stopGame(true);
		}
		
		if (button.getId() == 3) {
			mc.setCurrentScreen(null);
			mc.grabMouse();
		}
	}

	public final void render() {
		RenderHelper.getHelper().color(0, 0, this.getWidth(), this.getHeight(), 1610941696, -1607454624);
		RenderHelper.getHelper().renderText("Game menu", this.getWidth() / 2, 40);
		super.render();
	}
}
