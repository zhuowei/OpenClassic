package com.mojang.minecraft;

import ch.spacebase.openclassic.api.OpenClassic;

import com.mojang.minecraft.KeyBinding;
import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.gamemode.CreativeGameMode;
import com.mojang.minecraft.gamemode.SurvivalGameMode;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import org.lwjgl.input.Keyboard;

public final class GameSettings {

	private static final String[] fog = new String[] { "FAR", "NORMAL", "SHORT", "TINY" };
	public boolean music = true;
	public boolean sound = true;
	public boolean invertMouse = false;
	public boolean showInfo = false;
	public int viewDistance = 0;
	public boolean viewBobbing = true;
	public boolean anaglyph = false;
	public boolean limitFPS = false;
	public boolean survival = false;
	public boolean smoothing = false;
	public boolean speed = false;
	public String texturePack = "none";
	public KeyBinding forwardKey = new KeyBinding("options.keys.forward", Keyboard.KEY_W);
	public KeyBinding leftKey = new KeyBinding("options.keys.left", Keyboard.KEY_A);
	public KeyBinding backKey = new KeyBinding("options.keys.back", Keyboard.KEY_S);
	public KeyBinding rightKey = new KeyBinding("options.keys.right", Keyboard.KEY_D);
	public KeyBinding jumpKey = new KeyBinding("options.keys.jump", Keyboard.KEY_SPACE);
	public KeyBinding buildKey = new KeyBinding("options.keys.blocks", Keyboard.KEY_B);
	public KeyBinding chatKey = new KeyBinding("options.keys.chat", Keyboard.KEY_T);
	public KeyBinding fogKey = new KeyBinding("options.keys.toggle-fog", Keyboard.KEY_F);
	public KeyBinding saveLocKey = new KeyBinding("options.keys.save-loc", Keyboard.KEY_RETURN);
	public KeyBinding loadLocKey = new KeyBinding("options.keys.load-loc", Keyboard.KEY_R);
	public KeyBinding[] bindings;
	private Minecraft mc;
	private File file;
	public int count;
	public int hacks;

	public GameSettings(Minecraft mc, File path) {
		this.bindings = new KeyBinding[] { this.forwardKey, this.leftKey, this.backKey, this.rightKey, this.jumpKey, this.buildKey, this.chatKey, this.fogKey, this.saveLocKey, this.loadLocKey };
		this.count = 10;
		this.hacks = 1;
		this.mc = mc;
		this.file = new File(path, "options.txt");
		this.load();
	}

	public final String getBinding(int key) {
		return this.getBindingName(key) + ": " + this.getBindingValue(key);
	}
	
	public final String getBindingName(int key) {
		return OpenClassic.getGame().getTranslator().translate(this.bindings[key].name);
	}
	
	public final String getBindingValue(int key) {
		return Keyboard.getKeyName(this.bindings[key].key);
	}

	public final void setBinding(int key, int keyId) {
		this.bindings[key].key = keyId;
		this.save();
	}

	public final void toggleSetting(int setting, int fogValue) {
		if (setting == 0) {
			this.music = !this.music;
			if(!this.music) this.mc.audio.stopMusic();
		}

		if (setting == 1) {
			this.sound = !this.sound;
		}

		if (setting == 2) {
			this.invertMouse = !this.invertMouse;
		}

		if (setting == 3) {
			this.showInfo = !this.showInfo;
		}

		if (setting == 4) {
			this.viewDistance = this.viewDistance + fogValue & 3;
		}

		if (setting == 5) {
			this.viewBobbing = !this.viewBobbing;
		}

		if (setting == 6) {
			this.anaglyph = !this.anaglyph;
			this.mc.textureManager.clear();
			if(this.mc.ingame) {
				this.mc.levelRenderer.refresh();
			}
		}

		if (setting == 7) {
			this.limitFPS = !this.limitFPS;
		}
		
		if (setting == 8) {
			this.survival = !this.survival;
			this.mc.mode = this.survival ? new SurvivalGameMode(this.mc) : new CreativeGameMode(this.mc);
			
			if(this.mc.level != null) {
				this.mc.mode.apply(this.mc.level);
			}
			
			if(this.mc.player != null) {
				this.mc.mode.apply(this.mc.player);
			}
		}
		
		if (setting == 9) {
			this.smoothing = !this.smoothing;
			this.mc.textureManager.clear();
			if(this.mc.ingame) {
				this.mc.levelRenderer.refresh();
			}
		}

		this.save();
	}

	public final String getSetting(int id) {
		return this.getSettingName(id) + ": " + this.getSettingValue(id);
	}
	
	public final String getSettingName(int id) {
		return id == 0 ? OpenClassic.getGame().getTranslator().translate("options.music") : (id == 1 ? OpenClassic.getGame().getTranslator().translate("options.sound") : (id == 2 ? OpenClassic.getGame().getTranslator().translate("options.invert-mouse") : (id == 3 ? OpenClassic.getGame().getTranslator().translate("options.show-info") : (id == 4 ? OpenClassic.getGame().getTranslator().translate("options.render-distance") : (id == 5 ? OpenClassic.getGame().getTranslator().translate("options.view-bobbing") : (id == 6 ? OpenClassic.getGame().getTranslator().translate("options.3d-anaglyph") : (id == 7 ? OpenClassic.getGame().getTranslator().translate("options.limit-frames") : (id == 8 ? OpenClassic.getGame().getTranslator().translate("options.survival") : id == 9 ? OpenClassic.getGame().getTranslator().translate("options.smoothing") : ""))))))));
	}
	
	public final String getSettingValue(int id) {
		return id == 0 ? (this.music ? OpenClassic.getGame().getTranslator().translate("options.on") : OpenClassic.getGame().getTranslator().translate("options.off")) : (id == 1 ? (this.sound ? OpenClassic.getGame().getTranslator().translate("options.on") : OpenClassic.getGame().getTranslator().translate("options.off")) : (id == 2 ? (this.invertMouse ? OpenClassic.getGame().getTranslator().translate("options.on") : OpenClassic.getGame().getTranslator().translate("options.off")) : (id == 3 ? (this.showInfo ? OpenClassic.getGame().getTranslator().translate("options.on") : OpenClassic.getGame().getTranslator().translate("options.off")) : (id == 4 ? fog[this.viewDistance] : (id == 5 ? (this.viewBobbing ? OpenClassic.getGame().getTranslator().translate("options.on") : OpenClassic.getGame().getTranslator().translate("options.off")) : (id == 6 ? (this.anaglyph ? OpenClassic.getGame().getTranslator().translate("options.on") : OpenClassic.getGame().getTranslator().translate("options.off")) : (id == 7 ? (this.limitFPS ? OpenClassic.getGame().getTranslator().translate("options.on") : OpenClassic.getGame().getTranslator().translate("options.off")) : (id == 8 ? (this.survival ? OpenClassic.getGame().getTranslator().translate("options.on") : OpenClassic.getGame().getTranslator().translate("options.off")) : id == 9 ? (this.smoothing ? OpenClassic.getGame().getTranslator().translate("options.on") : OpenClassic.getGame().getTranslator().translate("options.off")) : ""))))))));
	}
	
	public final String getHackName(int id) {
		switch(id) {
			case 0: return OpenClassic.getGame().getTranslator().translate("options.hacks.speed");
			default: return "";
		}
	}
	
	public final String getHackValue(int id) {
		switch(id) {
			case 0: return this.speed ? OpenClassic.getGame().getTranslator().translate("options.on") : OpenClassic.getGame().getTranslator().translate("options.off");
			default: return "";
		}
	}
	
	public void toggleHack(int id) {
		switch(id) {
			case 0: {
				this.speed = !this.speed;
				break;
			}
		}
	}

	private void load() {
		try {
			if (this.file.exists()) {
				BufferedReader reader = new BufferedReader(new FileReader(this.file));
				String line = null;

				while ((line = reader.readLine()) != null) {
					String[] setting = line.split(":");
					if (setting[0].equals("music")) {
						this.music = setting[1].equals("true");
					}

					if (setting[0].equals("sound")) {
						this.sound = setting[1].equals("true");
					}

					if (setting[0].equals("invertYMouse")) {
						this.invertMouse = setting[1].equals("true");
					}

					if (setting[0].equals("showFrameRate")) {
						this.showInfo = setting[1].equals("true");
					}

					if (setting[0].equals("viewDistance")) {
						this.viewDistance = Integer.parseInt(setting[1]);
					}

					if (setting[0].equals("bobView")) {
						this.viewBobbing = setting[1].equals("true");
					}

					if (setting[0].equals("anaglyph3d")) {
						this.anaglyph = setting[1].equals("true");
					}

					if (setting[0].equals("limitFramerate")) {
						this.limitFPS = setting[1].equals("true");
					}
					
					if (setting[0].equals("survival")) {
						this.survival = setting[1].equals("true");
					}
					
					if (setting[0].equals("smoothing")) {
						this.smoothing = setting[1].equals("true");
					}
					
					if (setting[0].equals("texturepack")) {
						this.texturePack = setting[1];
					}

					for (int index = 0; index < this.bindings.length; index++) {
						if (setting[0].equals("key_" + this.bindings[index].key)) {
							this.bindings[index].key = Integer.parseInt(setting[1]);
						}
					}
				}

				reader.close();
			}
		} catch (IOException e) {
			System.out.println(OpenClassic.getGame().getTranslator().translate("core.fail-options-load"));
			e.printStackTrace();
		}
	}

	public void save() {
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(this.file));
			writer.println("music:" + this.music);
			writer.println("sound:" + this.sound);
			writer.println("invertYMouse:" + this.invertMouse);
			writer.println("showFrameRate:" + this.showInfo);
			writer.println("viewDistance:" + this.viewDistance);
			writer.println("bobView:" + this.viewBobbing);
			writer.println("anaglyph3d:" + this.anaglyph);
			writer.println("limitFramerate:" + this.limitFPS);
			writer.println("survival:" + this.survival);
			writer.println("smoothing:" + this.smoothing);
			writer.println("texturepack:" + this.texturePack);

			for (int binding = 0; binding < this.bindings.length; ++binding) {
				writer.println("key_" + this.bindings[binding].key + ":" + this.bindings[binding].key);
			}

			writer.close();
		} catch (Exception e) {
			System.out.println(OpenClassic.getGame().getTranslator().translate("core.fail-options-save"));
			e.printStackTrace();
		}
	}

}
