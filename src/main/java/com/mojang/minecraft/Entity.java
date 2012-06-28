package com.mojang.minecraft;

import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.StepSound;
import ch.spacebase.openclassic.api.block.VanillaBlock;

import com.mojang.minecraft.level.BlockMap;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.model.Vector;
import com.mojang.minecraft.net.PositionUpdate;
import com.mojang.minecraft.phys.AABB;
import com.mojang.minecraft.render.TextureManager;
import com.mojang.util.MathHelper;
import java.io.Serializable;
import java.util.ArrayList;

public abstract class Entity implements Serializable {

	public static final long serialVersionUID = 0L;
	public Level level;
	public float xo;
	public float yo;
	public float zo;
	public float x;
	public float y;
	public float z;
	public float xd;
	public float yd;
	public float zd;
	public float yRot;
	public float xRot;
	public float yRotO;
	public float xRotO;
	public AABB bb;
	public boolean onGround = false;
	public boolean horizontalCollision = false;
	public boolean collision = false;
	public boolean slide = true;
	public boolean removed = false;
	public float heightOffset = 0.0F;
	public float bbWidth = 0.6F;
	public float bbHeight = 1.8F;
	public float walkDistO = 0.0F;
	public float walkDist = 0.0F;
	public boolean makeStepSound = true;
	public float fallDistance = 0.0F;
	private int nextStep = 1;
	public BlockMap blockMap;
	public float xOld;
	public float yOld;
	public float zOld;
	public int textureId = 0;
	public float ySlideOffset = 0.0F;
	public float footSize = 0.0F;
	public boolean noPhysics = false;
	public float pushthrough = 0.0F;
	public boolean hovered = false;

	public Entity(Level level) {
		this.level = level;
		this.setPos(0.0F, 0.0F, 0.0F);
	}

	public void resetPos() {
		this.resetPos(null);
	}
	
	public void resetPos(Position pos) {
		if (pos != null) {
			pos = pos.clone();
			while(pos.getY() > 0 && this.level.getCubes(this.bb).size() != 0) {
				pos.setY(pos.getY() + 1);
				this.setPos((float) pos.getX(), (float) pos.getY(), (float) pos.getZ());
			}
			
			this.xd = 0;
			this.yd = 0;
			this.zd = 0;
			this.yRot = pos.getYaw();
			this.xRot = pos.getPitch();
		} else if (this.level != null) {
			float x = this.level.xSpawn + 0.5F;
			float y = this.level.ySpawn;

			for (float z = this.level.zSpawn + 0.5F; y > 0.0F; ++y) {
				this.setPos(x, y, z);
				if (this.level.getCubes(this.bb).size() == 0) break;
			}

			this.xd = 0;
			this.yd = 0;
			this.zd = 0;
			this.yRot = this.level.rotSpawn;
			this.xRot = 0;
		}
	}

	public void remove() {
		this.removed = true;
	}

	public void setSize(float width, float height) {
		this.bbWidth = width;
		this.bbHeight = height;
	}

	public void setPos(PositionUpdate pos) {
		if (pos.position) {
			this.setPos(pos.x, pos.y, pos.z);
		} else {
			this.setPos(this.x, this.y, this.z);
		}

		if (pos.rotation) {
			this.setRot(pos.yaw, pos.pitch);
		} else {
			this.setRot(this.yRot, this.xRot);
		}
	}

	protected void setRot(float yaw, float pitch) {
		this.yRot = yaw;
		this.xRot = pitch;
	}

	public void setPos(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		float widthCenter = this.bbWidth / 2.0F;
		float heightCenter = this.bbHeight / 2.0F;
		this.bb = new AABB(x - widthCenter, y - heightCenter, z - widthCenter, x + widthCenter, y + heightCenter, z + widthCenter);
	}

	public void turn(float yaw, float pitch) {
		float oldPitch = this.xRot;
		float oldYaw = this.yRot;
		this.yRot = (float) (this.yRot + yaw * 0.15D);
		this.xRot = (float) (this.xRot - pitch * 0.15D);
		if (this.xRot < -90.0F) {
			this.xRot = -90.0F;
		}

		if (this.xRot > 90.0F) {
			this.xRot = 90.0F;
		}

		this.xRotO += this.xRot - oldPitch;
		this.yRotO += this.yRot - oldYaw;
	}

	public void interpolateTurn(float yaw, float pitch) {
		this.yRot = (float) (this.yRot + yaw * 0.15D);
		this.xRot = (float) (this.xRot - pitch * 0.15D);
		if (this.xRot < -90.0F) {
			this.xRot = -90.0F;
		}

		if (this.xRot > 90.0F) {
			this.xRot = 90.0F;
		}
	}

	public void tick() {
		this.walkDistO = this.walkDist;
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		this.xRotO = this.xRot;
		this.yRotO = this.yRot;
	}

	public boolean isFree(float x, float y, float z, float radius) {
		AABB grown = this.bb.grow(radius, radius, radius).cloneMove(x, y, z);
		return this.level.getCubes(grown).size() <= 0 || !this.level.containsAnyLiquid(grown);
	}

	public boolean isFree(float x, float y, float z) {
		AABB moved = this.bb.cloneMove(x, y, z);
		return this.level.getCubes(moved).size() <= 0 || !this.level.containsAnyLiquid(moved);
	}

	public void move(float x, float y, float z) {
		if (this.noPhysics) {
			this.bb.move(x, y, z);
			this.x = (this.bb.x0 + this.bb.x1) / 2.0F;
			this.y = this.bb.y0 + this.heightOffset - this.ySlideOffset;
			this.z = (this.bb.z0 + this.bb.z1) / 2.0F;
		} else {
			float oldEntityX = this.x;
			float oldEntityZ = this.z;
			float oldX = x;
			float oldY = y;
			float oldZ = z;
			AABB copy = this.bb.copy();
			ArrayList<AABB> cubes = this.level.getCubes(this.bb.expand(x, y, z));

			for (AABB cube : cubes) {
				y = cube.clipYCollide(this.bb, y);
			}

			this.bb.move(0.0F, y, 0.0F);
			if (!this.slide && oldY != y) {
				x = 0;
				y = 0;
				z = 0;
			}

			boolean stepFurther = this.onGround || oldY != y && oldY < 0.0F;

			for (AABB cube : cubes) {
				x = cube.clipXCollide(this.bb, x);
			}

			this.bb.move(x, 0.0F, 0.0F);
			if (!this.slide && oldX != x) {
				z = 0.0F;
				y = 0.0F;
				x = 0.0F;
			}

			for (AABB cube : cubes) {
				z = cube.clipZCollide(this.bb, z);
			}

			this.bb.move(0.0F, 0.0F, z);
			if (!this.slide && oldZ != z) {
				x = 0;
				y = 0;
				z = 0;
			}

			if (this.footSize > 0 && stepFurther && this.ySlideOffset < 0.05F && (oldX != x || oldZ != z)) {
				float newX = x;
				float newY = y;
				float newZ = z;
				x = oldX;
				y = this.footSize;
				z = oldZ;
				AABB newCopy = this.bb.copy();
				this.bb = copy.copy();
				cubes = this.level.getCubes(this.bb.expand(oldX, y, oldZ));

				for (AABB cube : cubes) {
					y = cube.clipYCollide(this.bb, y);
				}

				this.bb.move(0.0F, y, 0.0F);
				if (!this.slide && oldY != y) {
					z = 0.0F;
					y = 0.0F;
					x = 0.0F;
				}

				for (AABB cube : cubes) {
					x = cube.clipXCollide(this.bb, x);
				}

				this.bb.move(x, 0.0F, 0.0F);
				if (!this.slide && oldX != x) {
					z = 0.0F;
					y = 0.0F;
					x = 0.0F;
				}

				for (AABB cube : cubes) {
					z = cube.clipZCollide(this.bb, z);
				}

				this.bb.move(0.0F, 0.0F, z);
				if (!this.slide && oldZ != z) {
					z = 0.0F;
					y = 0.0F;
					x = 0.0F;
				}

				if (newX * newX + newZ * newZ >= x * x + z * z) {
					x = newX;
					y = newY;
					z = newZ;
					this.bb = newCopy.copy();
				} else {
					this.ySlideOffset = (float) (this.ySlideOffset + 0.5D);
				}
			}

			this.horizontalCollision = oldX != x || oldZ != z;
			this.onGround = oldY != y && oldY < 0;
			this.collision = this.horizontalCollision || oldY != y;
			if (this.onGround) {
				if (this.fallDistance > 0) {
					this.causeFallDamage(this.fallDistance);
					this.fallDistance = 0;
				}
			} else if (y < 0.0F) {
				this.fallDistance -= y;
			}

			if (oldX != x) {
				this.xd = 0.0F;
			}

			if (oldY != y) {
				this.yd = 0.0F;
			}

			if (oldZ != z) {
				this.zd = 0.0F;
			}

			this.x = (this.bb.x0 + this.bb.x1) / 2.0F;
			this.y = this.bb.y0 + this.heightOffset - this.ySlideOffset;
			this.z = (this.bb.z0 + this.bb.z1) / 2.0F;
			float xDiff = this.x - oldEntityX;
			float zDiff = this.z - oldEntityZ;
			this.walkDist = (float) (this.walkDist + MathHelper.sqrt(xDiff * xDiff + zDiff * zDiff) * 0.6D);
			if (this.makeStepSound) {
				int id = this.level.getTile((int) this.x, (int) (this.y - 0.2F - this.heightOffset), (int) this.z);
				if (this.walkDist > this.nextStep && id > 0) {
					this.nextStep++;
					StepSound step = Blocks.fromId(id).getStepSound();
					if (step != StepSound.NONE) {
						this.playSound(step.getSound(), step.getVolume() * 0.75F, step.getPitch());
					}
				}
			}

			this.ySlideOffset *= 0.4F;
		}
	}

	protected void causeFallDamage(float distance) {
	}

	public boolean isInWater() {
		return this.level.containsLiquid(this.bb.grow(0.0F, -0.4F, 0.0F), VanillaBlock.WATER);
	}

	public boolean isUnderWater() {
		int block = this.level.getTile((int) this.x, (int) (this.y + 0.12F), (int) this.z);
		return block != 0 && (Blocks.fromId(block) == VanillaBlock.WATER || Blocks.fromId(block) == VanillaBlock.STATIONARY_WATER);
	}

	public boolean isInLava() {
		return this.level.containsLiquid(this.bb.grow(0.0F, -0.4F, 0.0F), VanillaBlock.LAVA);
	}

	public void moveRelative(float relX, float relZ, float var3) {
		this.moveRelative(relX, relZ, var3, 1);
	}
	
	public void moveRelative(float relX, float relZ, float var3, float speed) {
		float var4 = MathHelper.sqrt(relX * relX + relZ * relZ);
		if (var4 >= 0.01F) {
			if (var4 < 1.0F) {
				var4 = 1.0F;
			}

			var4 = var3 / var4;
			relX *= var4;
			relZ *= var4;
			var3 = MathHelper.sin(this.yRot * (float) Math.PI / 180);
			var4 = MathHelper.cos(this.yRot * (float) Math.PI / 180);
			
			relX *= speed;
			relZ *= speed;
			
			this.xd += relX * var4 - relZ * var3;
			this.zd += relZ * var4 + relX * var3;
		}
	}

	public boolean isLit() {
		return this.level.isLit((int) this.x, (int) this.y, (int) this.z);
	}

	public float getBrightness(float var1) {
		int y = (int) (this.y + this.heightOffset / 2.0F - 0.5F);
		return this.level.getBrightness((int) this.x, y, (int) this.z);
	}

	public void render(TextureManager textures, float var2) {
	}

	public void setLevel(Level level) {
		this.level = level;
	}

	public void playSound(String sound, float volume, float pitch) {
		this.level.playSound(sound, this, volume, pitch);
	}

	public void moveTo(float x, float y, float z, float yaw, float pitch) {
		this.xo = x;
		this.x = x;
		this.yo = y;
		this.y = y;
		this.zo = z;
		this.z = z;
		this.yRot = yaw;
		this.xRot = pitch;
		this.setPos(x, y, z);
	}

	public float distanceTo(Entity other) {
		float xDistance = this.x - other.x;
		float yDistance = this.y - other.y;
		float zDistance = this.z - other.z;
		return MathHelper.sqrt(xDistance * xDistance + yDistance * yDistance + zDistance * zDistance);
	}

	public float distanceTo(float x, float y, float z) {
		float xDistance = this.x - x;
		float yDistance = this.y - y;
		float zDistance = this.z - z;
		return MathHelper.sqrt(xDistance * xDistance + yDistance * yDistance + zDistance * zDistance);
	}

	public float distanceToSqr(Entity other) {
		float xDistance = this.x - other.x;
		float yDistance = this.y - other.y;
		float zDistance = this.z - other.z;
		return xDistance * xDistance + yDistance * yDistance + zDistance * zDistance;
	}

	public void playerTouch(Entity player) {
	}

	public void push(Entity entity) {
		float xDiff = entity.x - this.x;
		float zDiff = entity.z - this.z;
		float sqXZDiff = xDiff * xDiff + zDiff * zDiff;
		if (sqXZDiff >= 0.01F) {
			float xzDiff = MathHelper.sqrt(sqXZDiff);
			xDiff /= xzDiff;
			zDiff /= xzDiff;
			xDiff /= xzDiff;
			zDiff /= xzDiff;
			xDiff *= 0.05F;
			zDiff *= 0.05F;
			xDiff *= 1.0F - this.pushthrough;
			zDiff *= 1.0F - this.pushthrough;
			this.push(-xDiff, 0.0F, -zDiff);
			entity.push(xDiff, 0.0F, zDiff);
		}

	}

	protected void push(float x, float y, float z) {
		this.xd += x;
		this.yd += y;
		this.zd += z;
	}

	public void hurt(Entity cause, int damage) {
	}

	public boolean intersects(float x1, float y1, float z1, float x2, float y2, float z2) {
		return this.bb.intersects(x1, y1, z1, x2, y2, z2);
	}

	public boolean isPickable() {
		return false;
	}

	public boolean isPushable() {
		return false;
	}

	public boolean isShootable() {
		return false;
	}

	public void awardKillScore(Entity entity, int amount) {
	}

	public boolean shouldRender(Vector point) {
		float x = this.x - point.x;
		float y = this.y - point.y;
		float z = this.z - point.z;
		float sqDistance = x * x + y * y + z * z;
		return this.shouldRenderAtSqrDistance(sqDistance);
	}

	public boolean shouldRenderAtSqrDistance(float sqDistance) {
		float sqSize = this.bb.getSize() * 64.0F;
		return sqDistance < sqSize * sqSize;
	}

	public int getTexture() {
		return this.textureId;
	}

	public boolean isCreativeModeAllowed() {
		return false;
	}

	public void renderHover(TextureManager textures, float var2) {
	}
}
