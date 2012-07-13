package com.mojang.minecraft.model;

import com.mojang.minecraft.model.AnimalModel;
import com.mojang.minecraft.model.ModelPart;

public final class SheepFurModel extends AnimalModel {

	public SheepFurModel() {
		super(12, 0.0F);
		this.b = new ModelPart(0, 0);
		this.b.addBox(-3.0F, -4.0F, -4.0F, 6, 6, 6, 0.6F);
		this.b.setPos(0.0F, 6.0F, -8.0F);
		this.c = new ModelPart(28, 8);
		this.c.addBox(-4.0F, -10.0F, -7.0F, 8, 16, 6, 1.75F);
		this.c.setPos(0.0F, 5.0F, 2.0F);
		float var1 = 0.5F;
		this.d = new ModelPart(0, 16);
		this.d.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, var1);
		this.d.setPos(-3.0F, 12.0F, 7.0F);
		this.e = new ModelPart(0, 16);
		this.e.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, var1);
		this.e.setPos(3.0F, 12.0F, 7.0F);
		this.f = new ModelPart(0, 16);
		this.f.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, var1);
		this.f.setPos(-3.0F, 12.0F, -5.0F);
		this.g = new ModelPart(0, 16);
		this.g.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, var1);
		this.g.setPos(3.0F, 12.0F, -5.0F);
	}
}
