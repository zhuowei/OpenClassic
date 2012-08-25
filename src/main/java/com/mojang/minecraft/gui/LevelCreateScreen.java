package com.mojang.minecraft.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.TextBox;
import ch.spacebase.openclassic.api.gui.widget.StateButton;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.util.GeneralUtils;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.level.LevelIO;

import org.lwjgl.input.Keyboard;

public final class LevelCreateScreen extends GuiScreen {

	private GuiScreen parent;
	private TextBox widget;
	private int type = 0;

	public LevelCreateScreen(GuiScreen parent) {
		this.parent = parent;
	}

	public final void onOpen() {
		Keyboard.enableRepeatEvents(true);
		
		this.widget = new TextBox(0, this.getWidth() / 2 - 100, this.getHeight() / 2 - 45, this, 30);
		
		this.clearWidgets();
		this.attachWidget(new StateButton(0, this.getWidth() / 2 - 100, this.getHeight() / 4 + 48, this, OpenClassic.getGame().getTranslator().translate("gui.level-create.type")));
		this.getWidget(0, StateButton.class).setState("normal");
		this.attachWidget(new Button(1, this.getWidth() / 2 - 100, this.getHeight() / 4 + 72, this, OpenClassic.getGame().getTranslator().translate("gui.level-create.small")));
		this.attachWidget(new Button(2, this.getWidth() / 2 - 100, this.getHeight() / 4 + 96, this, OpenClassic.getGame().getTranslator().translate("gui.level-create.normal")));
		this.attachWidget(new Button(3, this.getWidth() / 2 - 100, this.getHeight() / 4 + 120, this, OpenClassic.getGame().getTranslator().translate("gui.level-create.huge")));
		this.attachWidget(new Button(4, this.getWidth() / 2 - 100, this.getHeight() / 4 + 144, this, OpenClassic.getGame().getTranslator().translate("gui.cancel")));
		this.attachWidget(this.widget);
		
		this.getWidget(1, Button.class).setActive(false);
		this.getWidget(2, Button.class).setActive(false);
		this.getWidget(3, Button.class).setActive(false);
	}

	public final void onClose() {
		Keyboard.enableRepeatEvents(false);
	}

	public final void onButtonClick(Button button) {
		if (button.isActive()) {
			Minecraft mc = GeneralUtils.getMinecraft();
			
			if(button.getId() == 0) {
				this.type++;
				if(this.type >= OpenClassic.getGame().getGenerators().size()) {
					this.type = 0;
				}
				
				((StateButton) button).setState(OpenClassic.getGame().getGenerators().keySet().toArray(new String[OpenClassic.getGame().getGenerators().keySet().size()])[this.type]);
			}
			
			if ((button.getId() == 1 || button.getId() == 2 || button.getId() == 3) && this.widget.getText().trim().length() > 0) {
				mc.levelName = this.widget.getText();
				mc.levelSize = button.getId() - 1;
				mc.initGame(OpenClassic.getGame().getGenerator(this.getWidget(0, StateButton.class).getState()));
				LevelIO.save(mc.level);
				mc.setCurrentScreen(null);
				mc.grabMouse();
			}

			if (button.getId() == 4) {
				mc.setCurrentScreen(this.parent);
			}
		}
	}

	public final void onKeyPress(char c, int key) {
		super.onKeyPress(c, key);
		this.getWidget(1, Button.class).setActive(this.widget.getText().trim().length() > 0);
		this.getWidget(2, Button.class).setActive(this.widget.getText().trim().length() > 0);
		this.getWidget(3, Button.class).setActive(this.widget.getText().trim().length() > 0);
	}

	public final void render() {
		RenderHelper.getHelper().drawDirtBG();
		RenderHelper.getHelper().renderText(OpenClassic.getGame().getTranslator().translate("gui.level-create.name"), this.getWidth() / 2, 40);

		super.render();
	}
}
