package com.mojang.minecraft.render;

import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.VanillaBlock;

import com.mojang.minecraft.Entity;
import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.model.ModelPoint;
import com.mojang.minecraft.player.Player;
import com.mojang.minecraft.render.HeldBlock;
import com.mojang.util.MathHelper;
import java.nio.FloatBuffer;
import java.util.Random;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public final class Renderer {

	public Minecraft mc;
	public float b = 1.0F;
	public boolean displayActive = false;
	public float d = 0.0F;
	public HeldBlock heldBlock;
	public int f;
	public Entity g = null;
	public Random rand = new Random();
	private FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
	public float i;
	public float j;
	public float k;

	public Renderer(Minecraft mc) {
		this.mc = mc;
		this.heldBlock = new HeldBlock(mc);
	}

	public com.mojang.minecraft.model.ModelPoint a(float var1) {
		Player var4;
		float var2 = (var4 = this.mc.player).xo + (var4.x - var4.xo) * var1;
		float var3 = var4.yo + (var4.y - var4.yo) * var1;
		float var5 = var4.zo + (var4.z - var4.zo) * var1;
		return new com.mojang.minecraft.model.ModelPoint(var2, var3, var5);
	}

	public void b(float var1) {
		Player var3;
		float var2 = (var3 = this.mc.player).hurtTime - var1;
		if (var3.health <= 0) {
			var1 += var3.deathTime;
			GL11.glRotatef(40.0F - 8000.0F / (var1 + 200.0F), 0.0F, 0.0F, 1.0F);
		}

		if (var2 >= 0.0F) {
			var2 = MathHelper.a((var2 /= var3.hurtDuration) * var2 * var2 * var2 * 3.1415927F);
			var1 = var3.hurtDir;
			GL11.glRotatef(-var3.hurtDir, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(-var2 * 14.0F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(var1, 0.0F, 1.0F, 0.0F);
		}
	}

	public void c(float var1) {
		Player var4;
		float var2 = (var4 = this.mc.player).walkDist - var4.walkDistO;
		var2 = var4.walkDist + var2 * var1;
		float var3 = var4.oBob + (var4.bob - var4.oBob) * var1;
		float var5 = var4.oTilt + (var4.tilt - var4.oTilt) * var1;
		GL11.glTranslatef(MathHelper.a(var2 * 3.1415927F) * var3 * 0.5F, -Math.abs(MathHelper.b(var2 * 3.1415927F) * var3), 0.0F);
		GL11.glRotatef(MathHelper.a(var2 * 3.1415927F) * var3 * 3.0F, 0.0F, 0.0F, 1.0F);
		GL11.glRotatef(Math.abs(MathHelper.b(var2 * 3.1415927F + 0.2F) * var3) * 5.0F, 1.0F, 0.0F, 0.0F);
		GL11.glRotatef(var5, 1.0F, 0.0F, 0.0F);
	}

	public final void a(boolean var1) {
		if (!var1) {
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_COLOR_BUFFER_BIT);
		} else {
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_COLOR_BUFFER_BIT);
			GL11.glEnable(GL11.GL_COLOR_MATERIAL);
			GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE);
			float var4 = 0.7F;
			float var2 = 0.3F;
			ModelPoint var3 = (new ModelPoint(0.0F, -1.0F, 0.5F)).a();
			GL11.glLight(GL11.GL_COLOR_BUFFER_BIT, GL11.GL_POSITION, this.a(var3.x, var3.y, var3.z, 0.0F));
			GL11.glLight(GL11.GL_COLOR_BUFFER_BIT, GL11.GL_DIFFUSE, this.a(var2, var2, var2, 1.0F));
			GL11.glLight(GL11.GL_COLOR_BUFFER_BIT, GL11.GL_AMBIENT, this.a(0.0F, 0.0F, 0.0F, 1.0F));
			GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, this.a(var4, var4, var4, 1.0F));
		}
	}

	public final void a() {
		int width = this.mc.width * 240 / this.mc.height;
		int height = this.mc.height * 240 / this.mc.height;
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0.0D, width, height, 0.0D, 100.0D, 300.0D);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glTranslatef(0.0F, 0.0F, -200.0F);
	}

	public void renderFog() {
		Level var1 = this.mc.level;
		Player var2 = this.mc.player;
		GL11.glFog(GL11.GL_FOG_COLOR, this.a(this.i, this.j, this.k, 1.0F));
		GL11.glNormal3f(0.0F, -1.0F, 0.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		BlockType var5;
		if ((var5 = Blocks.fromId(var1.getTile((int) var2.x, (int) (var2.y + 0.12F), (int) var2.z))) != null && var5.isLiquid()) {
			GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
			float var3;
			float var4;
			float var7;
			float var8;
			if (var5 == VanillaBlock.WATER || var5 == VanillaBlock.STATIONARY_WATER) {
				GL11.glFogf(GL11.GL_FOG_DENSITY, 0.1F);
				var7 = 0.4F;
				var8 = 0.4F;
				var3 = 0.9F;
				if (this.mc.settings.anaglyph) {
					var4 = (var7 * 30.0F + var8 * 59.0F + var3 * 11.0F) / 100.0F;
					var8 = (var7 * 30.0F + var8 * 70.0F) / 100.0F;
					var3 = (var7 * 30.0F + var3 * 70.0F) / 100.0F;
					var7 = var4;
				}

				GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, this.a(var7, var8, var3, 1.0F));
			} else if (var5 == VanillaBlock.LAVA || var5 == VanillaBlock.STATIONARY_LAVA) {
				GL11.glFogf(GL11.GL_FOG_DENSITY, 2.0F);
				var7 = 0.4F;
				var8 = 0.3F;
				var3 = 0.3F;
				if (this.mc.settings.anaglyph) {
					var4 = (var7 * 30.0F + var8 * 59.0F + var3 * 11.0F) / 100.0F;
					var8 = (var7 * 30.0F + var8 * 70.0F) / 100.0F;
					var3 = (var7 * 30.0F + var3 * 70.0F) / 100.0F;
					var7 = var4;
				}

				GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, this.a(var7, var8, var3, 1.0F));
			}
		} else {
			GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR);
			GL11.glFogf(GL11.GL_FOG_START, 0.0F);
			GL11.glFogf(GL11.GL_FOG_END, this.d);
			GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, this.a(1.0F, 1.0F, 1.0F, 1.0F));
		}

		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glColorMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT);
	}

	private FloatBuffer a(float var1, float var2, float var3, float var4) {
		this.buffer.clear();
		this.buffer.put(var1).put(var2).put(var3).put(var4);
		this.buffer.flip();
		return this.buffer;
	}
}
