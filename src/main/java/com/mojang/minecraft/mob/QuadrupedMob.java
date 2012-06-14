package com.mojang.minecraft.mob;

import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.mob.Mob;

public class QuadrupedMob extends Mob {

	public static final long serialVersionUID = 0L;

	public QuadrupedMob(Level level, float x, float y, float z) {
		super(level);
		this.setSize(1.4F, 1.2F);
		this.setPos(x, y, z);
		this.modelName = "pig";
	}
}
