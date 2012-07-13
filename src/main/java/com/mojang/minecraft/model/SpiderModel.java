package com.mojang.minecraft.model;

import com.mojang.minecraft.model.Model;
import com.mojang.minecraft.model.ModelPart;
import com.mojang.util.MathHelper;

public final class SpiderModel extends Model {

	private ModelPart b = new ModelPart(32, 4);
	private ModelPart c;
	private ModelPart d;
	private ModelPart e;
	private ModelPart f;
	private ModelPart g;
	private ModelPart h;
	private ModelPart i;
	private ModelPart j;
	private ModelPart k;
	private ModelPart l;

	public SpiderModel() {
		this.b.addBox(-4.0F, -4.0F, -8.0F, 8, 8, 8, 0.0F);
		this.b.setPos(0.0F, 0.0F, -3.0F);
		this.c = new ModelPart(0, 0);
		this.c.addBox(-3.0F, -3.0F, -3.0F, 6, 6, 6, 0.0F);
		this.d = new ModelPart(0, 12);
		this.d.addBox(-5.0F, -4.0F, -6.0F, 10, 8, 12, 0.0F);
		this.d.setPos(0.0F, 0.0F, 9.0F);
		this.e = new ModelPart(18, 0);
		this.e.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, 0.0F);
		this.e.setPos(-4.0F, 0.0F, 2.0F);
		this.f = new ModelPart(18, 0);
		this.f.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, 0.0F);
		this.f.setPos(4.0F, 0.0F, 2.0F);
		this.g = new ModelPart(18, 0);
		this.g.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, 0.0F);
		this.g.setPos(-4.0F, 0.0F, 1.0F);
		this.h = new ModelPart(18, 0);
		this.h.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, 0.0F);
		this.h.setPos(4.0F, 0.0F, 1.0F);
		this.i = new ModelPart(18, 0);
		this.i.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, 0.0F);
		this.i.setPos(-4.0F, 0.0F, 0.0F);
		this.j = new ModelPart(18, 0);
		this.j.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, 0.0F);
		this.j.setPos(4.0F, 0.0F, 0.0F);
		this.k = new ModelPart(18, 0);
		this.k.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, 0.0F);
		this.k.setPos(-4.0F, 0.0F, -1.0F);
		this.l = new ModelPart(18, 0);
		this.l.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, 0.0F);
		this.l.setPos(4.0F, 0.0F, -1.0F);
	}

	public final void render(float var1, float var2, float var3, float var4, float var5, float var6) {
		this.b.yaw = var4 / 57.295776F;
		this.b.pitch = var5 / 57.295776F;
		var4 = 0.7853982F;
		this.e.roll = -var4;
		this.f.roll = var4;
		this.g.roll = -var4 * 0.74F;
		this.h.roll = var4 * 0.74F;
		this.i.roll = -var4 * 0.74F;
		this.j.roll = var4 * 0.74F;
		this.k.roll = -var4;
		this.l.roll = var4;
		var4 = 0.3926991F;
		this.e.yaw = var4 * 2.0F;
		this.f.yaw = -var4 * 2.0F;
		this.g.yaw = var4;
		this.h.yaw = -var4;
		this.i.yaw = -var4;
		this.j.yaw = var4;
		this.k.yaw = -var4 * 2.0F;
		this.l.yaw = var4 * 2.0F;
		var4 = -(MathHelper.cos(var1 * 0.6662F * 2.0F) * 0.4F) * var2;
		var5 = -(MathHelper.cos(var1 * 0.6662F * 2.0F + 3.1415927F) * 0.4F) * var2;
		float var7 = -(MathHelper.cos(var1 * 0.6662F * 2.0F + 1.5707964F) * 0.4F) * var2;
		float var8 = -(MathHelper.cos(var1 * 0.6662F * 2.0F + 4.712389F) * 0.4F) * var2;
		float var9 = Math.abs(MathHelper.sin(var1 * 0.6662F) * 0.4F) * var2;
		float var10 = Math.abs(MathHelper.sin(var1 * 0.6662F + 3.1415927F) * 0.4F) * var2;
		float var11 = Math.abs(MathHelper.sin(var1 * 0.6662F + 1.5707964F) * 0.4F) * var2;
		var2 = Math.abs(MathHelper.sin(var1 * 0.6662F + 4.712389F) * 0.4F) * var2;
		this.e.yaw += var4;
		this.f.yaw -= var4;
		this.g.yaw += var5;
		this.h.yaw -= var5;
		this.i.yaw += var7;
		this.j.yaw -= var7;
		this.k.yaw += var8;
		this.l.yaw -= var8;
		this.e.roll += var9;
		this.f.roll -= var9;
		this.g.roll += var10;
		this.h.roll -= var10;
		this.i.roll += var11;
		this.j.roll -= var11;
		this.k.roll += var2;
		this.l.roll -= var2;
		this.b.render(var6);
		this.c.render(var6);
		this.d.render(var6);
		this.e.render(var6);
		this.f.render(var6);
		this.g.render(var6);
		this.h.render(var6);
		this.i.render(var6);
		this.j.render(var6);
		this.k.render(var6);
		this.l.render(var6);
	}
}
