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

	public void renderModel(TextureManager textures, float var2, float var3, float var4, float var5, float var6, float var7) {
		super.renderModel(textures, var2, var3, var4, var5, var6, var7);
		HumanoidModel model = (HumanoidModel) modelCache.getModel(this.modelName);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		if (this.allowAlpha) {
			GL11.glEnable(GL11.GL_CULL_FACE);
		}

		if (this.hasHair) {
			GL11.glDisable(GL11.GL_CULL_FACE);
			model.c.g = model.b.g;
			model.c.f = model.b.f;
			model.c.a(var7);
			GL11.glEnable(GL11.GL_CULL_FACE);
		}

		if (this.armor || this.helmet) {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.bindTexture("/armor/plate.png"));
			GL11.glDisable(GL11.GL_CULL_FACE);
			HumanoidModel armored = (HumanoidModel) modelCache.getModel("humanoid.armor");
			armored.b.l = this.helmet;
			armored.d.l = this.armor;
			armored.e.l = this.armor;
			armored.f.l = this.armor;
			armored.g.l = false;
			armored.h.l = false;
			armored.b.g = model.b.g;
			armored.b.f = model.b.f;
			armored.e.f = model.e.f;
			armored.e.h = model.e.h;
			armored.f.f = model.f.f;
			armored.f.h = model.f.h;
			armored.g.f = model.g.f;
			armored.h.f = model.h.f;
			armored.b.a(var7);
			armored.d.a(var7);
			armored.e.a(var7);
			armored.f.a(var7);
			armored.g.a(var7);
			armored.h.a(var7);
			GL11.glEnable(GL11.GL_CULL_FACE);
		}

		GL11.glDisable(GL11.GL_ALPHA_TEST);
	}
}
