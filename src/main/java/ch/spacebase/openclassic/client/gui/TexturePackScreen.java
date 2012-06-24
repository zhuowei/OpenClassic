package ch.spacebase.openclassic.client.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ButtonList;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.util.GeneralUtils;

import com.mojang.minecraft.render.TextureManager;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

public class TexturePackScreen extends GuiScreen {

	private GuiScreen parent;
	private String[] textures = null;

	public TexturePackScreen(GuiScreen parent) {
		this.parent = parent;
	}

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(new ButtonList(0, this.getWidth(), this.getHeight(), this, true));
		this.attachWidget(new Button(1, this.getWidth() / 2 - 75, this.getHeight() / 6 + 156, 150, 20, this, true, "Back to Menu"));
	
		String textures = "Default";
		for(String file : (new File(OpenClassic.getClient().getDirectory(), "texturepacks").list())) {
			if(!file.endsWith(".zip")) continue;
			textures += ";";
			textures += file.substring(0, file.indexOf("."));
		}
		
		this.textures = textures.split(";");
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
			
			// TODO: Textures won't rebind properly
			TextureManager textureManager = GeneralUtils.getMinecraft().textureManager;
			Iterator<Integer> itr = textureManager.textureImgs.keySet().iterator();
			
			while (itr.hasNext()) {
				int tex = itr.next().intValue();
				textureManager.bindTexture(textureManager.textureImgs.get(Integer.valueOf(tex)), tex);
			}

			Iterator<String> iter = textureManager.textures.keySet().iterator();

			while (itr.hasNext()) {
				String texture = iter.next();

				try {
					BufferedImage img = null;
					if(!textureManager.jarTexture.get(texture)) {
						img = ImageIO.read(new FileInputStream(texture));
					} else {
						if(GeneralUtils.getMinecraft().settings.texturePack.equals("none")) {
							img = ImageIO.read(TextureManager.class.getResourceAsStream(texture));
						} else {
							ZipFile zip = new ZipFile(new File(OpenClassic.getClient().getDirectory(), "texturepacks/" + GeneralUtils.getMinecraft().settings.texturePack));
							if(zip.getEntry(texture.startsWith("/") ? texture.substring(1, texture.length()) : texture) != null) {
								img = ImageIO.read(zip.getInputStream(zip.getEntry(texture.startsWith("/") ? texture.substring(1, texture.length()) : texture)));
							} else {
								img = ImageIO.read(TextureManager.class.getResourceAsStream(texture));
							}
							
							zip.close();
						}
					}
					
					textureManager.bindTexture(img, textureManager.textures.get(texture));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void render() {
		RenderHelper.getHelper().drawDirtBG();
		RenderHelper.getHelper().renderText("Select a texture pack", this.getWidth() / 2, 15, 16777215);
		RenderHelper.getHelper().renderText("Current texture pack: " + (!GeneralUtils.getMinecraft().settings.texturePack.equals("none") ? GeneralUtils.getMinecraft().settings.texturePack.substring(0, GeneralUtils.getMinecraft().settings.texturePack.indexOf('.')) : "Default"), this.getWidth() / 2, this.getHeight() / 2 + 48, 16777215);
		super.render();
	}
}
