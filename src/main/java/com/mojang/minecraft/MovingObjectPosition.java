package com.mojang.minecraft;

import com.mojang.minecraft.Entity;
import com.mojang.minecraft.model.Vector;

public final class MovingObjectPosition {

	public boolean entityPos;
	public int x;
	public int y;
	public int z;
	public int side;
	public Vector blockPos;
	public Entity entity;

	public MovingObjectPosition(int x, int y, int z, int side, Vector blockPos) {
		this.entityPos = false;
		this.x = x;
		this.y = y;
		this.z = z;
		this.side = side;
		this.blockPos = new Vector(blockPos.x, blockPos.y, blockPos.z);
	}

	public MovingObjectPosition(Entity entity) {
		this.entityPos = true;
		this.entity = entity;
	}
}
