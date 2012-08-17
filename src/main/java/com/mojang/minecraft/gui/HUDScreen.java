package com.mojang.minecraft.gui;

import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.gui.MainScreen;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.api.util.Constants;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.ChatLine;
import com.mojang.minecraft.gamemode.SurvivalGameMode;
import com.mojang.minecraft.gui.ChatInputScreen;
import com.mojang.minecraft.render.ShapeRenderer;
import com.mojang.util.MathHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public final class HUDScreen implements MainScreen {

	public List<ChatLine> chatHistory = new ArrayList<ChatLine>();
	private Random rand = new Random();
	private Minecraft mc;
	public int width;
	public int height;
	public String clickedPlayer = null;
	public int ticks = 0;

	public HUDScreen(Minecraft mc, int width, int height) {
		this.mc = mc;
		this.width = width * 240 / height;
		this.height = height * 240 / height;
	}

	public final void render(float renderPartialTicks, boolean focus, int mouseX, int mouseY) {
		this.mc.renderer.reset();
		RenderHelper.getHelper().bindTexture("/gui/gui.png", true);
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_BLEND);
		
		if(!this.mc.hideGui) {
			RenderHelper.getHelper().drawImage(this.width / 2 - 91, this.height - 22, -90, 0, 0, 182, 22);
			RenderHelper.getHelper().drawImage(this.width / 2 - 91 - 1 + this.mc.player.inventory.selected * 20, this.height - 22 - 1, -90, 0, 22, 24, 22);
		
			RenderHelper.getHelper().bindTexture("/gui/icons.png", true);
			RenderHelper.getHelper().drawImage(this.width / 2 - 7, this.height / 2 - 7, -90, 0, 0, 16, 16);
		
			boolean glow = this.mc.player.invulnerableTime / 3 % 2 != 0 && this.mc.player.invulnerableTime >= 10;
			this.rand.setSeed((this.ticks * 312871L));
			if (this.mc.mode.isSurvival()) {
				for (int heart = 0; heart < 10; heart++) {
					int heartX = this.width / 2 - 91 + (heart << 3);
					int heartY = this.height - 32;
					
					if (this.mc.player.health <= 4) {
						heartY += this.rand.nextInt(2);
					}
	
					RenderHelper.getHelper().drawImage(heartX, heartY, -90, 16 + (glow ? 9 : 0), 0, 9, 9);
					if (glow) {
						if ((heart << 1) + 1 < this.mc.player.lastHealth) {
							RenderHelper.getHelper().drawImage(heartX, heartY, -90, 70, 0, 9, 9);
						}
	
						if ((heart << 1) + 1 == this.mc.player.lastHealth) {
							RenderHelper.getHelper().drawImage(heartX, heartY, -90, 79, 0, 9, 9);
						}
					}
	
					if ((heart << 1) + 1 < this.mc.player.health) {
						RenderHelper.getHelper().drawImage(heartX, heartY, -90, 52, 0, 9, 9);
					}
	
					if ((heart << 1) + 1 == this.mc.player.health) {
						RenderHelper.getHelper().drawImage(heartX, heartY, -90, 61, 0, 9, 9);
					}
				}
	
				if (this.mc.player.isUnderWater()) {
					int var100 = (int) Math.ceil((this.mc.player.airSupply - 2) * 10.0D / 300.0D);
					int var101 = (int) Math.ceil(this.mc.player.airSupply * 10.0D / 300.0D) - var100;
	
					for (int count = 0; count < var100 + var101; count++) {
						if (count < var100) {
							RenderHelper.getHelper().drawImage(this.width / 2 - 91 + (count << 3), this.height - 32 - 9, -90, 16, 18, 9, 9);
						} else {
							RenderHelper.getHelper().drawImage(this.width / 2 - 91 + (count << 3), this.height - 32 - 9, -90, 25, 18, 9, 9);
						}
					}
				}
			}
	
			GL11.glDisable(GL11.GL_BLEND);
	
			for (int slot = 0; slot < this.mc.player.inventory.slots.length; slot++) {
				int x = this.width / 2 - 90 + slot * 20;
				int y = this.height - 16;
				int block = this.mc.player.inventory.slots[slot];
				
				if (block > 0) {
					GL11.glPushMatrix();
					GL11.glTranslatef(x, y, -50);
					
					if (this.mc.player.inventory.popTime[slot] > 0) {
						float off = (this.mc.player.inventory.popTime[slot] - renderPartialTicks) / 5;
						GL11.glTranslatef(10, (-MathHelper.sin(off * off * (float) Math.PI) * 8) + 10, 0);
						GL11.glScalef(MathHelper.sin(off * off * (float) Math.PI) + 1, MathHelper.sin(off * (float) Math.PI) + 1, 1);
						GL11.glTranslatef(-10, -10, 0);
					}
	
					GL11.glScalef(10, 10, 10);
					GL11.glTranslatef(1, 0.5F, 0);
					GL11.glRotatef(-30, 1, 0, 0);
					GL11.glRotatef(45, 0, 1, 0);
					GL11.glTranslatef(-1.5F, 0.5F, 0.5F);
					GL11.glScalef(-1, -1, -1);
					
					ShapeRenderer.instance.begin();
					Blocks.fromId(block).getModel().renderFullbright(-2, 0, 0);
					ShapeRenderer.instance.end();
					
					GL11.glPopMatrix();
					
					if (this.mc.player.inventory.count[slot] > 1) {
						this.mc.fontRenderer.renderWithShadow(String.valueOf(this.mc.player.inventory.count[slot]), x + 19 - this.mc.fontRenderer.getWidth(String.valueOf(this.mc.player.inventory.count[slot])), y + 6, 16777215);
					}
				}
			}
	
			this.mc.fontRenderer.renderWithShadow(Constants.CLIENT_VERSION, 2, 2, 16777215);
			if (this.mc.settings.showInfo) {
				this.mc.fontRenderer.renderWithShadow(this.mc.debugInfo, 2, 12, 16777215);
				this.mc.fontRenderer.renderWithShadow("Position: " + (int) Math.floor(this.mc.player.x) + ", " + (int) Math.floor(this.mc.player.y) + ", " + (int) Math.floor(this.mc.player.z), 2, 22, 16777215);
			}
	
			if (this.mc.mode instanceof SurvivalGameMode) {
				String score = "Score: &e" + this.mc.player.getScore();
				this.mc.fontRenderer.renderWithShadow(score, this.width - this.mc.fontRenderer.getWidth(score) - 2, 2, 16777215);
				this.mc.fontRenderer.renderWithShadow("Arrows: " + this.mc.player.arrows, this.width / 2 + 8, this.height - 33, 16777215);
			}
		}

		byte maxMsgs = 10;
		boolean showAllMsgs = false;
		if (this.mc.currentScreen instanceof ChatInputScreen) {
			maxMsgs = 20;
			showAllMsgs = true;
		}

		for (int message = 0; message < this.chatHistory.size() && message < maxMsgs; message++) {
			if (this.chatHistory.get(message).time < 200 || showAllMsgs) {
				this.mc.fontRenderer.renderWithShadow(this.chatHistory.get(message).message, 2, this.height - 8 - message * 9 - 20, 16777215);
			}
		}

		this.clickedPlayer = null;
		if (Keyboard.isKeyDown(Keyboard.KEY_TAB) && this.mc.netManager != null && this.mc.netManager.isConnected()) {
			List<String> players = this.mc.netManager.getPlayers();
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glColor4f(0, 0, 0, 0.7F);
			GL11.glVertex2f((this.width / 2 + 128), (this.height / 2 - 68 - 12));
			GL11.glVertex2f((this.width / 2 - 128), (this.height / 2 - 68 - 12));
			GL11.glColor4f(0.2F, 0.2F, 0.2F, 0.8F);
			GL11.glVertex2f((this.width / 2 - 128), (this.height / 2 + 68));
			GL11.glVertex2f((this.width / 2 + 128), (this.height / 2 + 68));
			GL11.glEnd();
			
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			
			this.mc.fontRenderer.renderWithShadow("Connected players:", this.width / 2 - this.mc.fontRenderer.getWidth("Connected players:") / 2, this.height / 2 - 64 - 12, 16777215);

			for (int count = 0; count < players.size(); ++count) {
				int x = this.width / 2 + count % 2 * 120 - 120;
				int y = this.height / 2 - 64 + (count / 2 << 3);
				if (focus && mouseX >= x && mouseY >= y && mouseX < x + 120 && mouseY < y + 8) {
					this.clickedPlayer = players.get(count);
					this.mc.fontRenderer.renderNoShadow(players.get(count), x + 2, y, 16777215);
				} else {
					this.mc.fontRenderer.renderNoShadow(players.get(count), x, y, 15658734);
				}
			}
		}
	}

	public final void addChat(String message) {
		this.chatHistory.add(0, new ChatLine(message));

		while (this.chatHistory.size() > 50) {
			this.chatHistory.remove(this.chatHistory.size() - 1);
		}
	}

	@Override
	public String getClickedPlayer() {
		return this.clickedPlayer;
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getHeight() {
		return this.height;
	}

	@Override
	public List<String> getChat() {
		List<String> result = new ArrayList<String>();
		for(ChatLine line : this.chatHistory) {
			result.add(line.message);
		}
		
		return result;
	}
	
	@Override
	public String getChatMessage(int index) {
		if(this.chatHistory.size() <= index) return null;
		return this.chatHistory.get(index).message;
	}

	@Override
	public String getLastChat() {
		if(this.chatHistory.size() <= 0) return null;
		return this.chatHistory.get(0).message;
	}
}
