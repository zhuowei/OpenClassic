package com.mojang.minecraft.render;

import com.mojang.minecraft.player.Player;
import java.util.Comparator;

public final class ChunkDistanceComparator implements Comparator<com.mojang.minecraft.render.Chunk> {

	private Player player;

	public ChunkDistanceComparator(Player player) {
		this.player = player;
	}

	@Override
	public int compare(Chunk chunk, Chunk other) {
		float sqDist = chunk.distanceSquared(this.player);
		float otherSqDist = other.distanceSquared(this.player);

		/* if (sqDist > otherSqDist) {
			return -1;
		} else if (sqDist < otherSqDist) {
			return 1;	
		} else {
			return 0;
		} */
		
		if (sqDist >= otherSqDist) {
			return -1;
		} else {
			return 1;	
		}
	}
}