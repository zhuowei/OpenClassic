package com.mojang.minecraft.mob;

import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.mob.Mob;
import com.mojang.minecraft.model.HumanoidModel;
import com.mojang.minecraft.render.TextureManager;
import org.lwjgl.opengl.GL11;

public class HumanoidMob extends Mob {

	public static final long serialVersionUID = 0L;
	public boolean helmet = Math.random() < 0.20000000298023224D;
	public boolean armor = Math.random() < 0.20000000298023224D;

	public HumanoidMob(Level level, float x, float y, float z) {
		super(level);
		this.modelName = "humanoid";
		this.setPos(x, y, z);
	}

	public void renderModel(TextureManager textures, float var2, float var3, float var4, float var5, float var6, float scale) {
		super.renderModel(textures, var2, var3, var4, var5, var6, scale);
		HumanoidModel model = (HumanoidModel) modelCache.getModel(this.modelName);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		if (this.allowAlpha) {
			GL11.glEnable(GL11.GL_CULL_FACE);
		}

		if (this.hasHair) {
			GL11.glDisable(GL11.GL_CULL_FACE);
			model.c.yaw = model.helmet.yaw;
			model.c.pitch = model.helmet.pitch;
			model.c.render(scale);
			GL11.glEnable(GL11.GL_CULL_FACE);
		}

		if (this.armor || this.helmet) {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.bindTexture("/armor/plate.png"));
			GL11.glDisable(GL11.GL_CULL_FACE);
			HumanoidModel armored = (HumanoidModel) modelCache.getModel("humanoid.armor");
			armored.helmet.render = this.helmet;
			armored.chestplate.render = this.armor;
			armored.leggings.render = this.armor;
			armored.arm.render = this.armor;
			armored.g.render = false;
			armored.h.render = false;
			armored.helmet.yaw = model.helmet.yaw;
			armored.helmet.pitch = model.helmet.pitch;
			armored.leggings.pitch = model.leggings.pitch;
			armored.leggings.roll = model.leggings.roll;
			armored.arm.pitch = model.arm.pitch;
			armored.arm.roll = model.arm.roll;
			armored.g.pitch = model.g.pitch;
			armored.h.pitch = model.h.pitch;
			armored.helmet.render(scale);
			armored.chestplate.render(scale);
			armored.leggings.render(scale);
			armored.arm.render(scale);
			armored.g.render(scale);
			armored.h.render(scale);
			GL11.glEnable(GL11.GL_CULL_FACE);
		}

		GL11.glDisable(GL11.GL_ALPHA_TEST);
	}
}
