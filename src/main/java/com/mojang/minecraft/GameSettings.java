package com.mojang.minecraft;

import com.mojang.minecraft.KeyBinding;
import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.gamemode.CreativeGameMode;
import com.mojang.minecraft.gamemode.SurvivalGameMode;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
	public KeyBinding forwardKey = new KeyBinding("Forward", Keyboard.KEY_W);
	public KeyBinding leftKey = new KeyBinding("Left", Keyboard.KEY_A);
	public KeyBinding backKey = new KeyBinding("Back", Keyboard.KEY_S);
	public KeyBinding rightKey = new KeyBinding("Right", Keyboard.KEY_D);
	public KeyBinding jumpKey = new KeyBinding("Jump", Keyboard.KEY_SPACE);
	public KeyBinding buildKey = new KeyBinding("Build", Keyboard.KEY_B);
	public KeyBinding chatKey = new KeyBinding("Chat", Keyboard.KEY_T);
	public KeyBinding fogKey = new KeyBinding("Toggle fog", Keyboard.KEY_F);
	public KeyBinding saveLocKey = new KeyBinding("Save location", Keyboard.KEY_RETURN);
	public KeyBinding loadLocKey = new KeyBinding("Load location", Keyboard.KEY_R);
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
		return this.bindings[key].name + ": " + Keyboard.getKeyName(this.bindings[key].key);
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
		}

		this.save();
	}

	public final String getSetting(int id) {
		return id == 0 ? "Music: " + (this.music ? "ON" : "OFF") : (id == 1 ? "Sound: " + (this.sound ? "ON" : "OFF") : (id == 2 ? "Invert mouse: " + (this.invertMouse ? "ON" : "OFF") : (id == 3 ? "Show Info: " + (this.showInfo ? "ON" : "OFF") : (id == 4 ? "Render distance: " + fog[this.viewDistance] : (id == 5 ? "View bobbing: " + (this.viewBobbing ? "ON" : "OFF") : (id == 6 ? "3d anaglyph: " + (this.anaglyph ? "ON" : "OFF") : (id == 7 ? "Limit framerate: " + (this.limitFPS ? "ON" : "OFF") : (id == 8 ? "Survival Mode: " + (this.survival ? "ON" : "OFF") : id == 9 ? "Smoothing: " + (this.smoothing ? "ON" : "OFF") : ""))))))));
	}
	
	public final String getHack(int id) {
		switch(id) {
			case 0: return "Speedhack: " + (this.speed ? "ON" : "OFF");
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
		} catch (Exception e) {
			System.out.println("Failed to load options");
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
			System.out.println("Failed to save options");
			e.printStackTrace();
		}
	}

}
