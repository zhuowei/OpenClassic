package com.mojang.minecraft.mob;

import ch.spacebase.openclassic.api.render.RenderHelper;

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
			model.headwear.yaw = model.head.yaw;
			model.headwear.pitch = model.head.pitch;
			model.headwear.render(scale);
			GL11.glEnable(GL11.GL_CULL_FACE);
		}

		if (this.armor || this.helmet) {
			RenderHelper.getHelper().bindTexture("/armor/plate.png", true);
			GL11.glDisable(GL11.GL_CULL_FACE);
			HumanoidModel armored = (HumanoidModel) modelCache.getModel("humanoid.armor");
			armored.head.render = this.helmet;
			armored.body.render = this.armor;
			armored.rightArm.render = this.armor;
			armored.leftArm.render = this.armor;
			armored.rightLeg.render = false;
			armored.leftLeg.render = false;
			armored.head.yaw = model.head.yaw;
			armored.head.pitch = model.head.pitch;
			armored.rightArm.pitch = model.rightArm.pitch;
			armored.rightArm.roll = model.rightArm.roll;
			armored.leftArm.pitch = model.leftArm.pitch;
			armored.leftArm.roll = model.leftArm.roll;
			armored.rightLeg.pitch = model.rightLeg.pitch;
			armored.leftLeg.pitch = model.leftLeg.pitch;
			armored.head.render(scale);
			armored.body.render(scale);
			armored.rightArm.render(scale);
			armored.leftArm.render(scale);
			armored.rightLeg.render(scale);
			armored.leftLeg.render(scale);
			GL11.glEnable(GL11.GL_CULL_FACE);
		}

		GL11.glDisable(GL11.GL_ALPHA_TEST);
	}
}
