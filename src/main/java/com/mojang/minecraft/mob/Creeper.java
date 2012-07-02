package com.mojang.minecraft.mob;

import java.io.Serializable;

import ch.spacebase.openclassic.api.block.VanillaBlock;

import com.mojang.minecraft.Entity;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.mob.Mob;
import com.mojang.minecraft.mob.ai.BasicAttackAI;
import com.mojang.minecraft.particle.TerrainParticle;
import com.mojang.util.MathHelper;

public class Creeper extends Mob {

	public static final long serialVersionUID = 0L;

	public Creeper(Level level, float x, float y, float z) {
		super(level);
		this.heightOffset = 1.62F;
		this.modelName = "creeper";
		this.textureName = "/mob/creeper.png";
		this.ai = new CreeperAI();
		this.ai.defaultLookAngle = 45;
		this.deathScore = 200;
		this.setPos(x, y, z);
	}

	public float getBrightness(float additionalTicks) {
		float brightness = (20 - this.health) / 20.0F;
		return ((MathHelper.sin(this.tickCount + additionalTicks) * 0.5F + 0.5F) * brightness * 0.5F + 0.25F + brightness * 0.25F) * super.getBrightness(additionalTicks);
	}
	
	public static class CreeperAI extends BasicAttackAI implements Serializable {
		public static final long serialVersionUID = 0L;

		public final boolean attack(Entity entity) {
			if (super.attack(entity)) {
				this.mob.hurt(entity, 6);
				return true;
			}
			
			return false;
		}

		public final void beforeRemove() {
			this.level.explode(this.mob, this.mob.x, this.mob.y, this.mob.z, 4);

			for (int count = 0; count < 500; ++count) {
				float particleX = (float) this.random.nextGaussian();
				float particleY = (float) this.random.nextGaussian();
				float particleZ = (float) this.random.nextGaussian();
				float var6 = MathHelper.sqrt(particleX * particleX + particleY * particleY + particleZ * particleZ);
				float var7 = particleX / var6 / var6;
				float var8 = particleY / var6 / var6;
				float var9 = particleZ / var6 / var6;
				this.level.particleEngine.spawnParticle(new TerrainParticle(this.level, this.mob.x + particleX, this.mob.y + particleY, this.mob.z + particleZ, var7, var8, var9, VanillaBlock.LEAVES));
			}
		}
	}
}
