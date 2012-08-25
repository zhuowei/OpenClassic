package com.mojang.minecraft;

import ch.spacebase.openclassic.api.ProgressBar;
import ch.spacebase.openclassic.client.render.ClientRenderHelper;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.render.ShapeRenderer;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

public final class ProgressBarDisplay implements ProgressBar {

	public String text = "";
	private Minecraft mc;
	private String title = "";
	private long start = System.currentTimeMillis();
	private int progress = 0;

	public ProgressBarDisplay(Minecraft mc) {
		this.mc = mc;
	}

	public void setTitle(String title) {
		if (this.mc.running) {
			this.title = title;
			int x = this.mc.width * 240 / this.mc.height;
			int y = this.mc.height * 240 / this.mc.height;
			
			GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			GL11.glOrtho(0, x, y, 0, 100, 300);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glLoadIdentity();
			GL11.glTranslatef(0, 0, -200);
		}
	}

	public void setText(String text) {
		this.setText(text, true);
	}
	
	public void setText(String text, boolean update) {
		if (this.mc.running) {
			this.text = text;
			if(update) this.setProgress(-1);
		}
	}

	public void setProgress(int progress) {
		if (!this.mc.running) {
			return;
		} else {
			if (System.currentTimeMillis() - this.start < 0 || System.currentTimeMillis() - this.start >= 20) {
				this.start = System.currentTimeMillis();
				int x = this.mc.width * 240 / this.mc.height;
				int y = this.mc.height * 240 / this.mc.height;
				ClientRenderHelper.getHelper().drawDirtBG();
				if (progress >= 0) {
					int barX = x / 2 - 50;
					int barY = y / 2 + 16;
					GL11.glDisable(GL11.GL_TEXTURE_2D);
					ShapeRenderer.instance.begin();
					ShapeRenderer.instance.color(8421504);
					ShapeRenderer.instance.vertex(barX, barY, 0.0F);
					ShapeRenderer.instance.vertex(barX, (barY + 2), 0.0F);
					ShapeRenderer.instance.vertex((barX + 100), (barY + 2), 0.0F);
					ShapeRenderer.instance.vertex((barX + 100), barY, 0.0F);
					ShapeRenderer.instance.color(8454016);
					ShapeRenderer.instance.vertex(barX, barY, 0.0F);
					ShapeRenderer.instance.vertex(barX, (barY + 2), 0.0F);
					ShapeRenderer.instance.vertex((barX + progress), (barY + 2), 0.0F);
					ShapeRenderer.instance.vertex((barX + progress), barY, 0.0F);
					ShapeRenderer.instance.end();
					GL11.glEnable(GL11.GL_TEXTURE_2D);
				}

				this.mc.fontRenderer.renderWithShadow(this.title, (x - this.mc.fontRenderer.getWidth(this.title)) / 2, y / 2 - 4 - 16, 16777215);
				this.mc.fontRenderer.renderWithShadow(this.text, (x - this.mc.fontRenderer.getWidth(this.text)) / 2, y / 2 - 4 + 8, 16777215);
				Display.update();
			}
			
			this.progress = progress;
		}
	}

	@Override
	public String getText() {
		return this.text;
	}

	@Override
	public String getTitle() {
		return this.title;
	}

	@Override
	public int getProgress() {
		return this.progress;
	}
}
