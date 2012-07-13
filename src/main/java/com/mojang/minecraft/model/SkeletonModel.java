package com.mojang.minecraft.model;

import com.mojang.minecraft.model.ModelPart;
import com.mojang.minecraft.model.ZombieModel;

public final class SkeletonModel extends ZombieModel {

	public SkeletonModel() {
		this.leggings = new ModelPart(40, 16);
		this.leggings.addBox(-1.0F, -2.0F, -1.0F, 2, 12, 2, 0.0F);
		this.leggings.setPos(-5.0F, 2.0F, 0.0F);
		this.arm = new ModelPart(40, 16);
		this.arm.invertVertices = true;
		this.arm.addBox(-1.0F, -2.0F, -1.0F, 2, 12, 2, 0.0F);
		this.arm.setPos(5.0F, 2.0F, 0.0F);
		this.g = new ModelPart(0, 16);
		this.g.addBox(-1.0F, 0.0F, -1.0F, 2, 12, 2, 0.0F);
		this.g.setPos(-2.0F, 12.0F, 0.0F);
		this.h = new ModelPart(0, 16);
		this.h.invertVertices = true;
		this.h.addBox(-1.0F, 0.0F, -1.0F, 2, 12, 2, 0.0F);
		this.h.setPos(2.0F, 12.0F, 0.0F);
	}
}
