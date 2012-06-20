package com.mojang.minecraft.gamemode;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.StepSound;
import ch.spacebase.openclassic.api.event.EventFactory;
import ch.spacebase.openclassic.api.event.block.BlockBreakEvent;
import ch.spacebase.openclassic.client.render.ClientRenderHelper;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.player.Player;

public class GameMode {

	protected final Minecraft mc;
	public boolean creative = false;

	public GameMode(Minecraft mc) {
		this.mc = mc;
	}

	public void apply(Level level) {
		level.creativeMode = false;
		level.growTrees = true;
	}

	public void openInventory() {
	}

	public void hitBlock(int x, int y, int z) {
		this.breakBlock(x, y, z);
	}

	public boolean canPlace(int block) {
		return true;
	}

	public void breakBlock(int x, int y, int z) {
		Level var4 = this.mc.level;
		BlockType type = Blocks.fromId(var4.getTile(x, y, z));
		
		if(this.mc.netManager == null && EventFactory.callEvent(new BlockBreakEvent(var4.openclassic.getBlockAt(x, y, z), OpenClassic.getClient().getPlayer(), this.mc.renderer.heldBlock.block)).isCancelled()) {
			return;
		}
		
		boolean var6 = var4.netSetTile(x, y, z, 0);
		if (type != null && var6) {
			if (this.mc.isConnected()) {
				this.mc.netManager.sendBlockChange(x, y, z, 0, this.mc.player.inventory.getSelected());
			} else if(type.getPhysics() != null) {
				type.getPhysics().onBreak(var4.openclassic.getBlockAt(x, y, z));
			}

			if (type.getStepSound() != StepSound.NONE) {
				if(type.getStepSound() == StepSound.SAND) {
					var4.playSound(StepSound.GRAVEL.getSound(), x, y, z, (StepSound.GRAVEL.getVolume() + 1.0F) / 2.0F, StepSound.GRAVEL.getPitch() * 0.8F);
				} else {
					var4.playSound(type.getStepSound().getSound(), x, y, z, (type.getStepSound().getVolume() + 1.0F) / 2.0F, type.getStepSound().getPitch() * 0.8F);
				}
			}
			
			ClientRenderHelper.getHelper().spawnDestructionParticles(type, var4, x, y, z, this.mc.particleManager);
		}
	}

	public void hitBlock(int x, int y, int z, int side) {
	}

	public void resetHits() {
	}

	public void applyBlockCracks(float time) {
	}

	public float d() {
		return 5.0F;
	}

	public boolean useItem(Player player, int type) {
		return false;
	}

	public void preparePlayer(Player player) {
	}

	public void spawnMobs() {
	}

	public void prepareLevel(Level level) {
	}

	public boolean isSurvival() {
		return true;
	}

	public void apply(Player player) {
	}
}
