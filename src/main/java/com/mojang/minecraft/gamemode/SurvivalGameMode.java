package com.mojang.minecraft.gamemode;

import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.client.render.ClientRenderHelper;
import ch.spacebase.openclassic.client.util.BlockUtils;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.gamemode.GameMode;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.level.MobSpawner;
import com.mojang.minecraft.mob.Mob;
import com.mojang.minecraft.player.Player;

public final class SurvivalGameMode extends GameMode {

	private int hitX;
	private int hitY;
	private int hitZ;
	private int hits;
	private int blockHardness;
	private int hitDelay;
	private MobSpawner spawner;

	public SurvivalGameMode(Minecraft mc) {
		super(mc);
	}

	public final void preparePlayer(Player player) {
		player.inventory.slots[8] = VanillaBlock.TNT.getId();
		player.inventory.count[8] = 10;
	}

	public final void breakBlock(int x, int y, int z) {
		int block = this.mc.level.getTile(x, y, z);
		BlockUtils.dropItems(block, this.mc.level, x, y, z);
		super.breakBlock(x, y, z);
	}

	public final boolean canPlace(int block) {
		return this.mc.player.inventory.removeSelected(block);
	}

	public final void hitBlock(int x, int y, int z) {
		int block = this.mc.level.getTile(x, y, z);
		if (block > 0 && BlockUtils.getHardness(Blocks.fromId(block)) == 0) {
			this.breakBlock(x, y, z);
		}

	}

	public final void resetHits() {
		this.hits = 0;
		this.hitDelay = 0;
	}

	public final void hitBlock(int x, int y, int z, int side) {
		if (this.hitDelay > 0) {
			this.hitDelay--;
		} else if (x == this.hitX && y == this.hitY && z == this.hitZ) {
			int type = this.mc.level.getTile(x, y, z);
			if (type != 0) {
				this.blockHardness = BlockUtils.getHardness(Blocks.fromId(type));
				ClientRenderHelper.getHelper().spawnBlockParticles(this.mc.level, x, y, z, side, this.mc.particleManager);
				this.hits++;
				if (this.hits == this.blockHardness + 1) {
					this.breakBlock(x, y, z);
					this.hits = 0;
					this.hitDelay = 5;
				}

			}
		} else {
			this.hits = 0;
			this.hitX = x;
			this.hitY = y;
			this.hitZ = z;
		}
	}

	public final void applyBlockCracks(float time) {
		if (this.hits <= 0) {
			this.mc.levelRenderer.cracks = 0;
		} else {
			this.mc.levelRenderer.cracks = (this.hits + time - 1) / this.blockHardness;
		}
	}

	public final float getReachDistance() {
		return 4.0F;
	}

	public final boolean useItem(Player player, int type) {
		BlockType block = Blocks.fromId(type);
		if (block == VanillaBlock.RED_MUSHROOM && this.mc.player.inventory.removeSelected(type)) {
			player.hurt(null, 3);
			return true;
		} else if (block == VanillaBlock.BROWN_MUSHROOM && this.mc.player.inventory.removeSelected(type)) {
			player.heal(5);
			return true;
		}
		
		return false;
	}

	public final void apply(Level level) {
		super.apply(level);
		this.spawner = new MobSpawner(level);
	}
	
	public final void apply(Player player) {
		for(int slot = 0; slot < 9; slot++) {
			player.inventory.slots[slot] = -1;
			player.inventory.count[slot] = 0;
		}
		
		player.inventory.slots[8] = VanillaBlock.TNT.getId();
		player.inventory.count[8] = 10;
	}

	public final void spawnMobs() {
		int area = this.spawner.level.width * this.spawner.level.height * this.spawner.level.depth / 64 / 64 / 64;
		if (this.spawner.level.random.nextInt(100) < area && this.spawner.level.countInstanceOf(Mob.class) < area * 20) {
			this.spawner.spawn(area, this.spawner.level.player, null);
		}

	}

	public final void prepareLevel(Level level) {
		this.spawner = new MobSpawner(level);
		this.mc.progressBar.setText("Spawning..");
		int area = level.width * level.height * level.depth / 800;
		this.spawner.spawn(area, null, this.mc.progressBar);
	}
}
