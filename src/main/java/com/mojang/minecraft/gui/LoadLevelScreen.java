package com.mojang.minecraft.gui;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ButtonList;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.gui.ConfirmDeleteScreen;
import ch.spacebase.openclassic.client.util.GeneralUtils;

import com.mojang.minecraft.level.Level;
import java.io.File;
import java.util.Arrays;

public class LoadLevelScreen extends GuiScreen implements Runnable {

	private GuiScreen parent;
	private boolean finished = false;
	private boolean loaded = false;
	private String[] levels = null;
	private String text = "";
	private String title = "Load level";
	public boolean levelSelected = false;
	
	private boolean delete = false;

	public LoadLevelScreen(GuiScreen parent) {
		this.parent = parent;
	}

	public void run() {
		try {
			if (this.levelSelected) {
				try {
					Thread.sleep(100L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			this.text = "Getting level list..";
			
			String levels = "";
			for(String file : (new File(GeneralUtils.getMinecraft().dir, "levels").list())) {
				if(!file.endsWith(".map") && !file.endsWith(".mine") && !file.endsWith(".mclevel") && !file.endsWith(".oclvl") && !file.endsWith(".dat") && !file.endsWith(".lvl")) continue;
				if(!levels.equals("")) levels += ";";
				levels += file.substring(0, file.indexOf("."));
			}
			
			this.levels = levels.split(";");
			this.getWidget(0, ButtonList.class).setContents(Arrays.asList(this.levels));
 
			this.text = "";
			this.loaded = true;
		} catch (Exception e) {
			e.printStackTrace();
			this.text = "Failed to load levels";
		}
		
		this.finished = true;
	}

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(new ButtonList(0, this.getWidth(), this.getHeight(), this, true));
		
		(new Thread(this)).start();

		this.attachWidget(new Button(1, this.getWidth() / 2 - 156, this.getHeight() / 6 + 144, 100, 20, this, true, "New Level"));
		this.attachWidget(new Button(2, this.getWidth() / 2 - 52, this.getHeight() / 6 + 144, 100, 20, this, true, "Delete Level"));
		this.attachWidget(new Button(3, this.getWidth() / 2 + 52, this.getHeight() / 6 + 144, 100, 20, this, true, "Back to Menu"));
	}

	public final void onButtonClick(Button button) {
			if (button.isActive() && !this.levelSelected) {
				if((this.finished || this.loaded) && button.getId() == 1) {
					GeneralUtils.getMinecraft().setCurrentScreen(new LevelNameScreen(this));
				}
				
				if((this.finished || this.loaded) && button.getId() == 2) {
					if(this.delete) {
						this.title = "Load level";
						this.delete = false;
					} else {
						this.title = Color.RED + "Select a level to delete.";
						this.delete = true;
					}
				}

				if ((this.finished || this.loaded) && button.getId() == 3) {
					GeneralUtils.getMinecraft().setCurrentScreen(this.parent);
				}
			}
	}
	
	@Override
	public void onButtonListClick(ButtonList list, Button button) {
		if(button.isActive() && !this.levelSelected && this.loaded) {
			if(this.delete) {
				File file = null;
				for(File f : (new File(GeneralUtils.getMinecraft().dir, "levels")).listFiles()) {
					if(f != null && f.getName().equals(button.getText() + ".mine") || f.getName().equals(button.getText() + ".map") || f.getName().equals(button.getText() + ".oclvl") || f.getName().equals(button.getText() + ".lvl") || f.getName().equals(button.getText() + ".dat") || f.getName().equals(button.getText() + ".mclevel")) {
						file = f;
						break;
					}
				}
				
				if(file == null) return;
				
				GeneralUtils.getMinecraft().setCurrentScreen(new ConfirmDeleteScreen(this, file));
				this.delete = false;
				this.title = "Load level";
			} else {
				this.loadLevel(button.getId());
			}
		}
	}

	protected void loadLevel(int id) {
		Level level = GeneralUtils.getMinecraft().levelIo.load(this.getWidget(0, ButtonList.class).getButton(id).getText());
		if (level != null) {
			GeneralUtils.getMinecraft().setLevel(level);
			GeneralUtils.getMinecraft().initGame();
			GeneralUtils.getMinecraft().setCurrentScreen(null);
		}
	}

	public void render() {
		RenderHelper.getHelper().drawDirtBG();
		RenderHelper.getHelper().renderText(this.title, this.getWidth() / 2, 15, 16777215);
		
		if (this.levelSelected) {
			RenderHelper.getHelper().renderText("Selecting file..", this.getWidth() / 2, this.getHeight() / 2 - 4);

			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			if (!this.loaded) {
				RenderHelper.getHelper().renderText(this.text, this.getWidth() / 2, this.getHeight() / 2 - 4);
			}

			super.render();
		}
	}
}
