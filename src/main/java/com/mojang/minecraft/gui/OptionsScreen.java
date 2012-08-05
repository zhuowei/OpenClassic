package com.mojang.minecraft.gui;

import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.StateButton;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.gui.HacksScreen;
import ch.spacebase.openclassic.client.util.GeneralUtils;

import com.mojang.minecraft.GameSettings;
import com.mojang.minecraft.gui.ControlsScreen;

public final class OptionsScreen extends GuiScreen {

	private GuiScreen parent;
	private String title = "Options";
	private GameSettings settings;

	public OptionsScreen(GuiScreen parent, GameSettings settings) {
		this.parent = parent;
		this.settings = settings;
	}

	public final void onOpen() {
		this.clearWidgets();
		for (int count = 0; count < this.settings.count; count++) {
			this.attachWidget(new StateButton(count, this.getWidth() / 2 - 155 + count % 2 * 160, this.getHeight() / 6 + 24 * (count >> 1), 155, 20, this, this.settings.getSettingName(count)));
			this.getWidget(count, StateButton.class).setState(this.settings.getSettingValue(count));
		}
		
		this.attachWidget(new Button(75, this.getWidth() / 2 - 100, this.getHeight() / 6 + 124, this, "Hacks..."));
		this.attachWidget(new Button(100, this.getWidth() / 2 - 100, this.getHeight() / 6 + 148, this, "Controls..."));
		this.attachWidget(new Button(200, this.getWidth() / 2 - 100, this.getHeight() / 6 + 172, this, "Done"));
		
		if(GeneralUtils.getMinecraft().netManager != null) {
			this.getWidget(8, Button.class).setActive(false);
		}
		
		if(GeneralUtils.getMinecraft().mipmapMode == 0) {
			this.getWidget(9, Button.class).setActive(false);
		}
		
		if(!GeneralUtils.getMinecraft().hacks || !GeneralUtils.getMinecraft().ingame) {
			this.getWidget(75, Button.class).setActive(false);
		}
	}
	
	public void update() {
		Button button = this.getWidget(8, Button.class);
		if(button.isActive() && GeneralUtils.getMinecraft().netManager != null) {
			button.setActive(false);
		}
		
		if(!button.isActive() && GeneralUtils.getMinecraft().netManager == null) {
			button.setActive(true);
		}
		
		super.update();
	}

	public final void onButtonClick(Button button) {
		if (button.isActive()) {
			if (button.getId() < 75) {
				this.settings.toggleSetting(button.getId(), 1);
				((StateButton) button).setState(this.settings.getSettingValue(button.getId()));
			}

			if (button.getId() == 75) {
				GeneralUtils.getMinecraft().setCurrentScreen(new HacksScreen(this, this.settings));
			}
			
			if (button.getId() == 100) {
				GeneralUtils.getMinecraft().setCurrentScreen(new ControlsScreen(this, this.settings));
			}

			if (button.getId() == 200) {
				GeneralUtils.getMinecraft().setCurrentScreen(this.parent);
			}
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
