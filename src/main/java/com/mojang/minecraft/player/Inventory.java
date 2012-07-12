package com.mojang.minecraft.player;

import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;

import java.io.Serializable;

public class Inventory implements Serializable {

	public static final long serialVersionUID = 0L;
	public static final int POP_TIME_DURATION = 5;
	
	public int[] slots = new int[9];
	public int[] count = new int[9];
	public int[] popTime = new int[9];
	public int selected = 0;

	public Inventory() {
		for (int slot = 0; slot < 9; ++slot) {
			this.slots[slot] = -1;
			this.count[slot] = 0;
		}
	}

	public int getSelected() {
		return this.slots[this.selected];
	}

	private int getFirst(int block) {
		for (int slot = 0; slot < this.slots.length; slot++) {
			if (this.slots[slot] == block) {
				return slot;
			}
		}

		return -1;
	}
	
	private int getFirstNotFull(int block) {
		for (int slot = 0; slot < this.slots.length; slot++) {
			if (this.slots[slot] == block && this.count[slot] < 99) {
				return slot;
			}
		}

		return -1;
	}

	public void grabTexture(int block, boolean creative) {
		int slot = this.getFirst(block);
		if (slot >= 0) {
			this.selected = slot;
		} else {
			if (creative && Blocks.fromId(block) != null && Blocks.fromId(block).isSelectable()) {
				this.replaceSlot(Blocks.fromId(block));
			}
		}
	}

	public void swapPaint(int direction) {
		if (direction > 0) {
			direction = 1;
		}

		if (direction < 0) {
			direction = -1;
		}

		for (this.selected -= direction; this.selected < 0; this.selected += this.slots.length);

		while (this.selected >= this.slots.length) {
			this.selected -= this.slots.length;
		}
	}

	public void replaceSlot(int block) {
		int count = 0;
		if (block >= 0) {
			for(BlockType b : Blocks.getBlocks()) {
				if(b != null && b.isSelectable()) {
					if(count == block) this.replaceSlot(b);
					count++;
				}
			}
		}
	}

	public void replaceSlot(BlockType block) {
		if (block != null) {
			int var2 = this.getFirst(block.getId());
			if (var2 >= 0) {
				this.slots[var2] = this.slots[this.selected];
			}

			this.slots[this.selected] = block.getId();
		}
	}

	public boolean addResource(int block) {
		int slot = this.getFirstNotFull(block);
		if (slot < 0) {
			slot = this.getFirst(-1);
		}

		if (slot >= 0) {
			this.slots[slot] = block;
			this.count[slot]++;
			this.popTime[slot] = 5;
			return true;
		}
		
		return false;
	}

	public void tick() {
		for (int slot = 0; slot < this.popTime.length; slot++) {
			if (this.popTime[slot] > 0) {
				this.popTime[slot]--;
			}
		}

	}

	public boolean removeResource(int block) {
		int slot = this.getFirst(block);
		if (slot < 0) {
			return false;
		} else {
			this.count[slot]--;
			if (this.count[slot] <= 0) {
				this.slots[slot] = -1;
				this.count[slot] = 0;
			}

			return true;
		}
	}
	
	public boolean removeSelected(int block) {
		int slot = this.selected;
		if (block != this.slots[slot]) {
			return false;
		} else {
			this.count[slot]--;
			if (this.count[slot] <= 0) {
				this.slots[slot] = -1;
				this.count[slot] = 0;
			}

			return true;
		}
	}
}
