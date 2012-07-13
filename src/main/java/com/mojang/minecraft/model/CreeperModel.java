package com.mojang.minecraft.model;

import com.mojang.minecraft.model.Model;
import com.mojang.minecraft.model.ModelPart;
import com.mojang.util.MathHelper;

public final class CreeperModel extends Model {

	private ModelPart b = new ModelPart(0, 0);
	private ModelPart c;
	private ModelPart d;
	private ModelPart e;
	private ModelPart f;
	private ModelPart g;
	private ModelPart h;

	public CreeperModel() {
		this.b.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F);
		this.c = new ModelPart(32, 0);
		this.c.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F + 0.5F);
		this.d = new ModelPart(16, 16);
		this.d.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F);
		this.e = new ModelPart(0, 16);
		this.e.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, 0.0F);
		this.e.setPos(-2.0F, 12.0F, 4.0F);
		this.f = new ModelPart(0, 16);
		this.f.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, 0.0F);
		this.f.setPos(2.0F, 12.0F, 4.0F);
		this.g = new ModelPart(0, 16);
		this.g.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, 0.0F);
		this.g.setPos(-2.0F, 12.0F, -4.0F);
		this.h = new ModelPart(0, 16);
		this.h.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, 0.0F);
		this.h.setPos(2.0F, 12.0F, -4.0F);
	}

	public final void render(float var1, float var2, float var3, float var4, float var5, float var6) {
		this.b.yaw = var4 / 57.295776F;
		this.b.pitch = var5 / 57.295776F;
		this.e.pitch = MathHelper.cos(var1 * 0.6662F) * 1.4F * var2;
		this.f.pitch = MathHelper.cos(var1 * 0.6662F + 3.1415927F) * 1.4F * var2;
		this.g.pitch = MathHelper.cos(var1 * 0.6662F + 3.1415927F) * 1.4F * var2;
		this.h.pitch = MathHelper.cos(var1 * 0.6662F) * 1.4F * var2;
		this.b.render(var6);
		this.d.render(var6);
		this.e.render(var6);
		this.f.render(var6);
		this.g.render(var6);
		this.h.render(var6);
	}
}
