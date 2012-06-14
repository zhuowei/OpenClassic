package ch.spacebase.openclassic.client.block.physics;

import com.mojang.minecraft.item.PrimedTnt;

import ch.spacebase.openclassic.api.block.Block;
import ch.spacebase.openclassic.api.block.physics.BlockPhysics;
import ch.spacebase.openclassic.client.level.ClientLevel;

public class TNTPhysics implements BlockPhysics {

	@Override
	public void update(Block block) {
	}

	@Override
	public void onPlace(Block block) {
	}

	@Override
	public void onBreak(Block block) {
		if (!((ClientLevel) block.getLevel()).getHandle().creativeMode) {
			((ClientLevel) block.getLevel()).getHandle().addEntity(new PrimedTnt(((ClientLevel) block.getLevel()).getHandle(), block.getPosition().getBlockX() + 0.5F, block.getPosition().getBlockY() + 0.5F, block.getPosition().getBlockZ() + 0.5F));
		}
	}

	@Override
	public void onNeighborChange(Block block, Block neighbor) {
	}

}
