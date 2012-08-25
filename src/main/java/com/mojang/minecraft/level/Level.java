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
import com.mojang.minecraft.model.Vector;
import com.mojang.minecraft.particle.ParticleManager;
import com.mojang.minecraft.phys.AABB;
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
	private transient int[] highest;
	public transient Random random = new Random();
	private transient int id;
	private transient ArrayList<TickNextTick> tickNextTicks;
	public BlockMap blockMap;
	private boolean networkMode;
	public transient Minecraft rendererContext;
	public boolean creativeMode;
	public int waterLevel;
	public int skyColor;
	public int fogColor;
	public int cloudColor;
	private int unprocessed;
	private int tickCount;
	public Entity player;
	public transient ParticleManager particleEngine;
	public transient Object font;
	public boolean growTrees;
	
	public transient ClientLevel openclassic;

	public Level() {
		this.id = this.random.nextInt();
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
			this.highest = new int[this.width * this.height];
			Arrays.fill(this.highest, this.depth);
			this.calcLightDepths(0, 0, this.width, this.height);
			this.random = new Random();
			this.id = this.random.nextInt();
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
		if(this.rendererContext != null) {
			this.rendererContext.levelRenderer.refresh();
		}
		
		this.tickNextTicks.clear();
		this.findSpawn();
		this.initTransient();
	}

	public void findSpawn() {
		Random rand = new Random();
		int attempts = 0;

		int x = 0;
		int z = 0;
		int y = 0;
		while (y <= this.getWaterLevel()) {
			attempts++;
			x = rand.nextInt(this.width / 2) + this.width / 4;
			y = this.getHighestTile(x, z) + 1;
			z = rand.nextInt(this.height / 2) + this.height / 4;
			if (attempts == 10000) {
				this.xSpawn = x;
				this.ySpawn = -100;
				this.zSpawn = z;
				return;
			}
		}

		this.xSpawn = x;
		this.ySpawn = y;
		this.zSpawn = z;
	}

	public void calcLightDepths(int x1, int z1, int x2, int z2) {
		for (int x = x1; x < x1 + x2; ++x) {
			for (int z = z1; z < z1 + z2; ++z) {
				int highest = this.highest[x + z * this.width];

				int blocker;
				for (blocker = this.depth - 1; blocker > 0 && !this.isLightBlocker(x, blocker, z); blocker--);

				this.highest[x + z * this.width] = blocker;
				if (highest != blocker) {
					int lower = highest < blocker ? highest : blocker;
					highest = highest > blocker ? highest : blocker;
					if(this.rendererContext != null) {
						this.rendererContext.levelRenderer.queueChunks(x - 1, lower - 1, z - 1, x + 1, highest + 1, z + 1);
					}
				}
			}
		}

	}

	public boolean isLightBlocker(int var1, int var2, int var3) {
		BlockType block = Blocks.fromId(this.getTile(var1, var2, var3));
		return block != null && block.isOpaque();
	}

	public ArrayList<AABB> getCubes(AABB aabb) {
		ArrayList<AABB> var2 = new ArrayList<AABB>();
		int var3 = (int) aabb.x0;
		int var4 = (int) aabb.x1 + 1;
		int var5 = (int) aabb.y0;
		int var6 = (int) aabb.y1 + 1;
		int var7 = (int) aabb.z0;
		int var8 = (int) aabb.z1 + 1;
		if (aabb.x0 < 0.0F) {
			--var3;
		}

		if (aabb.y0 < 0.0F) {
			--var5;
		}

		if (aabb.z0 < 0.0F) {
			--var7;
		}

		for (; var3 < var4; ++var3) {
			for (int var9 = var5; var9 < var6; ++var9) {
				for (int var10 = var7; var10 < var8; ++var10) {
					AABB var11;
					if (var3 >= 0 && var9 >= 0 && var10 >= 0 && var3 < this.width && var9 < this.depth && var10 < this.height) {
						BlockType var12;
						if ((var12 = Blocks.fromId(this.getTile(var3, var9, var10))) != null && (var11 = BlockUtils.getCollisionBox(var12.getId(), var3, var9, var10)) != null && aabb.intersectsInner(var11)) {
							var2.add(var11);
						}
					} else if ((var3 < 0 || var9 < 0 || var10 < 0 || var3 >= this.width || var10 >= this.height) && (var11 = BlockUtils.getCollisionBox(VanillaBlock.BEDROCK.getId(), var3, var9, var10)) != null && aabb.intersectsInner(var11)) {
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

	public boolean setTileNoNeighborChange(int x, int y, int z, int type) {
		return this.networkMode ? false : this.netSetTileNoNeighborChange(x, y, z, type);
	}

	public boolean netSetTileNoNeighborChange(int x, int y, int z, int type) {
		if (x >= 0 && y >= 0 && z >= 0 && x < this.width && y < this.depth && z < this.height) {
			if (type == this.blocks[(y * this.height + z) * this.width + x]) {
				return false;
			} else {
				if (type == 0 && (x == 0 || z == 0 || x == this.width - 1 || z == this.height - 1) && y >= this.getGroundLevel() && y < this.getWaterLevel()) {
					type = VanillaBlock.WATER.getId();
				}

				this.blocks[(y * this.height + z) * this.width + x] = (byte) type;
				this.calcLightDepths(x, z, 1, 1);
				if(this.rendererContext != null) {
					this.rendererContext.levelRenderer.queueChunks(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1);
				}

				return true;
			}
		} else {
			return false;
		}
	}

	public boolean setTile(int x, int y, int z, int type) {
		if (this.networkMode) {
			return false;
		} else if (this.setTileNoNeighborChange(x, y, z, type)) {
			this.updateNeighborsAt(x, y, z, type);
			return true;
		} else {
			return false;
		}
	}

	public boolean netSetTile(int x, int y, int z, int type) {
		if (this.netSetTileNoNeighborChange(x, y, z, type)) {
			this.updateNeighborsAt(x, y, z, type);
			return true;
		} else {
			return false;
		}
	}

	public void updateNeighborsAt(int x, int y, int z, int type) {
		if(this.openclassic.getPhysicsEnabled()) {
			this.a(x - 1, y, z, x, y, z, type);
			this.a(x + 1, y, z, x, y, z, type);
			this.a(x, y - 1, z, x, y, z, type);
			this.a(x, y + 1, z, x, y, z, type);
			this.a(x, y, z - 1, x, y, z, type);
			this.a(x, y, z + 1, x, y, z, type);
		}
	}

	public boolean setTileNoUpdate(int x, int y, int z, int type) {
		if (x >= 0 && y >= 0 && z >= 0 && x < this.width && y < this.depth && z < this.height) {
			if (type == this.blocks[(y * this.height + z) * this.width + x]) {
				return false;
			} else {
				this.blocks[(y * this.height + z) * this.width + x] = (byte) type;
				return true;
			}
		} else {
			return false;
		}
	}

	private void a(int x, int y, int z, int nx, int nz, int ny, int tile) {
		if (x >= 0 && y >= 0 && z >= 0 && x < this.width && y < this.depth && z < this.height) {
			BlockType type = Blocks.fromId(tile);
			if (type != null) {
				if(type.getPhysics() != null) {
					type.getPhysics().onNeighborChange(this.openclassic.getBlockAt(x, y, z), this.openclassic.getBlockAt(nx, ny, nz));
				}
			}

		}
	}

	public boolean isLit(int x, int y, int z) {
		return x >= 0 && y >= 0 && z >= 0 && x < this.width && y < this.depth && z < this.height ? y >= this.highest[x + z * this.width] : true;
	}

	public int getTile(int x, int y, int z) {
		return x >= 0 && y >= 0 && z >= 0 && x < this.width && y < this.depth && z < this.height ? this.blocks[(y * this.height + z) * this.width + x] & 255 : 0;
	}

	public boolean isSolidTile(int x, int y, int z) {
		BlockType type = Blocks.fromId(this.getTile(x, y, z));
		return type != null && type.isSolid();
	}

	public void tickEntities() {
		this.blockMap.tickAll();
	}

	public void tick() {
		this.tickCount++;
		int var1 = 6;
		int var2 = 6;
		
		for (var1 = 1; 1 << var1 < this.width; var1++);
		for (var2 = 1; 1 << var2 < this.height; var2++);

		if (this.tickCount % 5 == 0) {
			int ticks = this.tickNextTicks.size();

			for (int tick = 0; tick < ticks; ++tick) {
				TickNextTick next = this.tickNextTicks.remove(0);
				if (next.ticks > 0) {
					next.ticks--;
					this.tickNextTicks.add(next);
				} else {
					if (this.isInBounds(next.x, next.y, next.z)) {
						byte block = this.blocks[(next.y * this.height + next.z) * this.width + next.x];
						if(block == next.block && block > 0) {
							if(Blocks.fromId(block) != null && Blocks.fromId(block).getPhysics() != null && this.openclassic.getPhysicsEnabled()) {
								Blocks.fromId(block).getPhysics().update(this.openclassic.getBlockAt(next.x, next.y, next.z));
							}
						}
					}
				}
			}
		}

		this.unprocessed += this.width * this.height * this.depth;
		int ticks = this.unprocessed / 200;
		this.unprocessed = 0;

		for (int count = 0; count < ticks; ++count) {
			this.id = this.id * 3 + 1013904223;
			int y = this.id >> 2;
			int x = (y) & (this.width - 1);
			int z = y >> var1 & (this.height - 1);
			y = y >> var1 + var2 & (this.depth - 1);
			BlockType block = Blocks.fromId(this.blocks[(y * this.height + z) * this.width + x]);
			if(block != null && block.getPhysics() != null && this.openclassic.getPhysicsEnabled() && !EventFactory.callEvent(new BlockPhysicsEvent(this.openclassic.getBlockAt(x, y, z))).isCancelled()) {
				block.getPhysics().update(this.openclassic.getBlockAt(x, y, z));
			}
		}
		
		this.openclassic.tick();
	}

	public int countInstanceOf(Class<? extends Entity> clazz) {
		int instances = 0;

		for (int count = 0; count < this.blockMap.all.size(); ++count) {
			Entity entity = this.blockMap.all.get(count);
			if (clazz.isAssignableFrom(entity.getClass())) {
				instances++;
			}
		}

		return instances;
	}

	private boolean isInBounds(int x, int y, int z) {
		return x >= 0 && y >= 0 && z >= 0 && x < this.width && y < this.depth && z < this.height;
	}

	public float getGroundLevel() {
		return this.getWaterLevel() - 2.0F;
	}

	public float getWaterLevel() {
		return this.waterLevel;
	}

	public boolean containsAnyLiquid(AABB aabb) {
		int var2 = (int) aabb.x0;
		int var3 = (int) aabb.x1 + 1;
		int var4 = (int) aabb.y0;
		int var5 = (int) aabb.y1 + 1;
		int var6 = (int) aabb.z0;
		int var7 = (int) aabb.z1 + 1;
		if (aabb.x0 < 0.0F) {
			--var2;
		}

		if (aabb.y0 < 0.0F) {
			--var4;
		}

		if (aabb.z0 < 0.0F) {
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

	public boolean containsLiquid(AABB aabb, BlockType block) {
		block = toMoving(block);
		
		int var3 = (int) aabb.x0;
		int var4 = (int) aabb.x1 + 1;
		int var5 = (int) aabb.y0;
		int var6 = (int) aabb.y1 + 1;
		int var7 = (int) aabb.z0;
		int var8 = (int) aabb.z1 + 1;
		if (aabb.x0 < 0.0F) {
			--var3;
		}

		if (aabb.y0 < 0.0F) {
			--var5;
		}

		if (aabb.z0 < 0.0F) {
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
					if (type != null && toMoving(type) == block) {
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
			if (block > 0 && Blocks.fromId(block) != null) {
				next.ticks = Blocks.fromId(block).getTickDelay();
			}

			this.tickNextTicks.add(next);
		}
	}

	public boolean isFree(AABB aabb) {
		return this.blockMap.getEntities(null, aabb).size() == 0;
	}

	public List<Entity> findEntities(Entity entity, AABB aabb) {
		return this.blockMap.getEntities(entity, aabb);
	}

	public boolean isSolid(float x, float y, float z, float distance) {
		return this.isSolid(x - distance, y - distance, z - distance) || this.isSolid(x - distance, y - distance, z + distance) || this.isSolid(x - distance, y + distance, z - distance) || this.isSolid(x - distance, y + distance, z + distance) || this.isSolid(x + distance, y - distance, z - distance) || this.isSolid(x + distance, y - distance, z + distance) || this.isSolid(x + distance, y + distance, z - distance) || this.isSolid(x + distance, y + distance, z + distance);
	}

	private boolean isSolid(float x, float y, float z) {
		int tile = this.getTile((int) x, (int) y, (int) z);
		return tile > 0 && Blocks.fromId(tile) != null && Blocks.fromId(tile).isSolid();
	}

	public int getHighestTile(int x, int z) {
		int y;
		for (y = this.depth; (this.getTile(x, y - 1, z) == 0 || (Blocks.fromId(this.getTile(x, y - 1, z)) != null && Blocks.fromId(this.getTile(x, y - 1, z)).isLiquid())) && y > 0; --y);

		return y;
	}

	public void setSpawnPos(int x, int y, int z, float rot) {
		Position old = new Position(this.openclassic, this.xSpawn, this.ySpawn, this.zSpawn, (byte) this.rotSpawn, (byte) 0);
		this.xSpawn = x;
		this.ySpawn = y;
		this.zSpawn = z;
		this.rotSpawn = rot;
		
		EventFactory.callEvent(new SpawnChangeEvent(this.openclassic, old));
	}

	public float getBrightness(int x, int y, int z) {
		return this.isLit(x, y, z) ? 1 : 0.6F;
	}

	public byte[] copyBlocks() {
		return Arrays.copyOf(this.blocks, this.blocks.length);
	}

	public boolean isWater(int x, int y, int z) {
		int tile = this.getTile(x, y, z);
		return tile > 0 && (Blocks.fromId(tile) == VanillaBlock.WATER || Blocks.fromId(tile) == VanillaBlock.STATIONARY_WATER);
	}

	public void setNetworkMode(boolean network) {
		this.networkMode = network;
	}

	public MovingObjectPosition clip(Vector vec1, Vector vec2) {
		return this.clip(vec1, vec2, false);
	}
	
	public MovingObjectPosition clip(Vector vec1, Vector vec2, boolean selection) {
		if (!Float.isNaN(vec1.x) && !Float.isNaN(vec1.y) && !Float.isNaN(vec1.z)) {
			if (!Float.isNaN(vec2.x) && !Float.isNaN(vec2.y) && !Float.isNaN(vec2.z)) {
				int var3 = (int) Math.floor(vec2.x);
				int var4 = (int) Math.floor(vec2.y);
				int var5 = (int) Math.floor(vec2.z);
				int var6 = (int) Math.floor(vec1.x);
				int var7 = (int) Math.floor(vec1.y);
				int var8 = (int) Math.floor(vec1.z);
				int var9 = 20;

				while (var9-- >= 0) {
					if (Float.isNaN(vec1.x) || Float.isNaN(vec1.y) || Float.isNaN(vec1.z)) {
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
					float var16 = vec2.x - vec1.x;
					float var17 = vec2.y - vec1.y;
					float var18 = vec2.z - vec1.z;
					if (var10 != 999.0F) {
						var13 = (var10 - vec1.x) / var16;
					}

					if (var11 != 999.0F) {
						var14 = (var11 - vec1.y) / var17;
					}

					if (var12 != 999.0F) {
						var15 = (var12 - vec1.z) / var18;
					}

					byte var24;
					if (var13 < var14 && var13 < var15) {
						if (var3 > var6) {
							var24 = 4;
						} else {
							var24 = 5;
						}

						vec1.x = var10;
						vec1.y += var17 * var13;
						vec1.z += var18 * var13;
					} else if (var14 < var15) {
						if (var4 > var7) {
							var24 = 0;
						} else {
							var24 = 1;
						}

						vec1.x += var16 * var14;
						vec1.y = var11;
						vec1.z += var18 * var14;
					} else {
						if (var5 > var8) {
							var24 = 2;
						} else {
							var24 = 3;
						}

						vec1.x += var16 * var15;
						vec1.y += var17 * var15;
						vec1.z = var12;
					}

					com.mojang.minecraft.model.Vector var20;
					var6 = (int) ((var20 = new com.mojang.minecraft.model.Vector(vec1.x, vec1.y, vec1.z)).x = (float) Math.floor(vec1.x));
					if (var24 == 5) {
						--var6;
						++var20.x;
					}

					var7 = (int) (var20.y = (float) Math.floor(vec1.y));
					if (var24 == 1) {
						--var7;
						++var20.y;
					}

					var8 = (int) (var20.z = (float) Math.floor(vec1.z));
					if (var24 == 3) {
						--var8;
						++var20.z;
					}

					int var22 = this.getTile(var6, var7, var8);
					BlockType var21 = Blocks.fromId(var22);
					if (var22 > 0 && (Blocks.fromId(var22) != null && !Blocks.fromId(var22).isLiquid())) {
						MovingObjectPosition var23 = null;
						if(selection) {
							var23 = BlockUtils.clipSelection(var21.getId(), var6, var7, var8, vec1, vec2);
						} else {
							var23 = BlockUtils.clip(var21.getId(), var6, var7, var8, vec1, vec2);
						}
						
						if (Blocks.fromId(var22).getModel().getCollisionBox(var6, var7, var8) != null) {
							if (var23 != null) {
								return var23;
							}
						} else {
							if(selection) {
								var23 = BlockUtils.clipSelection(var21.getId(), var6, var7, var8, vec1, vec2);
							} else {
								var23 = BlockUtils.clip(var21.getId(), var6, var7, var8, vec1, vec2);
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

	public boolean maybeGrowTree(int x, int y, int z) {
		int var4 = this.random.nextInt(3) + 4;
		boolean var5 = true;

		int var6;
		int var8;
		int var9;
		for (var6 = y; var6 <= y + 1 + var4; ++var6) {
			byte var7 = 1;
			if (var6 == y) {
				var7 = 0;
			}

			if (var6 >= y + 1 + var4 - 2) {
				var7 = 2;
			}

			for (var8 = x - var7; var8 <= x + var7 && var5; ++var8) {
				for (var9 = z - var7; var9 <= z + var7 && var5; ++var9) {
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
		} else if ((this.blocks[((y - 1) * this.height + z) * this.width + x] & 255) == VanillaBlock.GRASS.getId() && y < this.depth - var4 - 1) {
			this.setTile(x, y - 1, z, VanillaBlock.DIRT.getId());

			int var13;
			for (var13 = y - 3 + var4; var13 <= y + var4; ++var13) {
				var8 = var13 - (y + var4);
				var9 = 1 - var8 / 2;

				for (int var10 = x - var9; var10 <= x + var9; ++var10) {
					int var12 = var10 - x;

					for (var6 = z - var9; var6 <= z + var9; ++var6) {
						int var11 = var6 - z;
						if (Math.abs(var12) != var9 || Math.abs(var11) != var9 || this.random.nextInt(2) != 0 && var8 != 0) {
							this.setTile(var10, var13, var6, VanillaBlock.LEAVES.getId());
						}
					}
				}
			}

			for (var13 = 0; var13 < var4; ++var13) {
				this.setTile(x, y + var13, z, VanillaBlock.LOG.getId());
			}

			return true;
		} else {
			return false;
		}
	}

	public Entity getPlayer() {
		return this.player;
	}

	public void addEntity(Entity entity) {
		this.blockMap.insert(entity);
		entity.setLevel(this);
	}

	public void removeEntity(Entity entity) {
		this.blockMap.remove(entity);
	}

	public void explode(Entity entity, float var2, float var3, float var4, float var5) {
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

		List<Entity> var18 = this.blockMap.getEntities(entity, var6, var8, var10, var7, var9, var11);

		for (var13 = 0; var13 < var18.size(); ++var13) {
			Entity var20;
			if ((var15 = (var20 = var18.get(var13)).distanceTo(var2, var3, var4) / var5) <= 1.0F) {
				var16 = 1.0F - var15;
				var20.hurt(entity, (int) (var16 * 15.0F + 1.0F));
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
