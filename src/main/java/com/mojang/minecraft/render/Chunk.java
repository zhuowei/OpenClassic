package com.mojang.minecraft.render;

import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.VanillaBlock;

import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.player.Player;
import com.mojang.minecraft.render.ClippingHelper;
import org.lwjgl.opengl.GL11;

public final class Chunk {

	private Level level;
	private int baseListId = -1;
	public static int chunkUpdates = 0;
	private int x;
	private int y;
	private int z;
	private int width;
	private int height;
	private int depth;
	public boolean chunkDirty = false;
	private boolean[] dirty = new boolean[2];
	public boolean loaded;

	public Chunk(Level level, int x, int y, int z, int size, int baseId) {
		this.level = level;
		this.x = x;
		this.y = y;
		this.z = z;
		this.width = size;
		this.height = size;
		this.depth = size;
		this.baseListId = baseId;
		this.setAllDirty();
	}

	public final void update() {
		chunkUpdates++;
		this.setAllDirty();

		for (int count = 0; count < 2; ++count) {
			boolean continuing = false;
			boolean cleaned = false;
			GL11.glNewList(this.baseListId + count, 4864);
			ShapeRenderer.instance.begin();

			for (int x = this.x; x < this.x + this.width; x++) {
				for (int y = this.y; y < this.y + this.height; y++) {
					for (int z = this.z; z < this.z + this.depth; z++) {
						int type = this.level.getTile(x, y, z);
						if (type > 0) {
							BlockType block = Blocks.fromId(type);
							if(block == null) block = VanillaBlock.STONE;
							
							int updates = block.getId() == VanillaBlock.WATER.getId() || block.getId() == VanillaBlock.STATIONARY_WATER.getId() ? 1 : 0;
							if (updates != count) {
								continuing = true;
							} else {
								cleaned |= block.getModel().render(x, y, z, block.getId() == VanillaBlock.LAVA.getId() || block.getId() == VanillaBlock.STATIONARY_LAVA.getId() ? 100 : level.getBrightness(x, y, z));
							}
						}
					}
				}
			}

			ShapeRenderer.instance.end();
			GL11.glEndList();
			if (cleaned) {
				this.dirty[count] = false;
			}

			if (!continuing) {
				break;
			}
		}

	}

	public final float distanceSquared(Player player) {
		float xDistance = player.x - this.x;
		float yDistance = player.y - this.y;
		float zDistance = player.z - this.z;
		return xDistance * xDistance + yDistance * yDistance + zDistance * zDistance;
	}

	private void setAllDirty() {
		for (int index = 0; index < 2; ++index) {
			this.dirty[index] = true;
		}
	}

	public final void dispose() {
		this.setAllDirty();
		this.level = null;
	}

	public final int appendData(int[] data, int start, int var3) {
		if (!this.chunkDirty) {
			return start;
		} else {
			if (!this.dirty[var3]) {
				data[start++] = this.baseListId + var3;
			}

			return start;
		}
	}

	public final void clip(ClippingHelper check) {
		this.chunkDirty = check.isBoxInFrustrum(this.x, this.y, this.z, this.x + this.width, this.y + this.height, this.z + this.depth);
	}

}
