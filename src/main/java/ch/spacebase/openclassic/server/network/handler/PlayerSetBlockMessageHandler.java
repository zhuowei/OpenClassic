package ch.spacebase.openclassic.server.network.handler;

import ch.spacebase.openclassic.api.block.Block;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.entity.BlockEntity.BlockRemoveCause;
import ch.spacebase.openclassic.api.event.EventFactory;
import ch.spacebase.openclassic.api.event.block.BlockBreakEvent;
import ch.spacebase.openclassic.api.event.block.BlockPlaceEvent;
import ch.spacebase.openclassic.api.event.entity.EntityBlockRemoveEvent;
import ch.spacebase.openclassic.api.network.msg.BlockChangeMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerSetBlockMessage;
import ch.spacebase.openclassic.api.player.Session.State;
import ch.spacebase.openclassic.server.level.ServerLevel;
import ch.spacebase.openclassic.server.player.ServerPlayer;
import ch.spacebase.openclassic.server.player.ServerSession;

public class PlayerSetBlockMessageHandler extends MessageHandler<PlayerSetBlockMessage> {

	@Override
	public void handle(ServerSession session, ServerPlayer player, PlayerSetBlockMessage message) {
		if(session == null || player == null) return;
		if(session.getState() != State.GAME) return;
		
		if(message.getBlock() == VanillaBlock.AIR.getId() || message.getBlock() == VanillaBlock.WATER.getId() || message.getBlock() == VanillaBlock.STATIONARY_WATER.getId() || message.getBlock() == VanillaBlock.LAVA.getId() || message.getBlock() == VanillaBlock.STATIONARY_LAVA.getId() || message.getBlock() == VanillaBlock.BEDROCK.getId()) {
			session.disconnect("Block type hack detected.");
			return;
		}
		
		// TODO: Reach hack checks and check if player is in position
		
		BlockType old = player.getPosition().getLevel().getBlockTypeAt(message.getX(), message.getY(), message.getZ());
		if(!message.isPlacing() && old == VanillaBlock.BEDROCK && !player.hasPermission("openclassic.commands.solid")) {
			session.disconnect("Block break hack detected.");
			return;
		}
			
		Block block = player.getPosition().getLevel().getBlockAt(message.getX(), message.getY(), message.getZ());
		if(block != null && block.isEntity() && !message.isPlacing()) {
			if(block.getBlockEntity().getController() != null) {
				if(EventFactory.callEvent(new EntityBlockRemoveEvent(block.getBlockEntity(), BlockRemoveCause.PLAYER, block)).isCancelled() || !block.getBlockEntity().getController().onBlockRemoval(BlockRemoveCause.PLAYER, block)) {
					session.send(new BlockChangeMessage((short) block.getPosition().getBlockX(), (short) block.getPosition().getBlockY(), (short) block.getPosition().getBlockZ(), block.getTypeId()));
					return;
				}
			}
		}
		
		byte type = (message.isPlacing()) ? message.getBlock() : 0;
		if(message.isPlacing() && player.getPlaceMode() != 0 && type != 0) type = player.getPlaceMode();
		
		if(!message.isPlacing()) {
			if(EventFactory.callEvent(new BlockBreakEvent(block, player, Blocks.fromId(message.getBlock()))).isCancelled()) {
				session.send(new BlockChangeMessage((short) block.getPosition().getBlockX(), (short) block.getPosition().getBlockY(), (short) block.getPosition().getBlockZ(), block.getTypeId()));
				return;
			}
		}
		
		player.getPosition().getLevel().setBlockIdAt(message.getX(), message.getY(), message.getZ(), type);
		if(message.isPlacing()) {
			if(EventFactory.callEvent(new BlockPlaceEvent(block, player, Blocks.fromId(message.getBlock()))).isCancelled()) {
				if(player.getPosition().getLevel().getBlockIdAt(message.getX(), message.getY(), message.getZ()) == type) {
					player.getPosition().getLevel().setBlockAt(message.getX(), message.getY(), message.getZ(), old);
				}
				
				return;
			}
		}
		
		((ServerLevel) player.getPosition().getLevel()).updatePhysics(message.getX(), message.getY(), message.getZ());
		
		if(block != null && block.getType() != null && block.getType().getPhysics() != null) {
			if(message.isPlacing()) {
				block.getType().getPhysics().onPlace(block);
			} else {
				block.getType().getPhysics().onBreak(block);
			}
		}
	}

}
