package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ButtonList;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.util.GeneralUtils;

import java.io.File;
import java.util.Arrays;

public class TexturePackScreen extends GuiScreen {

	private GuiScreen parent;
	private String[] textures = null;

	public TexturePackScreen(GuiScreen parent) {
		this.parent = parent;
	}

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(new ButtonList(0, this.getWidth(), this.getHeight(), this));
		this.attachWidget(new Button(1, this.getWidth() / 2 - 75, this.getHeight() / 6 + 156, 150, 20, this, "Back to Menu"));
	
		StringBuilder textures = new StringBuilder("Default");
		for(String file : (new File(OpenClassic.getClient().getDirectory(), "texturepacks").list())) {
			if(!file.endsWith(".zip")) continue;
			textures.append(";").append(file.substring(0, file.indexOf(".")));
		}
		
		this.textures = textures.toString().split(";");
		this.getWidget(0, ButtonList.class).setContents(Arrays.asList(this.textures));
	}

	public final void onButtonClick(Button button) {
		if (button.isActive()) {
			if (button.getId() == 1) {
				GeneralUtils.getMinecraft().setCurrentScreen(this.parent);
			}
		}
	}
	
	@Override
	public void onButtonListClick(ButtonList list, Button button) {
		if(button.isActive()) {
			if(button.getText().equals("Default")) {
				GeneralUtils.getMinecraft().settings.texturePack = "none";
			} else {
				GeneralUtils.getMinecraft().settings.texturePack = button.getText() + ".zip";
			}
			
			GeneralUtils.getMinecraft().settings.save();
			GeneralUtils.getMinecraft().textureManager.clear();
		}
	}

	public void render() {
		RenderHelper.getHelper().drawDirtBG();
		RenderHelper.getHelper().renderText("Select a texture pack", this.getWidth() / 2, 15, 16777215);
		RenderHelper.getHelper().renderText("Current texture pack: " + (!GeneralUtils.getMinecraft().settings.texturePack.equals("none") ? GeneralUtils.getMinecraft().settings.texturePack.substring(0, GeneralUtils.getMinecraft().settings.texturePack.indexOf('.')) : "Default"), this.getWidth() / 2, this.getHeight() / 2 + 48, 16777215);
		super.render();
	}
}
