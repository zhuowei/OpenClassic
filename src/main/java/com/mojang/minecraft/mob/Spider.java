package com.mojang.minecraft.mob;

import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.mob.QuadrupedMob;
import com.mojang.minecraft.mob.ai.JumpAttackAI;

public class Spider extends QuadrupedMob {

	public static final long serialVersionUID = 0L;

	public Spider(Level level, float x, float y, float z) {
		super(level, x, y, z);
		this.heightOffset = 0.72F;
		this.modelName = "spider";
		this.textureName = "/mob/spider.png";
		this.setSize(1.4F, 0.9F);
		this.setPos(x, y, z);
		this.deathScore = 105;
		this.bobStrength = 0.0F;
		this.ai = new JumpAttackAI();
	}
}
