package com.mojang.minecraft.gamemode;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.Block;
import ch.spacebase.openclassic.api.block.StepSound;
import ch.spacebase.openclassic.api.entity.BlockEntity.BlockRemoveCause;
import ch.spacebase.openclassic.api.event.EventFactory;
import ch.spacebase.openclassic.api.event.block.BlockBreakEvent;
import ch.spacebase.openclassic.api.event.entity.EntityBlockRemoveEvent;
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
		Block block = this.mc.level.openclassic.getBlockAt(x, y, z);
		
		if (block != null && block.isEntity() && (EventFactory.callEvent(new EntityBlockRemoveEvent(block.getBlockEntity(), BlockRemoveCause.PLAYER, block)).isCancelled() || !block.getBlockEntity().getController().onBlockRemoval(BlockRemoveCause.PLAYER, block))) {
			return;
		}
		
		if(this.mc.netManager == null && EventFactory.callEvent(new BlockBreakEvent(block, OpenClassic.getClient().getPlayer(), this.mc.renderer.heldBlock.block)).isCancelled()) {
			return;
		}
		
		boolean var6 = this.mc.level.netSetTile(x, y, z, 0);
		if (block != null && block.getType() != null && var6) {
			if (this.mc.isConnected()) {
				this.mc.netManager.sendBlockChange(x, y, z, 0, this.mc.player.inventory.getSelected());
			} else if(block.getType().getPhysics() != null) {
				block.getType().getPhysics().onBreak(this.mc.level.openclassic.getBlockAt(x, y, z));
			}

			if (block.getType().getStepSound() != StepSound.NONE) {
				if(block.getType().getStepSound() == StepSound.SAND) {
					this.mc.level.playSound(StepSound.GRAVEL.getSound(), x, y, z, (StepSound.GRAVEL.getVolume() + 1.0F) / 2.0F, StepSound.GRAVEL.getPitch() * 0.8F);
				} else {
					this.mc.level.playSound(block.getType().getStepSound().getSound(), x, y, z, (block.getType().getStepSound().getVolume() + 1.0F) / 2.0F, block.getType().getStepSound().getPitch() * 0.8F);
				}
			}
			
			ClientRenderHelper.getHelper().spawnDestructionParticles(block.getType(), this.mc.level, x, y, z, this.mc.particleManager);
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
