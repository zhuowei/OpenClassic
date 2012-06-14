package com.mojang.minecraft.level;

public final class TickNextTick {

	public int x;
	public int y;
	public int z;
	public int block;
	public int ticks;

	public TickNextTick(int x, int y, int z, int block) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.block = block;
	}
}
