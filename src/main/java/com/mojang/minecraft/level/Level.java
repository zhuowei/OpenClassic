package com.mojang.minecraft.level;

import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.event.EventFactory;
import ch.spacebase.openclassic.api.event.block.BlockPhysicsEvent;
import ch.spacebase.openclassic.api.event.level.SpawnChangeEvent;
import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.client.util.BlockUtils;

import com.mojang.minecraft.Entity;
import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.MovingObjectPosition;
import com.mojang.minecraft.item.PrimedTnt;
import com.mojang.minecraft.level.BlockMap;
import com.mojang.minecraft.level.TickNextTick;
import com.mojang.minecraft.particle.ParticleManager;
import com.mojang.minecraft.phys.AABB;
import com.mojang.minecraft.render.LevelRenderer;
import com.mojang.util.MathHelper;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Level implements Serializable {

	private static final Random rand = new Random();
	
	public static final long serialVersionUID = 0L;
	public int width;
	public int height;
	public int depth;
	public byte[] blocks;
	public String name;
	public String creator;
	public long createTime;
	public int xSpawn;
	public int ySpawn;
	public int zSpawn;
	public float rotSpawn;
	private transient ArrayList<LevelRenderer> renderers = new ArrayList<LevelRenderer>();
	private transient int[] highest;
	public transient Random random = new Random();
	private transient int c;
	private transient ArrayList<TickNextTick> tickNextTicks;
	public BlockMap blockMap;
	private boolean networkMode;
	public transient Minecraft rendererContext;
	public boolean creativeMode;
	public int waterLevel;
	public int skyColor;
	public int fogColor;
	public int cloudColor;
	int unprocessed;
	private int tickCount;
	public Entity player;
	public transient ParticleManager particleEngine;
	public transient Object font;
	public boolean growTrees;
	
	public transient ClientLevel openclassic;

	public Level() {
		this.c = this.random.nextInt();
		this.tickNextTicks = new ArrayList<TickNextTick>();
		this.networkMode = false;
		this.unprocessed = 0;
		this.tickCount = 0;
		this.growTrees = false;
		
		this.openclassic = new ClientLevel(this);
	}

	public void initTransient() {
		if (this.blocks == null) {
			throw new RuntimeException("The level is corrupt!");
		} else {
			this.renderers = new ArrayList<LevelRenderer>();
			this.highest = new int[this.width * this.height];
			Arrays.fill(this.highest, this.depth);
			this.calcLightDepths(0, 0, this.width, this.height);
			this.random = new Random();
			this.c = this.random.nextInt();
			this.tickNextTicks = new ArrayList<TickNextTick>();
			if (this.waterLevel == 0) {
				this.waterLevel = this.depth / 2;
			}

			if (this.skyColor == 0) {
				this.skyColor = 10079487;
			}

			if (this.fogColor == 0) {
				this.fogColor = 16777215;
			}

			if (this.cloudColor == 0) {
				this.cloudColor = 16777215;
			}

			if (this.xSpawn == 0 && this.ySpawn == 0 && this.zSpawn == 0) {
				this.findSpawn();
			}

			if (this.blockMap == null) {
				this.blockMap = new BlockMap(this.width, this.depth, this.height);
			}

		}
	}

	public void setData(int width, int depth, int height, byte[] blocks) {
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.blocks = blocks;
		this.highest = new int[width * height];
		Arrays.fill(this.highest, this.depth);
		this.calcLightDepths(0, 0, width, height);

		for (width = 0; width < this.renderers.size(); ++width) {
			this.renderers.get(width).refresh();
		}

		this.tickNextTicks.clear();
		this.findSpawn();
		this.initTransient();
		System.gc();
	}

	public void findSpawn() {
		Random var1 = new Random();
		int var2 = 0;

		int var3;
		int var4;
		int var5;
		do {
			++var2;
			var3 = var1.nextInt(this.width / 2) + this.width / 4;
			var4 = var1.nextInt(this.height / 2) + this.height / 4;
			var5 = this.getHighestTile(var3, var4) + 1;
			if (var2 == 10000) {
				this.xSpawn = var3;
				this.ySpawn = -100;
				this.zSpawn = var4;
				return;
			}
		} while (var5 <= this.getWaterLevel());

		this.xSpawn = var3;
		this.ySpawn = var5;
		this.zSpawn = var4;
	}

	public void calcLightDepths(int var1, int var2, int var3, int var4) {
		for (int var5 = var1; var5 < var1 + var3; ++var5) {
			for (int var6 = var2; var6 < var2 + var4; ++var6) {
				int var7 = this.highest[var5 + var6 * this.width];

				int var8;
				for (var8 = this.depth - 1; var8 > 0 && !this.isLightBlocker(var5, var8, var6); --var8) {
					;
				}

				this.highest[var5 + var6 * this.width] = var8;
				if (var7 != var8) {
					int var9 = var7 < var8 ? var7 : var8;
					var7 = var7 > var8 ? var7 : var8;

					for (var8 = 0; var8 < this.renderers.size(); ++var8) {
						this.renderers.get(var8).addChunks(var5 - 1, var9 - 1, var6 - 1, var5 + 1, var7 + 1, var6 + 1);
					}
				}
			}
		}

	}

	public void addListener(LevelRenderer var1) {
		this.renderers.add(var1);
	}

	public void finalize() {
	}

	public void removeListener(LevelRenderer var1) {
		this.renderers.remove(var1);
	}

	public boolean isLightBlocker(int var1, int var2, int var3) {
		BlockType block = Blocks.fromId(this.getTile(var1, var2, var3));
		return block != null && block.isOpaque();
	}

	public ArrayList<AABB> getCubes(AABB var1) {
		ArrayList<AABB> var2 = new ArrayList<AABB>();
		int var3 = (int) var1.x0;
		int var4 = (int) var1.x1 + 1;
		int var5 = (int) var1.y0;
		int var6 = (int) var1.y1 + 1;
		int var7 = (int) var1.z0;
		int var8 = (int) var1.z1 + 1;
		if (var1.x0 < 0.0F) {
			--var3;
		}

		if (var1.y0 < 0.0F) {
			--var5;
		}

		if (var1.z0 < 0.0F) {
			--var7;
		}

		for (; var3 < var4; ++var3) {
			for (int var9 = var5; var9 < var6; ++var9) {
				for (int var10 = var7; var10 < var8; ++var10) {
					AABB var11;
					if (var3 >= 0 && var9 >= 0 && var10 >= 0 && var3 < this.width && var9 < this.depth && var10 < this.height) {
						BlockType var12;
						if ((var12 = Blocks.fromId(this.getTile(var3, var9, var10))) != null && (var11 = BlockUtils.getCollisionBox(var12.getId(), var3, var9, var10)) != null && var1.intersectsInner(var11)) {
							var2.add(var11);
						}
					} else if ((var3 < 0 || var9 < 0 || var10 < 0 || var3 >= this.width || var10 >= this.height) && (var11 = BlockUtils.getCollisionBox(VanillaBlock.BEDROCK.getId(), var3, var9, var10)) != null && var1.intersectsInner(var11)) {
						var2.add(var11);
					}
				}
			}
		}

		return var2;
	}

	public void swap(int var1, int var2, int var3, int var4, int var5, int var6) {
		if (!this.networkMode) {
			int var7 = this.getTile(var1, var2, var3);
			int var8 = this.getTile(var4, var5, var6);
			this.setTileNoNeighborChange(var1, var2, var3, var8);
			this.setTileNoNeighborChange(var4, var5, var6, var7);
			this.updateNeighborsAt(var1, var2, var3, var8);
			this.updateNeighborsAt(var4, var5, var6, var7);
		}
	}

	public boolean setTileNoNeighborChange(int var1, int var2, int var3, int var4) {
		return this.networkMode ? false : this.netSetTileNoNeighborChange(var1, var2, var3, var4);
	}

	public boolean netSetTileNoNeighborChange(int var1, int var2, int var3, int var4) {
		if (var1 >= 0 && var2 >= 0 && var3 >= 0 && var1 < this.width && var2 < this.depth && var3 < this.height) {
			if (var4 == this.blocks[(var2 * this.height + var3) * this.width + var1]) {
				return false;
			} else {
				if (var4 == 0 && (var1 == 0 || var3 == 0 || var1 == this.width - 1 || var3 == this.height - 1) && var2 >= this.getGroundLevel() && var2 < this.getWaterLevel()) {
					var4 = VanillaBlock.WATER.getId();
				}

				this.blocks[(var2 * this.height + var3) * this.width + var1] = (byte) var4;
				this.calcLightDepths(var1, var3, 1, 1);

				for (var4 = 0; var4 < this.renderers.size(); ++var4) {
					this.renderers.get(var4).addChunks(var1 - 1, var2 - 1, var3 - 1, var1 + 1, var2 + 1, var3 + 1);
				}

				return true;
			}
		} else {
			return false;
		}
	}

	public boolean setTile(int var1, int var2, int var3, int var4) {
		if (this.networkMode) {
			return false;
		} else if (this.setTileNoNeighborChange(var1, var2, var3, var4)) {
			this.updateNeighborsAt(var1, var2, var3, var4);
			return true;
		} else {
			return false;
		}
	}

	public boolean netSetTile(int var1, int var2, int var3, int var4) {
		if (this.netSetTileNoNeighborChange(var1, var2, var3, var4)) {
			this.updateNeighborsAt(var1, var2, var3, var4);
			return true;
		} else {
			return false;
		}
	}

	public void updateNeighborsAt(int var1, int var2, int var3, int var4) {
		if(this.openclassic.getPhysicsEnabled()) {
			this.a(var1 - 1, var2, var3, var1, var2, var3, var4);
			this.a(var1 + 1, var2, var3, var1, var2, var3, var4);
			this.a(var1, var2 - 1, var3, var1, var2, var3, var4);
			this.a(var1, var2 + 1, var3, var1, var2, var3, var4);
			this.a(var1, var2, var3 - 1, var1, var2, var3, var4);
			this.a(var1, var2, var3 + 1, var1, var2, var3, var4);
		}
	}

	public boolean setTileNoUpdate(int var1, int var2, int var3, int var4) {
		if (var1 >= 0 && var2 >= 0 && var3 >= 0 && var1 < this.width && var2 < this.depth && var3 < this.height) {
			if (var4 == this.blocks[(var2 * this.height + var3) * this.width + var1]) {
				return false;
			} else {
				this.blocks[(var2 * this.height + var3) * this.width + var1] = (byte) var4;
				return true;
			}
		} else {
			return false;
		}
	}

	private void a(int var1, int var2, int var3, int nx, int nz, int ny, int var4) {
		if (var1 >= 0 && var2 >= 0 && var3 >= 0 && var1 < this.width && var2 < this.depth && var3 < this.height) {
			BlockType var5;
			if ((var5 = Blocks.fromId(this.blocks[(var2 * this.height + var3) * this.width + var1])) != null) {
				if(var5.getPhysics() != null) {
					var5.getPhysics().onNeighborChange(this.openclassic.getBlockAt(var1, var2, var3), this.openclassic.getBlockAt(nx, ny, nz));
				}
			}

		}
	}

	public boolean isLit(int var1, int var2, int var3) {
		return var1 >= 0 && var2 >= 0 && var3 >= 0 && var1 < this.width && var2 < this.depth && var3 < this.height ? var2 >= this.highest[var1 + var3 * this.width] : true;
	}

	public int getTile(int var1, int var2, int var3) {
		return var1 >= 0 && var2 >= 0 && var3 >= 0 && var1 < this.width && var2 < this.depth && var3 < this.height ? this.blocks[(var2 * this.height + var3) * this.width + var1] & 255 : 0;
	}

	public boolean isSolidTile(int var1, int var2, int var3) {
		BlockType var4 = Blocks.fromId(this.getTile(var1, var2, var3));
		return var4 != null && var4.isSolid();
	}

	public void tickEntities() {
		this.blockMap.tickAll();
	}

	public void tick() {
		this.tickCount++;
		int var1 = 1;

		int var2;
		for (var2 = 1; 1 << var1 < this.width; ++var1) {
			;
		}

		while (1 << var2 < this.height) {
			++var2;
		}

		int var3 = this.height - 1;
		int var4 = this.width - 1;
		int var5 = this.depth - 1;
		int var6;
		int var7;
		if (this.tickCount % 5 == 0) {
			var6 = this.tickNextTicks.size();

			for (var7 = 0; var7 < var6; ++var7) {
				TickNextTick next = this.tickNextTicks.remove(0);
				if (next.ticks > 0) {
					next.ticks--;
					this.tickNextTicks.add(next);
				} else {
					if (this.a(next.x, next.y, next.z)) {
						byte block = this.blocks[(next.y * this.height + next.z) * this.width + next.x];
						if(block == next.block && block > 0) {
							if(Blocks.fromId(block).getPhysics() != null && this.openclassic.getPhysicsEnabled()) {
								Blocks.fromId(block).getPhysics().update(this.openclassic.getBlockAt(next.x, next.y, next.z));
							}
						}
					}
				}
			}
		}

		this.unprocessed += this.width * this.height * this.depth;
		var6 = this.unprocessed / 200;
		this.unprocessed -= var6 * 200;

		for (var7 = 0; var7 < var6; ++var7) {
			this.c = this.c * 3 + 1013904223;
			int y = this.c >> 2;
			int x = (y) & var4;
			int z = y >> var1 & var3;
			y = y >> var1 + var2 & var5;
			byte var11 = this.blocks[(y * this.height + z) * this.width + x];
			if(Blocks.fromId(var11).getPhysics() != null && this.openclassic.getPhysicsEnabled() && !EventFactory.callEvent(new BlockPhysicsEvent(this.openclassic.getBlockAt(x, y, z))).isCancelled()) {
				Blocks.fromId(var11).getPhysics().update(this.openclassic.getBlockAt(x, y, z));
			}
		}
		
		this.openclassic.tick();
	}

	public int countInstanceOf(Class<? extends Entity> var1) {
		int var2 = 0;

		for (int var3 = 0; var3 < this.blockMap.all.size(); ++var3) {
			Entity var4 = this.blockMap.all.get(var3);
			if (var1.isAssignableFrom(var4.getClass())) {
				++var2;
			}
		}

		return var2;
	}

	private boolean a(int var1, int var2, int var3) {
		return var1 >= 0 && var2 >= 0 && var3 >= 0 && var1 < this.width && var2 < this.depth && var3 < this.height;
	}

	public float getGroundLevel() {
		return this.getWaterLevel() - 2.0F;
	}

	public float getWaterLevel() {
		return this.waterLevel;
	}

	public boolean containsAnyLiquid(AABB var1) {
		int var2 = (int) var1.x0;
		int var3 = (int) var1.x1 + 1;
		int var4 = (int) var1.y0;
		int var5 = (int) var1.y1 + 1;
		int var6 = (int) var1.z0;
		int var7 = (int) var1.z1 + 1;
		if (var1.x0 < 0.0F) {
			--var2;
		}

		if (var1.y0 < 0.0F) {
			--var4;
		}

		if (var1.z0 < 0.0F) {
			--var6;
		}

		if (var2 < 0) {
			var2 = 0;
		}

		if (var4 < 0) {
			var4 = 0;
		}

		if (var6 < 0) {
			var6 = 0;
		}

		if (var3 > this.width) {
			var3 = this.width;
		}

		if (var5 > this.depth) {
			var5 = this.depth;
		}

		if (var7 > this.height) {
			var7 = this.height;
		}

		for (int var10 = var2; var10 < var3; ++var10) {
			for (var2 = var4; var2 < var5; ++var2) {
				for (int var8 = var6; var8 < var7; ++var8) {
					BlockType var9;
					if ((var9 = Blocks.fromId(this.getTile(var10, var2, var8))) != null && var9.isLiquid()) {
						return true;
					}
				}
			}
		}

		return false;
	}

	public boolean containsLiquid(AABB var1, BlockType var2) {
		var2 = toMoving(var2);
		
		int var3 = (int) var1.x0;
		int var4 = (int) var1.x1 + 1;
		int var5 = (int) var1.y0;
		int var6 = (int) var1.y1 + 1;
		int var7 = (int) var1.z0;
		int var8 = (int) var1.z1 + 1;
		if (var1.x0 < 0.0F) {
			--var3;
		}

		if (var1.y0 < 0.0F) {
			--var5;
		}

		if (var1.z0 < 0.0F) {
			--var7;
		}

		if (var3 < 0) {
			var3 = 0;
		}

		if (var5 < 0) {
			var5 = 0;
		}

		if (var7 < 0) {
			var7 = 0;
		}

		if (var4 > this.width) {
			var4 = this.width;
		}

		if (var6 > this.depth) {
			var6 = this.depth;
		}

		if (var8 > this.height) {
			var8 = this.height;
		}

		for (int var11 = var3; var11 < var4; ++var11) {
			for (var3 = var5; var3 < var6; ++var3) {
				for (int var9 = var7; var9 < var8; ++var9) {
					BlockType type = Blocks.fromId(this.getTile(var11, var3, var9));
					if (type != null && toMoving(type) == var2) {
						return true;
					}
				}
			}
		}

		return false;
	}
	
	public static BlockType toMoving(BlockType block) {
		if(block == VanillaBlock.STATIONARY_LAVA) return VanillaBlock.LAVA;
		if(block == VanillaBlock.STATIONARY_WATER) return VanillaBlock.WATER;
		
		return block;
	}

	public void addToTickNextTick(int x, int y, int z, int block) {
		if (!this.networkMode) {
			TickNextTick next = new TickNextTick(x, y, z, block);
			if (block > 0) {
				next.ticks = Blocks.fromId(block).getTickDelay();
			}

			this.tickNextTicks.add(next);
		}
	}

	public boolean isFree(AABB var1) {
		return this.blockMap.getEntities((Entity) null, var1).size() == 0;
	}

	public List<Entity> findEntities(Entity var1, AABB var2) {
		return this.blockMap.getEntities(var1, var2);
	}

	public boolean isSolid(float var1, float var2, float var3, float var4) {
		return this.a(var1 - var4, var2 - var4, var3 - var4) ? true : (this.a(var1 - var4, var2 - var4, var3 + var4) ? true : (this.a(var1 - var4, var2 + var4, var3 - var4) ? true : (this.a(var1 - var4, var2 + var4, var3 + var4) ? true : (this.a(var1 + var4, var2 - var4, var3 - var4) ? true : (this.a(var1 + var4, var2 - var4, var3 + var4) ? true : (this.a(var1 + var4, var2 + var4, var3 - var4) ? true : this.a(var1 + var4, var2 + var4, var3 + var4)))))));
	}

	private boolean a(float var1, float var2, float var3) {
		int var4;
		return (var4 = this.getTile((int) var1, (int) var2, (int) var3)) > 0 && Blocks.fromId(var4).isSolid();
	}

	public int getHighestTile(int var1, int var2) {
		int var3;
		for (var3 = this.depth; (this.getTile(var1, var3 - 1, var2) == 0 || (Blocks.fromId(this.getTile(var1, var3 - 1, var2)) != null && Blocks.fromId(this.getTile(var1, var3 - 1, var2)).isLiquid())) && var3 > 0; --var3);

		return var3;
	}

	public void setSpawnPos(int var1, int var2, int var3, float var4) {
		Position old = new Position(this.openclassic, this.xSpawn, this.ySpawn, this.zSpawn, (byte) this.rotSpawn, (byte) 0);
		this.xSpawn = var1;
		this.ySpawn = var2;
		this.zSpawn = var3;
		this.rotSpawn = var4;
		
		EventFactory.callEvent(new SpawnChangeEvent(this.openclassic, old));
	}

	public float getBrightness(int var1, int var2, int var3) {
		return this.isLit(var1, var2, var3) ? 1.0F : 0.6F;
	}

	public float getCaveness(float var1, float var2, float var3, float var4) {
		int var5 = (int) var1;
		int var14 = (int) var2;
		int var6 = (int) var3;
		float var7 = 0.0F;
		float var8 = 0.0F;

		for (int var9 = var5 - 6; var9 <= var5 + 6; ++var9) {
			for (int var10 = var6 - 6; var10 <= var6 + 6; ++var10) {
				if (this.a(var9, var14, var10) && !this.isSolidTile(var9, var14, var10)) {
					float var11 = var9 + 0.5F - var1;

					float var12;
					float var13;
					for (var13 = (float) (Math.atan2((var12 = var10 + 0.5F - var3), var11) - (var4 * 3.1415927F / 180.0F) + 1.5707963705062866D); var13 < -3.1415927F; var13 += 6.2831855F) {
						;
					}

					while (var13 >= 3.1415927F) {
						var13 -= 6.2831855F;
					}

					if (var13 < 0.0F) {
						var13 = -var13;
					}

					var11 = MathHelper.sqrt(var11 * var11 + 4.0F + var12 * var12);
					var11 = 1.0F / var11;
					if (var13 > 1.0F) {
						var11 = 0.0F;
					}

					if (var11 < 0.0F) {
						var11 = 0.0F;
					}

					var8 += var11;
					if (this.isLit(var9, var14, var10)) {
						var7 += var11;
					}
				}
			}
		}

		if (var8 == 0.0F) {
			return 0.0F;
		} else {
			return var7 / var8;
		}
	}

	public float getCaveness(Entity var1) {
		float var2 = MathHelper.cos(-var1.yRot * 0.017453292F + (float) Math.PI);
		float var3 = MathHelper.sin(-var1.yRot * 0.017453292F + (float) Math.PI);
		float var4 = MathHelper.cos(-var1.xRot * 0.017453292F);
		float var5 = MathHelper.sin(-var1.xRot * 0.017453292F);
		float var6 = var1.x;
		float var7 = var1.y;
		float var21 = var1.z;
		float var8 = 1.6F;
		float var9 = 0.0F;
		float var10 = 0.0F;

		for (int var11 = 0; var11 <= 200; ++var11) {
			float var12 = ((float) var11 / (float) 200 - 0.5F) * 2.0F;
			int var13 = 0;

			while (var13 <= 200) {
				float var14 = ((float) var13 / (float) 200 - 0.5F) * var8;
				float var16 = var4 * var14 + var5;
				var14 = var4 - var5 * var14;
				float var17 = var2 * var12 + var3 * var14;
				var14 = var2 * var14 - var3 * var12;
				int var15 = 0;

				while (true) {
					if (var15 < 10) {
						float var18 = var6 + var17 * var15 * 0.8F;
						float var19 = var7 + var16 * var15 * 0.8F;
						float var20 = var21 + var14 * var15 * 0.8F;
						if (!this.a(var18, var19, var20)) {
							++var9;
							if (this.isLit((int) var18, (int) var19, (int) var20)) {
								++var10;
							}

							++var15;
							continue;
						}
					}

					++var13;
					break;
				}
			}
		}

		if (var9 == 0.0F) {
			return 0.0F;
		} else {
			float var22;
			if ((var22 = var10 / var9 / 0.1F) > 1.0F) {
				var22 = 1.0F;
			}

			var22 = 1.0F - var22;
			return 1.0F - var22 * var22 * var22;
		}
	}

	public byte[] copyBlocks() {
		return Arrays.copyOf(this.blocks, this.blocks.length);
	}

	public boolean isWater(int var1, int var2, int var3) {
		int var4;
		return (var4 = this.getTile(var1, var2, var3)) > 0 && (Blocks.fromId(var4) == VanillaBlock.WATER || Blocks.fromId(var4) == VanillaBlock.STATIONARY_WATER);
	}

	public void setNetworkMode(boolean var1) {
		this.networkMode = var1;
	}

	public MovingObjectPosition clip(com.mojang.minecraft.model.Vector var1, com.mojang.minecraft.model.Vector var2) {
		return this.clip(var1, var2, false);
	}
	
	public MovingObjectPosition clip(com.mojang.minecraft.model.Vector var1, com.mojang.minecraft.model.Vector var2, boolean selection) {
		if (!Float.isNaN(var1.x) && !Float.isNaN(var1.y) && !Float.isNaN(var1.z)) {
			if (!Float.isNaN(var2.x) && !Float.isNaN(var2.y) && !Float.isNaN(var2.z)) {
				int var3 = (int) Math.floor(var2.x);
				int var4 = (int) Math.floor(var2.y);
				int var5 = (int) Math.floor(var2.z);
				int var6 = (int) Math.floor(var1.x);
				int var7 = (int) Math.floor(var1.y);
				int var8 = (int) Math.floor(var1.z);
				int var9 = 20;

				while (var9-- >= 0) {
					if (Float.isNaN(var1.x) || Float.isNaN(var1.y) || Float.isNaN(var1.z)) {
						return null;
					}

					if (var6 == var3 && var7 == var4 && var8 == var5) {
						return null;
					}

					float var10 = 999.0F;
					float var11 = 999.0F;
					float var12 = 999.0F;
					if (var3 > var6) {
						var10 = var6 + 1.0F;
					}

					if (var3 < var6) {
						var10 = var6;
					}

					if (var4 > var7) {
						var11 = var7 + 1.0F;
					}

					if (var4 < var7) {
						var11 = var7;
					}

					if (var5 > var8) {
						var12 = var8 + 1.0F;
					}

					if (var5 < var8) {
						var12 = var8;
					}

					float var13 = 999.0F;
					float var14 = 999.0F;
					float var15 = 999.0F;
					float var16 = var2.x - var1.x;
					float var17 = var2.y - var1.y;
					float var18 = var2.z - var1.z;
					if (var10 != 999.0F) {
						var13 = (var10 - var1.x) / var16;
					}

					if (var11 != 999.0F) {
						var14 = (var11 - var1.y) / var17;
					}

					if (var12 != 999.0F) {
						var15 = (var12 - var1.z) / var18;
					}

					byte var24;
					if (var13 < var14 && var13 < var15) {
						if (var3 > var6) {
							var24 = 4;
						} else {
							var24 = 5;
						}

						var1.x = var10;
						var1.y += var17 * var13;
						var1.z += var18 * var13;
					} else if (var14 < var15) {
						if (var4 > var7) {
							var24 = 0;
						} else {
							var24 = 1;
						}

						var1.x += var16 * var14;
						var1.y = var11;
						var1.z += var18 * var14;
					} else {
						if (var5 > var8) {
							var24 = 2;
						} else {
							var24 = 3;
						}

						var1.x += var16 * var15;
						var1.y += var17 * var15;
						var1.z = var12;
					}

					com.mojang.minecraft.model.Vector var20;
					var6 = (int) ((var20 = new com.mojang.minecraft.model.Vector(var1.x, var1.y, var1.z)).x = (float) Math.floor(var1.x));
					if (var24 == 5) {
						--var6;
						++var20.x;
					}

					var7 = (int) (var20.y = (float) Math.floor(var1.y));
					if (var24 == 1) {
						--var7;
						++var20.y;
					}

					var8 = (int) (var20.z = (float) Math.floor(var1.z));
					if (var24 == 3) {
						--var8;
						++var20.z;
					}

					int var22 = this.getTile(var6, var7, var8);
					BlockType var21 = Blocks.fromId(var22);
					if (var22 > 0 && (Blocks.fromId(var22) != null && !Blocks.fromId(var22).isLiquid())) {
						MovingObjectPosition var23 = null;
						if(selection) {
							var23 = BlockUtils.clipSelection(var21.getId(), var6, var7, var8, var1, var2);
						} else {
							var23 = BlockUtils.clip(var21.getId(), var6, var7, var8, var1, var2);
						}
						
						if (Blocks.fromId(var22).getModel().getCollisionBox() != null) {
							if (var23 != null) {
								return var23;
							}
						} else {
							if(selection) {
								var23 = BlockUtils.clipSelection(var21.getId(), var6, var7, var8, var1, var2);
							} else {
								var23 = BlockUtils.clip(var21.getId(), var6, var7, var8, var1, var2);
							}
							
							if (var23 != null) {
								return var23;
							}
						}
					}
				}

				return null;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public void playSound(String name, Entity entity, float volume, float pitch) {
		if (this.rendererContext != null) {
			Minecraft mc = this.rendererContext;
			if (!mc.settings.sound) {
				return;
			}

			if (entity.distanceToSqr(mc.player) < 1024) {
				mc.audio.playSound(name, entity.x, entity.y, entity.z, volume, pitch);
			}
		}
	}

	public void playSound(String name, float x, float y, float z, float volume, float pitch) {
		if (this.rendererContext != null) {
			Minecraft mc = this.rendererContext;
			if (!mc.settings.sound) {
				return;
			}

			mc.audio.playSound(name, x, y, z, volume, pitch);
		}
	}

	public boolean maybeGrowTree(int var1, int var2, int var3) {
		int var4 = this.random.nextInt(3) + 4;
		boolean var5 = true;

		int var6;
		int var8;
		int var9;
		for (var6 = var2; var6 <= var2 + 1 + var4; ++var6) {
			byte var7 = 1;
			if (var6 == var2) {
				var7 = 0;
			}

			if (var6 >= var2 + 1 + var4 - 2) {
				var7 = 2;
			}

			for (var8 = var1 - var7; var8 <= var1 + var7 && var5; ++var8) {
				for (var9 = var3 - var7; var9 <= var3 + var7 && var5; ++var9) {
					if (var8 >= 0 && var6 >= 0 && var9 >= 0 && var8 < this.width && var6 < this.depth && var9 < this.height) {
						if ((this.blocks[(var6 * this.height + var9) * this.width + var8] & 255) != 0) {
							var5 = false;
						}
					} else {
						var5 = false;
					}
				}
			}
		}

		if (!var5) {
			return false;
		} else if ((this.blocks[((var2 - 1) * this.height + var3) * this.width + var1] & 255) == VanillaBlock.GRASS.getId() && var2 < this.depth - var4 - 1) {
			this.setTile(var1, var2 - 1, var3, VanillaBlock.DIRT.getId());

			int var13;
			for (var13 = var2 - 3 + var4; var13 <= var2 + var4; ++var13) {
				var8 = var13 - (var2 + var4);
				var9 = 1 - var8 / 2;

				for (int var10 = var1 - var9; var10 <= var1 + var9; ++var10) {
					int var12 = var10 - var1;

					for (var6 = var3 - var9; var6 <= var3 + var9; ++var6) {
						int var11 = var6 - var3;
						if (Math.abs(var12) != var9 || Math.abs(var11) != var9 || this.random.nextInt(2) != 0 && var8 != 0) {
							this.setTile(var10, var13, var6, VanillaBlock.LEAVES.getId());
						}
					}
				}
			}

			for (var13 = 0; var13 < var4; ++var13) {
				this.setTile(var1, var2 + var13, var3, VanillaBlock.LOG.getId());
			}

			return true;
		} else {
			return false;
		}
	}

	public Entity getPlayer() {
		return this.player;
	}

	public void addEntity(Entity var1) {
		this.blockMap.insert(var1);
		var1.setLevel(this);
	}

	public void removeEntity(Entity var1) {
		this.blockMap.remove(var1);
	}

	public void explode(Entity var1, float var2, float var3, float var4, float var5) {
		int var6 = (int) (var2 - var5 - 1.0F);
		int var7 = (int) (var2 + var5 + 1.0F);
		int var8 = (int) (var3 - var5 - 1.0F);
		int var9 = (int) (var3 + var5 + 1.0F);
		int var10 = (int) (var4 - var5 - 1.0F);
		int var11 = (int) (var4 + var5 + 1.0F);

		int var13;
		float var15;
		float var16;
		for (int var12 = var6; var12 < var7; ++var12) {
			for (var13 = var9 - 1; var13 >= var8; --var13) {
				for (int var14 = var10; var14 < var11; ++var14) {
					var15 = var12 + 0.5F - var2;
					var16 = var13 + 0.5F - var3;
					float var17 = var14 + 0.5F - var4;
					int var19;
					if (var12 >= 0 && var13 >= 0 && var14 >= 0 && var12 < this.width && var13 < this.depth && var14 < this.height && var15 * var15 + var16 * var16 + var17 * var17 < var5 * var5 && (var19 = this.getTile(var12, var13, var14)) > 0 && BlockUtils.canExplode(Blocks.fromId(var19))) {
						BlockUtils.dropItems(var19, this, var12, var13, var14, 0.3F);
						this.setTile(var12, var13, var14, 0);
						
						if(Blocks.fromId(var19) == VanillaBlock.TNT && !this.creativeMode) {
							PrimedTnt tnt = new PrimedTnt(this, var12 + 0.5F, var13 + 0.5F, var14 + 0.5F);
							tnt.life = rand.nextInt(tnt.life / 4) + tnt.life / 8;
							this.addEntity(tnt);
						}
					}
				}
			}
		}

		List<Entity> var18 = this.blockMap.getEntities(var1, var6, var8, var10, var7, var9, var11);

		for (var13 = 0; var13 < var18.size(); ++var13) {
			Entity var20;
			if ((var15 = (var20 = var18.get(var13)).distanceTo(var2, var3, var4) / var5) <= 1.0F) {
				var16 = 1.0F - var15;
				var20.hurt(var1, (int) (var16 * 15.0F + 1.0F));
			}
		}

	}

	public Entity findSubclassOf(Class<? extends Entity> clazz) {
		for (Entity entity : this.blockMap.all) {
			if (clazz.isAssignableFrom(entity.getClass())) {
				return entity;
			}
		}

		return null;
	}

	public void removeAllNonCreativeModeEntities() {
		this.blockMap.removeAllNonCreativeModeEntities();
	}
}
