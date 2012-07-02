package com.mojang.minecraft.mob;

import java.io.Serializable;

import com.mojang.minecraft.item.Arrow;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.mob.Zombie;
import com.mojang.minecraft.mob.ai.BasicAttackAI;

public class Skeleton extends Zombie {

	public static final long serialVersionUID = 0L;

	public Skeleton(Level level, float x, float y, float z) {
		super(level, x, y, z);
		this.modelName = "skeleton";
		this.textureName = "/mob/skeleton.png";
		this.deathScore = 120;
		this.ai = new SkeletonAI(this);
		((SkeletonAI) this.ai).runSpeed = 0.3F;
		((SkeletonAI) this.ai).damage = 8;
	}

	public void shootArrow(Level level) {
		level.addEntity(new Arrow(level, this, this.x, this.y, this.z, this.yRot + 180.0F + (float) (Math.random() * 45.0D - 22.5D), this.xRot - (float) (Math.random() * 45.0D - 10.0D), 1.0F));
	}
	
	public static class SkeletonAI extends BasicAttackAI implements Serializable {
		public static final long serialVersionUID = 0L;

		private Skeleton parent;
		
		public SkeletonAI(Skeleton parent) {
			this.parent = parent;
		}
		
		public final void tick(Level level, Mob mob) {
			super.tick(level, mob);
			if (mob.health > 0 && this.random.nextInt(30) == 0 && this.attackTarget != null) {
				parent.shootArrow(level);
			}

		}

		public final void beforeRemove() {
			int arrows = (int) ((Math.random() + Math.random()) * 3.0D + 4.0D);

			for (int count = 0; count < arrows; ++count) {
				parent.level.addEntity(new Arrow(parent.level, parent.level.getPlayer(), parent.x, parent.y - 0.2F, parent.z, (float) Math.random() * 360.0F, -((float) Math.random()) * 60.0F, 0.4F));
			}
		}
	}
	
}
