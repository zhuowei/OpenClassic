package com.mojang.minecraft.mob;

import com.mojang.minecraft.Entity;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.mob.ai.AI;
import com.mojang.minecraft.mob.ai.BasicAI;
import com.mojang.minecraft.model.ModelManager;
import com.mojang.minecraft.render.TextureManager;
import com.mojang.util.MathHelper;
import org.lwjgl.opengl.GL11;

public class Mob extends Entity {

	public static final long serialVersionUID = 0L;
	public static final int ATTACK_DURATION = 5;
	public static final int TOTAL_AIR_SUPPLY = 300;
	public static final ModelManager modelCache = new ModelManager();
	public int invulnerableDuration = 20;
	public float rot;
	public float timeOffs;
	public float speed;
	public float rotA = (float) (Math.random() + 1.0D) * 0.01F;
	protected float yBodyRot = 0.0F;
	protected float yBodyRotO = 0.0F;
	protected float oRun;
	protected float run;
	protected float animStep;
	protected float animStepO;
	protected int tickCount = 0;
	public boolean hasHair = true;
	protected String textureName = "/char.png";
	public boolean allowAlpha = true;
	public float rotOffs = 0.0F;
	public String modelName = null;
	protected float bobStrength = 1.0F;
	protected int deathScore = 0;
	public float renderOffset = 0.0F;
	public int health = 20;
	public int lastHealth;
	public int invulnerableTime = 0;
	public int airSupply = 300;
	public int hurtTime;
	public int hurtDuration;
	public float hurtDir = 0.0F;
	public int deathTime = 0;
	public int attackTime = 0;
	public float oTilt;
	public float tilt;
	protected boolean dead = false;
	public AI ai;

	public Mob(Level level) {
		super(level);
		this.setPos(this.x, this.y, this.z);
		this.timeOffs = (float) Math.random() * 12398.0F;
		this.rot = (float) (Math.random() * 3.1415927410125732D * 2.0D);
		this.speed = 1.0F;
		this.ai = new BasicAI();
		this.footSize = 0.5F;
	}

	public boolean isPickable() {
		return !this.removed;
	}

	public boolean isPushable() {
		return !this.removed;
	}

	public final void tick() {
		super.tick();
		this.oTilt = this.tilt;
		if (this.attackTime > 0) {
			this.attackTime--;
		}

		if (this.hurtTime > 0) {
			this.hurtTime--;
		}

		if (this.invulnerableTime > 0) {
			this.invulnerableTime--;
		}

		if (this.health <= 0) {
			this.deathTime++;
			if (this.deathTime > 20) {
				if (this.ai != null) {
					this.ai.beforeRemove();
				}

				this.remove();
			}
		}

		if (this.isUnderWater()) {
			if (this.airSupply > 0) {
				this.airSupply--;
			} else {
				this.hurt(null, 2);
			}
		} else {
			this.airSupply = 300;
		}

		if (this.isInWater()) {
			this.fallDistance = 0;
		}

		if (this.isInLava()) {
			this.hurt(null, 10);
		}

		this.animStepO = this.animStep;
		this.yBodyRotO = this.yBodyRot;
		this.yRotO = this.yRot;
		this.xRotO = this.xRot;
		this.tickCount++;
		this.aiStep();
		float xDistance = this.x - this.xo;
		float zDistance = this.z - this.zo;
		float xzDistance = MathHelper.sqrt(xDistance * xDistance + zDistance * zDistance);
		float yaw = this.yBodyRot;
		float animStep = 0.0F;
		this.oRun = this.run;
		float friction = 0.0F;
		if (xzDistance > 0.05F) {
			friction = 1.0F;
			animStep = xzDistance * 3.0F;
			yaw = (float) Math.atan2(zDistance, xDistance) * 180.0F / 3.1415927F - 90.0F;
		}

		if (!this.onGround) {
			friction = 0.0F;
		}

		this.run += (friction - this.run) * 0.3F;

		for (xDistance = yaw - this.yBodyRot; xDistance < -180.0F; xDistance += 360.0F);

		while (xDistance >= 180) {
			xDistance -= 360;
		}

		this.yBodyRot += xDistance * 0.1;

		for (xDistance = this.yRot - this.yBodyRot; xDistance < -180.0F; xDistance += 360.0F);

		while (xDistance >= 180) {
			xDistance -= 360;
		}

		boolean negative = xDistance < -90 || xDistance >= 90;
		if (xDistance < -75) {
			xDistance = -75;
		}

		if (xDistance >= 75) {
			xDistance = 75;
		}

		this.yBodyRot = this.yRot - xDistance;
		this.yBodyRot += xDistance * 0.1F;
		if (negative) {
			animStep = -animStep;
		}

		while (this.yRot - this.yRotO < -180) {
			this.yRotO -= 360;
		}

		while (this.yRot - this.yRotO >= 180) {
			this.yRotO += 360;
		}

		while (this.yBodyRot - this.yBodyRotO < -180) {
			this.yBodyRotO -= 360;
		}

		while (this.yBodyRot - this.yBodyRotO >= 180) {
			this.yBodyRotO += 360;
		}

		while (this.xRot - this.xRotO < -180) {
			this.xRotO -= 360;
		}

		while (this.xRot - this.xRotO >= 180) {
			this.xRotO += 360;
		}

		this.animStep += animStep;
	}

	public void aiStep() {
		if (this.ai != null) {
			this.ai.tick(this.level, this);
		}
	}

	protected void bindTexture(TextureManager textures) {
		this.textureId = textures.bindTexture(this.textureName);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textureId);
	}

	public void render(TextureManager textures, float var2) {
		if (this.modelName != null) {
			float var3;
			if ((var3 = this.attackTime - var2) < 0.0F) {
				var3 = 0.0F;
			}

			while (this.yBodyRotO - this.yBodyRot < -180.0F) {
				this.yBodyRotO += 360.0F;
			}

			while (this.yBodyRotO - this.yBodyRot >= 180.0F) {
				this.yBodyRotO -= 360.0F;
			}

			while (this.xRotO - this.xRot < -180.0F) {
				this.xRotO += 360.0F;
			}

			while (this.xRotO - this.xRot >= 180.0F) {
				this.xRotO -= 360.0F;
			}

			while (this.yRotO - this.yRot < -180.0F) {
				this.yRotO += 360.0F;
			}

			while (this.yRotO - this.yRot >= 180.0F) {
				this.yRotO -= 360.0F;
			}

			float var4 = this.yBodyRotO + (this.yBodyRot - this.yBodyRotO) * var2;
			float var5 = this.oRun + (this.run - this.oRun) * var2;
			float var6 = this.yRotO + (this.yRot - this.yRotO) * var2;
			float var7 = this.xRotO + (this.xRot - this.xRotO) * var2;
			var6 -= var4;
			GL11.glPushMatrix();
			float var8 = this.animStepO + (this.animStep - this.animStepO) * var2;
			float var9;
			GL11.glColor3f(var9 = this.getBrightness(var2), var9, var9);
			var9 = 0.0625F;
			float var10 = -Math.abs(MathHelper.cos(var8 * 0.6662F)) * 5.0F * var5 * this.bobStrength - 23.0F;
			GL11.glTranslatef(this.xo + (this.x - this.xo) * var2, this.yo + (this.y - this.yo) * var2 - 1.62F + this.renderOffset, this.zo + (this.z - this.zo) * var2);
			float var11;
			if ((var11 = this.hurtTime - var2) > 0.0F || this.health <= 0) {
				if (var11 < 0.0F) {
					var11 = 0.0F;
				} else {
					var11 = MathHelper.sin((var11 /= this.hurtDuration) * var11 * var11 * var11 * 3.1415927F) * 14.0F;
				}

				float var12 = 0.0F;
				if (this.health <= 0) {
					var12 = (this.deathTime + var2) / 20.0F;
					if ((var11 += var12 * var12 * 800.0F) > 90.0F) {
						var11 = 90.0F;
					}
				}

				var12 = this.hurtDir;
				GL11.glRotatef(180.0F - var4 + this.rotOffs, 0.0F, 1.0F, 0.0F);
				GL11.glScalef(1.0F, 1.0F, 1.0F);
				GL11.glRotatef(-var12, 0.0F, 1.0F, 0.0F);
				GL11.glRotatef(-var11, 0.0F, 0.0F, 1.0F);
				GL11.glRotatef(var12, 0.0F, 1.0F, 0.0F);
				GL11.glRotatef(-(180.0F - var4 + this.rotOffs), 0.0F, 1.0F, 0.0F);
			}

			GL11.glTranslatef(0.0F, -var10 * var9, 0.0F);
			GL11.glScalef(1.0F, -1.0F, 1.0F);
			GL11.glRotatef(180.0F - var4 + this.rotOffs, 0.0F, 1.0F, 0.0F);
			if (!this.allowAlpha) {
				GL11.glDisable(GL11.GL_ALPHA_TEST);
			} else {
				GL11.glDisable(GL11.GL_CULL_FACE);
			}

			GL11.glScalef(-1.0F, 1.0F, 1.0F);
			modelCache.getModel(this.modelName).a = var3 / 5.0F;
			this.bindTexture(textures);
			this.renderModel(textures, var8, var2, var5, var6, var7, var9);
			if (this.invulnerableTime > this.invulnerableDuration - 10) {
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.75F);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
				this.bindTexture(textures);
				this.renderModel(textures, var8, var2, var5, var6, var7, var9);
				GL11.glDisable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			}

			GL11.glEnable(GL11.GL_ALPHA_TEST);
			if (this.allowAlpha) {
				GL11.glEnable(GL11.GL_CULL_FACE);
			}

			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glPopMatrix();
		}
	}

	public void renderModel(TextureManager textures, float var2, float var3, float var4, float var5, float var6, float var7) {
		modelCache.getModel(this.modelName).a(var2, var4, this.tickCount + var3, var5, var6, var7);
	}

	public void heal(int amount) {
		if (this.health > 0) {
			this.health += amount;
			if (this.health > 20) {
				this.health = 20;
			}

			this.invulnerableTime = this.invulnerableDuration / 2;
		}
	}

	public void hurt(Entity cause, int damage) {
		if (!this.level.creativeMode) {
			if (this.health > 0) {
				this.ai.hurt(cause, damage);
				if (this.invulnerableTime > this.invulnerableDuration / 2.0F) {
					if (this.lastHealth - damage >= this.health) {
						return;
					}

					this.health = this.lastHealth - damage;
				} else {
					this.lastHealth = this.health;
					this.invulnerableTime = this.invulnerableDuration;
					this.health -= damage;
					this.hurtTime = this.hurtDuration = 10;
				}

				this.hurtDir = 0;
				if (cause != null) {
					float xDistance = cause.x - this.x;
					float zDistance = cause.z - this.z;
					this.hurtDir = (float) (Math.atan2(zDistance, xDistance) * 180 / Math.PI) - this.yRot;
					this.knockback(cause, damage, xDistance, zDistance);
				} else {
					this.hurtDir = ((int) (Math.random() * 2.0D) * 180);
				}

				if (this.health <= 0) {
					this.die(cause);
				}
			}
		}
	}

	public void knockback(Entity entity, int damage, float xDistance, float zDistance) {
		float var5 = MathHelper.sqrt(xDistance * xDistance + zDistance * zDistance);
		float var6 = 0.4F;
		this.xd /= 2.0F;
		this.yd /= 2.0F;
		this.zd /= 2.0F;
		this.xd -= xDistance / var5 * var6;
		this.yd += 0.4F;
		this.zd -= zDistance / var5 * var6;
		if (this.yd > 0.4F) {
			this.yd = 0.4F;
		}

	}

	public void die(Entity cause) {
		if (!this.level.creativeMode) {
			if (this.deathScore > 0 && cause != null) {
				cause.awardKillScore(this, this.deathScore);
			}

			this.dead = true;
		}
	}

	protected void causeFallDamage(float distance) {
		if (!this.level.creativeMode) {
			int damage = (int) Math.ceil((distance - 3));
			if (damage > 0) {
				this.hurt(null, damage);
			}

		}
	}

	public void travel(float x, float z) {
		if (this.isInWater()) {
			float y = this.y;
			this.moveRelative(x, z, 0.02F);
			this.move(this.xd, this.yd, this.zd);
			this.xd *= 0.8F;
			this.yd *= 0.8F;
			this.zd *= 0.8F;
			this.yd = (float) (this.yd - 0.02D);
			if (this.horizontalCollision && this.isFree(this.xd, this.yd + 0.6F - this.y + y, this.zd)) {
				this.yd = 0.3F;
			}
		} else if (this.isInLava()) {
			float y = this.y;
			this.moveRelative(x, z, 0.02F);
			this.move(this.xd, this.yd, this.zd);
			this.xd *= 0.5F;
			this.yd *= 0.5F;
			this.zd *= 0.5F;
			this.yd = (float) (this.yd - 0.02D);
			if (this.horizontalCollision && this.isFree(this.xd, this.yd + 0.6F - this.y + y, this.zd)) {
				this.yd = 0.3F;
			}
		} else {
			this.moveRelative(x, z, this.onGround ? 0.1F : 0.02F);
			this.move(this.xd, this.yd, this.zd);
			this.xd *= 0.91F;
			this.yd *= 0.98F;
			this.zd *= 0.91F;
			this.yd = (float) (this.yd - 0.08D);
			if (this.onGround) {
				float y = 0.6F;
				this.xd *= y;
				this.zd *= y;
			}
		}
	}

	public boolean isShootable() {
		return true;
	}
}
