package com.mojang.minecraft.model;

import com.mojang.minecraft.model.Model;
import com.mojang.minecraft.model.ModelPart;
import com.mojang.util.MathHelper;

public class HumanoidModel extends Model {

	public ModelPart helmet;
	public ModelPart c;
	public ModelPart chestplate;
	public ModelPart leggings;
	public ModelPart arm;
	public ModelPart g;
	public ModelPart h;

	public HumanoidModel() {
		this(0.0F);
	}

	public HumanoidModel(float var1) {
		this.helmet = new ModelPart(0, 0);
		this.helmet.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, var1);
		this.c = new ModelPart(32, 0);
		this.c.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, var1 + 0.5F);
		this.chestplate = new ModelPart(16, 16);
		this.chestplate.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, var1);
		this.leggings = new ModelPart(40, 16);
		this.leggings.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, var1);
		this.leggings.setPos(-5.0F, 2.0F, 0.0F);
		this.arm = new ModelPart(40, 16);
		this.arm.invertVertices = true;
		this.arm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, var1);
		this.arm.setPos(5.0F, 2.0F, 0.0F);
		this.g = new ModelPart(0, 16);
		this.g.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, var1);
		this.g.setPos(-2.0F, 12.0F, 0.0F);
		this.h = new ModelPart(0, 16);
		this.h.invertVertices = true;
		this.h.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, var1);
		this.h.setPos(2.0F, 12.0F, 0.0F);
	}

	public final void render(float var1, float var2, float var3, float var4, float var5, float var6) {
		this.b(var1, var2, var3, var4, var5, var6);
		this.helmet.render(var6);
		this.chestplate.render(var6);
		this.leggings.render(var6);
		this.arm.render(var6);
		this.g.render(var6);
		this.h.render(var6);
	}

	public void b(float var1, float var2, float var3, float var4, float var5, float var6) {
		this.helmet.yaw = var4 / 57.295776F;
		this.helmet.pitch = var5 / 57.295776F;
		this.leggings.pitch = MathHelper.cos(var1 * 0.6662F + 3.1415927F) * 2.0F * var2;
		this.leggings.roll = (MathHelper.cos(var1 * 0.2312F) + 1.0F) * var2;
		this.arm.pitch = MathHelper.cos(var1 * 0.6662F) * 2.0F * var2;
		this.arm.roll = (MathHelper.cos(var1 * 0.2812F) - 1.0F) * var2;
		this.g.pitch = MathHelper.cos(var1 * 0.6662F) * 1.4F * var2;
		this.h.pitch = MathHelper.cos(var1 * 0.6662F + 3.1415927F) * 1.4F * var2;
		this.leggings.roll += MathHelper.cos(var3 * 0.09F) * 0.05F + 0.05F;
		this.arm.roll -= MathHelper.cos(var3 * 0.09F) * 0.05F + 0.05F;
		this.leggings.pitch += MathHelper.sin(var3 * 0.067F) * 0.05F;
		this.arm.pitch -= MathHelper.sin(var3 * 0.067F) * 0.05F;
	}
}
