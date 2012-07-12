package com.mojang.minecraft.gamemode;

import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.gamemode.GameMode;
import com.mojang.minecraft.gui.BlockSelectScreen;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.player.Player;

public final class CreativeGameMode extends GameMode {

	public CreativeGameMode(Minecraft mc) {
		super(mc);
		this.creative = true;
	}

	public final void openInventory() {
		this.mc.setCurrentScreen(new BlockSelectScreen());
	}

	public final void apply(Level level) {
		super.apply(level);
		
		level.removeAllNonCreativeModeEntities();
		level.creativeMode = true;
		level.growTrees = false;
	}

	public final void apply(Player player) {
		int slot = 0;
		for (BlockType block : Blocks.getBlocks()) {
			if(slot >= 9) break;
			if(block != null && block.isSelectable()) {
				player.inventory.count[slot] = 1;
				player.inventory.slots[slot] = block.getId();
				
				slot++;
			}
		}
	}

	public final boolean isSurvival() {
		return false;
	}
}
