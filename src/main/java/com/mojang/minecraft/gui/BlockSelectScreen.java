package com.mojang.minecraft.gui;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.util.GeneralUtils;

import com.mojang.minecraft.render.ShapeRenderer;

import org.lwjgl.opengl.GL11;

public final class BlockSelectScreen extends GuiScreen {

	public BlockSelectScreen() {
		this.setGrabsInput(false);
	}

	private int getBlockOnScreen(int x, int y) {
		int count = 0;
		for (BlockType block : Blocks.getBlocks()) {
			if(block != null && block.isSelectable()) {
				int blockX = this.getWidth() / 2 + count % 9 * 24 + -108 - 3;
				int blockZ = this.getHeight() / 2 + count / 9 * 24 + -60 + 3;
				if (x >= blockX && x <= blockX + 24 && y >= blockZ - 12 && y <= blockZ + 12) {
					return count;
				}
				
				count++;
			}
		}

		return -1;
	}

	public final void render() {
		int mouseX = RenderHelper.getHelper().getRenderMouseX();
		int mouseY = RenderHelper.getHelper().getRenderMouseY();
		
		int block = this.getBlockOnScreen(mouseX, mouseY);
		RenderHelper.getHelper().color(this.getWidth() / 2 - 120, 30, this.getWidth() / 2 + 120, 180, -1878719232, -1070583712);
		if (block >= 0) {
			int selectX = this.getWidth() / 2 + block % 9 * 24 + -108;
			int selectY = this.getHeight() / 2 + block / 9 * 24 + -60;
			RenderHelper.getHelper().color(selectX - 3, selectY - 8, selectX + 23, selectY + 24 - 6, -1862270977, -1056964609);
		}

		RenderHelper.getHelper().renderText(OpenClassic.getGame().getTranslator().translate("gui.blocks.select"), this.getWidth() / 2, 40);

		int count = 0;
		for (BlockType b : Blocks.getBlocks()) {
			if(b != null && b.isSelectable()) {
				GL11.glPushMatrix();
				GL11.glTranslatef(this.getWidth() / 2 + count % 9 * 24 + -108, this.getHeight() / 2 + count / 9 * 24 + -60, 0);
				GL11.glScalef(10.0F, 10.0F, 10.0F);
				GL11.glTranslatef(1.0F, 0.5F, 8.0F);
				GL11.glRotatef(-30.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
				if (block == count) {
					GL11.glScalef(1.6F, 1.6F, 1.6F);
				}
	
				GL11.glTranslatef(-1.5F, 0.5F, 0.5F);
				GL11.glScalef(-1.0F, -1.0F, -1.0F);
				ShapeRenderer.instance.begin();
				b.getModel().renderFullbright(-2, 0, 0);
				ShapeRenderer.instance.end();
				GL11.glPopMatrix();
				
				count++;
			}
		}
	}

	public final void onMouseClick(int x, int y, int state) {
		if (state == 0) {
			GeneralUtils.getMinecraft().player.inventory.replaceSlot(this.getBlockOnScreen(x, y));
			GeneralUtils.getMinecraft().setCurrentScreen(null);
		}
	}
}
