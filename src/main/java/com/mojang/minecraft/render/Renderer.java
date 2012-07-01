package com.mojang.minecraft.render;

import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.VanillaBlock;

import com.mojang.minecraft.Entity;
import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.model.Vector;
import com.mojang.minecraft.render.HeldBlock;
import com.mojang.util.MathHelper;
import java.nio.FloatBuffer;
import java.util.Random;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public final class Renderer {

	public Minecraft mc;
	public boolean displayActive = false;
	public float fogEnd = 0.0F;
	public HeldBlock heldBlock;
	public int levelTicks;
	public Entity entity = null;
	public Random rand = new Random();
	private FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
	public float fogRed;
	public float fogBlue;
	public float fogGreen;

	public Renderer(Minecraft mc) {
		this.mc = mc;
		this.heldBlock = new HeldBlock(mc);
	}

	public Vector a(float var1) {
		float x = this.mc.player.xo + (this.mc.player.x - this.mc.player.xo) * var1;
		float y = this.mc.player.yo + (this.mc.player.y - this.mc.player.yo) * var1;
		float z = this.mc.player.zo + (this.mc.player.z - this.mc.player.zo) * var1;
		return new Vector(x, y, z);
	}

	public void hurtEffect(float offset) {
		float effect = this.mc.player.hurtTime - offset;
		if (this.mc.player.health <= 0) {
			offset += this.mc.player.deathTime;
			GL11.glRotatef(40.0F - 8000.0F / (offset + 200.0F), 0, 0, 1);
		}

		if (effect >= 0) {
			effect = MathHelper.sin((effect /= this.mc.player.hurtDuration) * effect * effect * effect * (float) Math.PI);
			GL11.glRotatef(-this.mc.player.hurtDir, 0, 1, 0);
			GL11.glRotatef(-effect * 14.0F, 0, 0, 1);
			GL11.glRotatef(this.mc.player.hurtDir, 0, 1, 0);
		}
	}

	public void applyBobbing(float offset) {
		float dist = this.mc.player.walkDist + (this.mc.player.walkDist - this.mc.player.walkDistO) * offset;
		float bob = this.mc.player.oBob + (this.mc.player.bob - this.mc.player.oBob) * offset;
		float tilt = this.mc.player.oTilt + (this.mc.player.tilt - this.mc.player.oTilt) * offset;
		GL11.glTranslatef(MathHelper.sin(dist * (float) Math.PI) * bob * 0.5F, -Math.abs(MathHelper.cos(dist * (float) Math.PI) * bob), 0);
		GL11.glRotatef(MathHelper.sin(dist * (float) Math.PI) * bob * 3.0F, 0, 0, 1);
		GL11.glRotatef(Math.abs(MathHelper.cos(dist * (float) Math.PI + 0.2F) * bob) * 5.0F, 1, 0, 0);
		GL11.glRotatef(tilt, 1, 0, 0);
	}

	public final void setLighting(boolean lighting) {
		if (!lighting) {
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_COLOR_BUFFER_BIT);
		} else {
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_COLOR_BUFFER_BIT);
			GL11.glEnable(GL11.GL_COLOR_MATERIAL);
			GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE);
			Vector vec = new Vector(0.0F, -1.0F, 0.5F).a();
			GL11.glLight(GL11.GL_COLOR_BUFFER_BIT, GL11.GL_POSITION, this.getParamBuffer(vec.x, vec.y, vec.z, 0));
			GL11.glLight(GL11.GL_COLOR_BUFFER_BIT, GL11.GL_DIFFUSE, this.getParamBuffer(0.3F, 0.3F, 0.3F, 1));
			GL11.glLight(GL11.GL_COLOR_BUFFER_BIT, GL11.GL_AMBIENT, this.getParamBuffer(0, 0, 0, 1));
			GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, this.getParamBuffer(0.7F, 0.7F, 0.7F, 1));
		}
	}

	public final void reset() {
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
		GL11.glFog(GL11.GL_FOG_COLOR, this.getParamBuffer(this.fogRed, this.fogBlue, this.fogGreen, 1));
		GL11.glNormal3f(0, -1, 0);
		GL11.glColor4f(1, 1, 1, 1);
		BlockType type = Blocks.fromId(this.mc.level.getTile((int) this.mc.player.x, (int) (this.mc.player.y + 0.12F), (int) this.mc.player.z));
		if (type != null && type.isLiquid()) {
			GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
			if (type == VanillaBlock.WATER || type == VanillaBlock.STATIONARY_WATER) {
				GL11.glFogf(GL11.GL_FOG_DENSITY, 0.1F);
				float r = 0.4F;
				float g = 0.4F;
				float b = 0.9F;
				if (this.mc.settings.anaglyph) {
					r = (r * 30.0F + g * 59.0F + b * 11.0F) / 100.0F;
					g = (r * 30.0F + g * 70.0F) / 100.0F;
					b = (r * 30.0F + b * 70.0F) / 100.0F;
				}

				GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, this.getParamBuffer(r, g, b, 1));
			} else if (type == VanillaBlock.LAVA || type == VanillaBlock.STATIONARY_LAVA) {
				GL11.glFogf(GL11.GL_FOG_DENSITY, 2);
				float r = 0.4F;
				float g = 0.3F;
				float b = 0.3F;
				if (this.mc.settings.anaglyph) {
					r = (r * 30.0F + g * 59.0F + b * 11.0F) / 100.0F;
					g = (r * 30.0F + g * 70.0F) / 100.0F;
					b = (r * 30.0F + b * 70.0F) / 100.0F;
				}

				GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, this.getParamBuffer(r, g, b, 1));
			}
		} else {
			GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR);
			GL11.glFogf(GL11.GL_FOG_START, 0);
			GL11.glFogf(GL11.GL_FOG_END, this.fogEnd);
			GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, this.getParamBuffer(1, 1, 1, 1));
		}

		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glColorMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT);
	}

	private FloatBuffer getParamBuffer(float param1, float param2, float param3, float param4) {
		this.buffer.clear();
		this.buffer.put(param1).put(param2).put(param3).put(param4);
		this.buffer.flip();
		return this.buffer;
	}
}
