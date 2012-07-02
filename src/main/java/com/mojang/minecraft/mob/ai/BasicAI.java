package com.mojang.minecraft.mob.ai;

import com.mojang.minecraft.Entity;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.mob.Mob;
import com.mojang.minecraft.mob.ai.AI;
import java.util.List;
import java.util.Random;

public class BasicAI extends AI {

	public static final long serialVersionUID = 0L;
	public Random random = new Random();
	public float xxa;
	public float yya;
	protected float yRotA;
	public Level level;
	public Mob mob;
	public boolean jumping = false;
	protected int attackDelay = 0;
	public float runSpeed = 0.7F;
	protected int noActionTime = 0;
	public Entity attackTarget = null;

	public void tick(Level level, Mob mob) {
		this.noActionTime++;
		
		Entity player = level.getPlayer();
		if (this.noActionTime > 600 && this.random.nextInt(800) == 0 && player != null) {
			float xDistance = player.x - mob.x;
			float yDistance = player.y - mob.y;
			float zDistance = player.z - mob.z;
			float sqDistance = xDistance * xDistance + yDistance * yDistance + zDistance * zDistance;
			
			if (sqDistance < 1024.0F) {
				this.noActionTime = 0;
			} else {
				mob.remove();
			}
		}

		this.level = level;
		this.mob = mob;
		if (this.attackDelay > 0) {
			this.attackDelay--;
		}

		if (mob.health <= 0) {
			this.jumping = false;
			this.xxa = 0;
			this.yya = 0;
			this.yRotA = 0;
		} else {
			this.update();
		}

		boolean water = mob.isInWater();
		boolean lava = mob.isInLava();
		if (this.jumping) {
			if (water || lava) {
				mob.yd += 0.04F;
			} else if (mob.onGround) {
				this.jumpFromGround();
			}
		}

		this.xxa *= 0.98F;
		this.yya *= 0.98F;
		
		this.yRotA *= 0.9F;
		mob.travel(this.xxa, this.yya);
		List<Entity> entities = level.findEntities(mob, mob.bb.grow(0.2F, 0.0F, 0.2F));
		if (entities != null && entities.size() > 0) {
			for (Entity e : entities) {
				if (e.isPushable()) {
					e.push(mob);
				}
			}
		}

	}

	protected void jumpFromGround() {
		this.mob.yd = 0.42F;
	}

	public void update() {
		if (this.random.nextFloat() < 0.07F) {
			this.xxa = (this.random.nextFloat() - 0.5F) * this.runSpeed;
			this.yya = this.random.nextFloat() * this.runSpeed;
		}

		this.jumping = this.random.nextFloat() < 0.01F;
		if (this.random.nextFloat() < 0.04F) {
			this.yRotA = (this.random.nextFloat() - 0.5F) * 60.0F;
		}

		this.mob.yRot += this.yRotA;
		this.mob.xRot = this.defaultLookAngle;
		if (this.attackTarget != null) {
			this.yya = this.runSpeed;
			this.jumping = this.random.nextFloat() < 0.04F;
		}

		boolean var1 = this.mob.isInWater();
		boolean var2 = this.mob.isInLava();
		if (var1 || var2) {
			this.jumping = this.random.nextFloat() < 0.8F;
		}

	}

	public void beforeRemove() {
	}

	public void hurt(Entity cause, int damage) {
		this.noActionTime = 0;
	}
}
