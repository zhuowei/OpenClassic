package com.mojang.minecraft.mob;

import ch.spacebase.openclassic.api.block.VanillaBlock;

import com.mojang.minecraft.Entity;
import com.mojang.minecraft.item.Item;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.mob.QuadrupedMob;

public class Pig extends QuadrupedMob {

	public static final long serialVersionUID = 0L;

	public Pig(Level level, float x, float y, float z) {
		super(level, x, y, z);
		this.heightOffset = 1.72F;
		this.modelName = "pig";
		this.textureName = "/mob/pig.png";
	}

	public void die(Entity cause) {
		if (cause != null) {
			cause.awardKillScore(this, 10);
		}

		int drops = (int) (Math.random() + Math.random() + 1.0D);

		for (int count = 0; count < drops; ++count) {
			this.level.addEntity(new Item(this.level, this.x, this.y, this.z, VanillaBlock.BROWN_MUSHROOM.getId()));
		}

		super.die(cause);
	}
}
