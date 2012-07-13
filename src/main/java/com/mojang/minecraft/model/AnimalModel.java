package com.mojang.minecraft.model;

import com.mojang.minecraft.model.Model;
import com.mojang.minecraft.model.ModelPart;
import com.mojang.util.MathHelper;

public class AnimalModel extends Model {

	public ModelPart b = new ModelPart(0, 0);
	public ModelPart c;
	public ModelPart d;
	public ModelPart e;
	public ModelPart f;
	public ModelPart g;

	public AnimalModel(int var1, float var2) {
		this.b.addBox(-4.0F, -4.0F, -8.0F, 8, 8, 8, 0.0F);
		this.b.setPos(0.0F, (18 - var1), -6.0F);
		this.c = new ModelPart(28, 8);
		this.c.addBox(-5.0F, -10.0F, -7.0F, 10, 16, 8, 0.0F);
		this.c.setPos(0.0F, (17 - var1), 2.0F);
		this.d = new ModelPart(0, 16);
		this.d.addBox(-2.0F, 0.0F, -2.0F, 4, var1, 4, 0.0F);
		this.d.setPos(-3.0F, (24 - var1), 7.0F);
		this.e = new ModelPart(0, 16);
		this.e.addBox(-2.0F, 0.0F, -2.0F, 4, var1, 4, 0.0F);
		this.e.setPos(3.0F, (24 - var1), 7.0F);
		this.f = new ModelPart(0, 16);
		this.f.addBox(-2.0F, 0.0F, -2.0F, 4, var1, 4, 0.0F);
		this.f.setPos(-3.0F, (24 - var1), -5.0F);
		this.g = new ModelPart(0, 16);
		this.g.addBox(-2.0F, 0.0F, -2.0F, 4, var1, 4, 0.0F);
		this.g.setPos(3.0F, (24 - var1), -5.0F);
	}

	public final void render(float var1, float var2, float var3, float var4, float var5, float var6) {
		this.b.yaw = var4 / 57.295776F;
		this.b.pitch = var5 / 57.295776F;
		this.c.pitch = 1.5707964F;
		this.d.pitch = MathHelper.cos(var1 * 0.6662F) * 1.4F * var2;
		this.e.pitch = MathHelper.cos(var1 * 0.6662F + 3.1415927F) * 1.4F * var2;
		this.f.pitch = MathHelper.cos(var1 * 0.6662F + 3.1415927F) * 1.4F * var2;
		this.g.pitch = MathHelper.cos(var1 * 0.6662F) * 1.4F * var2;
		this.b.render(var6);
		this.c.render(var6);
		this.d.render(var6);
		this.e.render(var6);
		this.f.render(var6);
		this.g.render(var6);
	}
}
