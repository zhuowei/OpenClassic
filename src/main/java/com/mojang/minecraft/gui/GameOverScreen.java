package com.mojang.minecraft.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.util.GeneralUtils;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.player.Player;

import org.lwjgl.opengl.GL11;

public final class GameOverScreen extends GuiScreen {

	public final void onOpen() {
		this.clearWidgets();
		this.attachWidget(new Button(0, this.getWidth() / 2 - 100, this.getHeight() / 4 + 72, this, OpenClassic.getGame().getTranslator().translate("gui.game-over.respawn")));
		this.attachWidget(new Button(1, this.getWidth() / 2 - 100, this.getHeight() / 4 + 96, this, OpenClassic.getGame().getTranslator().translate("gui.game-over.main-menu")));
	}

	public final void onButtonClick(Button button) {
		Minecraft mc = GeneralUtils.getMinecraft();
		
		if (button.getId() == 0) {
			for(int slot = 0; slot < 9; slot++) {
				mc.player.inventory.slots[slot] = -1;
				mc.player.inventory.count[slot] = 0;
			}
			
			mc.player.airSupply = 20;
			mc.player.arrows = 20;
			mc.player.deathTime = 0;
			mc.player.health = Player.MAX_HEALTH;
			mc.player.resetPos();
			
			OpenClassic.getClient().setCurrentScreen(null);
		}

		if (button.getId() == 1) {
			mc.stopGame(true);
		}
	}

	public final void render() {
		RenderHelper.getHelper().color(0, 0, this.getWidth(), this.getHeight(), 1615855616, -1602211792);
		
		GL11.glPushMatrix();
		GL11.glScalef(2.0F, 2.0F, 2.0F);
		RenderHelper.getHelper().renderText(OpenClassic.getGame().getTranslator().translate("gui.game-over.game-over"), this.getWidth() / 2 / 2, 30);
		GL11.glPopMatrix();
		RenderHelper.getHelper().renderText(String.format(OpenClassic.getGame().getTranslator().translate("gui.game-over.score"), GeneralUtils.getMinecraft().player.getScore()), this.getWidth() / 2, 100);
		super.render();
	}
}
