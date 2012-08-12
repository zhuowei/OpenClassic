package com.mojang.minecraft.model;

import com.mojang.minecraft.model.ModelPart;
import com.mojang.minecraft.model.ZombieModel;

public final class SkeletonModel extends ZombieModel {

	public SkeletonModel() {
		this.rightArm = new ModelPart(40, 16);
		this.rightArm.addBox(-1.0F, -2.0F, -1.0F, 2, 12, 2, 0.0F);
		this.rightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
		this.leftArm = new ModelPart(40, 16);
		this.leftArm.mirror = true;
		this.leftArm.addBox(-1.0F, -2.0F, -1.0F, 2, 12, 2, 0.0F);
		this.leftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
		this.rightLeg = new ModelPart(0, 16);
		this.rightLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 12, 2, 0.0F);
		this.rightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
		this.leftLeg = new ModelPart(0, 16);
		this.leftLeg.mirror = true;
		this.leftLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 12, 2, 0.0F);
		this.leftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
	}
}
