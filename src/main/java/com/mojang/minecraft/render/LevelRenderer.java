package com.mojang.minecraft.render;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.player.Player;
import com.mojang.minecraft.render.TextureManager;
import com.mojang.minecraft.render.ShapeRenderer;
import com.mojang.minecraft.render.ChunkDistanceComparator;
import com.mojang.minecraft.render.Chunk;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public final class LevelRenderer {

	public Level level;
	public TextureManager textureManager;
	public int listId;
	public IntBuffer buffer = BufferUtils.createIntBuffer(65536);
	public List<Chunk> chunks = new ArrayList<Chunk>();
	private Chunk[] loadQueue;
	public Chunk[] chunkCache;
	private int xChunks;
	private int zChunks;
	private int yChunks;
	private int baseListId;
	public Minecraft mc;
	private int[] chunkDataCache = new int['\uc350'];
	public int ticks = 0;
	private float lastLoadX = -9999.0F;
	private float lastLoadY = -9999.0F;
	private float lastLoadZ = -9999.0F;
	public float cracks;

	public LevelRenderer(Minecraft mc, TextureManager textureManager) {
		this.mc = mc;
		this.textureManager = textureManager;
		this.listId = GL11.glGenLists(2);
		this.baseListId = GL11.glGenLists(524288);
	}

	public final void refresh() {
		if (this.chunkCache != null) {
			for (int index = 0; index < this.chunkCache.length; ++index) {
				this.chunkCache[index].dispose();
			}
		}

		this.xChunks = this.level.width / 16;
		this.zChunks = this.level.depth / 16;
		this.yChunks = this.level.height / 16;
		this.chunkCache = new Chunk[this.xChunks * this.zChunks * this.yChunks];
		this.loadQueue = new Chunk[this.xChunks * this.zChunks * this.yChunks];
		int listCount = 0;

		for (int x = 0; x < this.xChunks; x++) {
			for (int y = 0; y < this.zChunks; y++) {
				for (int z = 0; z < this.yChunks; z++) {
					this.chunkCache[(z * this.zChunks + y) * this.xChunks + x] = new Chunk(this.level, x << 4, y << 4, z << 4, 16, this.baseListId + listCount);
					this.loadQueue[(z * this.zChunks + y) * this.xChunks + x] = this.chunkCache[(z * this.zChunks + y) * this.xChunks + x];
					listCount += 2;
				}
			}
		}

		for (int x = 0; x < this.chunks.size(); x++) {
			this.chunks.get(x).loaded = false;
		}

		this.chunks.clear();
		GL11.glNewList(this.listId, 4864);
		GL11.glColor4f(0.5F, 0.5F, 0.5F, 1.0F);
		float groundLevel = this.level.getGroundLevel();
		int var5 = 128;
		if (128 > this.level.width) {
			var5 = this.level.width;
		}

		if (var5 > this.level.height) {
			var5 = this.level.height;
		}

		int var6 = 2048 / var5;
		ShapeRenderer.instance.reset();

		for (int var7 = -var5 * var6; var7 < this.level.width + var5 * var6; var7 += var5) {
			for (int var8 = -var5 * var6; var8 < this.level.height + var5 * var6; var8 += var5) {
				float var10 = groundLevel;
				if (var7 >= 0 && var8 >= 0 && var7 < this.level.width && var8 < this.level.height) {
					var10 = 0;
				}

				ShapeRenderer.instance.addTexturedPoint(var7, var10, (var8 + var5), 0.0F, var5);
				ShapeRenderer.instance.addTexturedPoint((var7 + var5), var10, (var8 + var5), var5, var5);
				ShapeRenderer.instance.addTexturedPoint((var7 + var5), var10, var8, var5, 0.0F);
				ShapeRenderer.instance.addTexturedPoint(var7, var10, var8, 0.0F, 0.0F);
			}
		}

		ShapeRenderer.instance.draw();
		GL11.glColor3f(0.8F, 0.8F, 0.8F);
		ShapeRenderer.instance.reset();

		for (int var7 = 0; var7 < this.level.width; var7 += var5) {
			ShapeRenderer.instance.addTexturedPoint(var7, 0.0F, 0.0F, 0.0F, 0.0F);
			ShapeRenderer.instance.addTexturedPoint((var7 + var5), 0.0F, 0.0F, var5, 0.0F);
			ShapeRenderer.instance.addTexturedPoint((var7 + var5), groundLevel, 0.0F, var5, groundLevel);
			ShapeRenderer.instance.addTexturedPoint(var7, groundLevel, 0.0F, 0.0F, groundLevel);
			ShapeRenderer.instance.addTexturedPoint(var7, groundLevel, this.level.height, 0.0F, groundLevel);
			ShapeRenderer.instance.addTexturedPoint((var7 + var5), groundLevel, this.level.height, var5, groundLevel);
			ShapeRenderer.instance.addTexturedPoint((var7 + var5), 0.0F, this.level.height, var5, 0.0F);
			ShapeRenderer.instance.addTexturedPoint(var7, 0.0F, this.level.height, 0.0F, 0.0F);
		}

		GL11.glColor3f(0.6F, 0.6F, 0.6F);

		for (int var7 = 0; var7 < this.level.height; var7 += var5) {
			ShapeRenderer.instance.addTexturedPoint(0.0F, groundLevel, var7, 0.0F, 0.0F);
			ShapeRenderer.instance.addTexturedPoint(0.0F, groundLevel, (var7 + var5), var5, 0.0F);
			ShapeRenderer.instance.addTexturedPoint(0.0F, 0.0F, (var7 + var5), var5, groundLevel);
			ShapeRenderer.instance.addTexturedPoint(0.0F, 0.0F, var7, 0.0F, groundLevel);
			ShapeRenderer.instance.addTexturedPoint(this.level.width, 0.0F, var7, 0.0F, groundLevel);
			ShapeRenderer.instance.addTexturedPoint(this.level.width, 0.0F, (var7 + var5), var5, groundLevel);
			ShapeRenderer.instance.addTexturedPoint(this.level.width, groundLevel, (var7 + var5), var5, 0.0F);
			ShapeRenderer.instance.addTexturedPoint(this.level.width, groundLevel, var7, 0.0F, 0.0F);
		}

		ShapeRenderer.instance.draw();
		GL11.glEndList();
		GL11.glNewList(this.listId + 1, 4864);
		GL11.glColor3f(1.0F, 1.0F, 1.0F);
		float waterLevel = this.level.getWaterLevel();
		GL11.glBlendFunc(770, 771);
		int var100 = 128;
		if (var100 > this.level.width) {
			var100 = this.level.width;
		}

		if (var100 > this.level.height) {
			var100 = this.level.height;
		}

		var5 = 2048 / var100;
		ShapeRenderer.instance.reset();

		for (int var1000 = -var100 * var5; var1000 < this.level.width + var100 * var5; var1000 += var100) {
			for (int var7 = -var100 * var5; var7 < this.level.height + var100 * var5; var7 += var100) {
				float var13 = waterLevel - 0.1F;
				if (var1000 < 0 || var7 < 0 || var1000 >= this.level.width || var7 >= this.level.height) {
					ShapeRenderer.instance.addTexturedPoint(var1000, var13, (var7 + var100), 0.0F, var100);
					ShapeRenderer.instance.addTexturedPoint((var1000 + var100), var13, (var7 + var100), var100, var100);
					ShapeRenderer.instance.addTexturedPoint((var1000 + var100), var13, var7, var100, 0.0F);
					ShapeRenderer.instance.addTexturedPoint(var1000, var13, var7, 0.0F, 0.0F);
					ShapeRenderer.instance.addTexturedPoint(var1000, var13, var7, 0.0F, 0.0F);
					ShapeRenderer.instance.addTexturedPoint((var1000 + var100), var13, var7, var100, 0.0F);
					ShapeRenderer.instance.addTexturedPoint((var1000 + var100), var13, (var7 + var100), var100, var100);
					ShapeRenderer.instance.addTexturedPoint(var1000, var13, (var7 + var100), 0.0F, var100);
				}
			}
		}

		ShapeRenderer.instance.draw();
		GL11.glDisable(3042);
		GL11.glEndList();
		this.addChunks(0, 0, 0, this.level.width, this.level.depth, this.level.height);
	}

	public final int sortChunks(Player player, int var2) {
		float xDiff = player.x - this.lastLoadX;
		float yDiff = player.y - this.lastLoadY;
		float zDiff = player.z - this.lastLoadZ;
		float sqDistance = xDiff * xDiff + yDiff * yDiff + zDiff * zDiff;
		if (sqDistance > 64) {
			this.lastLoadX = player.x;
			this.lastLoadY = player.y;
			this.lastLoadZ = player.z;
			
			try {
				Arrays.sort(this.loadQueue, new ChunkDistanceComparator(player));
			} catch(Exception e) {
			}
		}

		int length = 0;

		for (int index = 0; index < this.loadQueue.length; ++index) {
			length = this.loadQueue[index].appendData(this.chunkDataCache, length, var2);
		}

		this.buffer.clear();
		this.buffer.put(this.chunkDataCache, 0, length);
		this.buffer.flip();
		if (this.buffer.remaining() > 0) {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textureManager.bindTexture("/terrain.png"));
			GL11.glCallLists(this.buffer);
		}

		return this.buffer.remaining();
	}

	public final void addChunks(int x1, int z1, int y1, int x2, int z2, int y2) {
		x1 /= 16;
		z1 /= 16;
		y1 /= 16;
		x2 /= 16;
		z2 /= 16;
		y2 /= 16;
		if (x1 < 0) {
			x1 = 0;
		}

		if (z1 < 0) {
			z1 = 0;
		}

		if (y1 < 0) {
			y1 = 0;
		}

		if (x2 > this.xChunks - 1) {
			x2 = this.xChunks - 1;
		}

		if (z2 > this.zChunks - 1) {
			z2 = this.zChunks - 1;
		}

		if (y2 > this.yChunks - 1) {
			y2 = this.yChunks - 1;
		}

		for (int x = x1; x <= x2; x++) {
			for (int z = z1; z <= z2; z++) {
				for (int y = y1; y <= y2; y++) {
					Chunk chunk = this.chunkCache[(y * this.zChunks + z) * this.xChunks + x];
					if (!chunk.loaded) {
						chunk.loaded = true;
						this.chunks.add(this.chunkCache[(y * this.zChunks + z) * this.xChunks + x]);
					}
				}
			}
		}

	}
}
