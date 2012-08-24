package ch.spacebase.openclassic.client.level;

import java.util.ArrayList;
import java.util.List;

import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.block.Block;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.data.NBTData;
import ch.spacebase.openclassic.api.entity.BlockEntity;
import ch.spacebase.openclassic.api.event.EventFactory;
import ch.spacebase.openclassic.api.event.entity.EntityDeathEvent;
import ch.spacebase.openclassic.api.level.Level;
import ch.spacebase.openclassic.api.network.msg.Message;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.client.util.GeneralUtils;

public class ClientLevel implements Level {

	private com.mojang.minecraft.level.Level handle;
	private List<BlockEntity> entities = new ArrayList<BlockEntity>();
	private boolean generating = false;
	
	private boolean physics = true;
	public NBTData data;
	
	public ClientLevel(com.mojang.minecraft.level.Level handle) {
		this.handle = handle;
	}
	
	public void tick() {
		for(BlockEntity entity : this.entities) {
			if(entity.getController() != null) entity.getController().tick();
		}
	}
	
	@Override
	public void addPlayer(Player player) {
	}

	@Override
	public void removePlayer(String player) {
	}

	@Override
	public boolean getPhysicsEnabled() {
		return this.physics;
	}

	@Override
	public void setPhysicsEnabled(boolean enabled) {
		this.physics = enabled;
	}

	@Override
	public List<Player> getPlayers() {
		List<Player> result = new ArrayList<Player>();
		result.add(GeneralUtils.getMinecraft().player.openclassic);
		return result;
	}

	@Override
	public String getName() {
		return this.handle.name;
	}

	@Override
	public String getAuthor() {
		return this.handle.creator;
	}

	@Override
	public long getCreationTime() {
		return this.handle.createTime;
	}

	@Override
	public Position getSpawn() {
		return new Position(this, this.handle.xSpawn, this.handle.ySpawn, this.handle.zSpawn, (byte) this.handle.rotSpawn, (byte) 0);
	}

	@Override
	public void setSpawn(Position pos) {
		this.handle.setSpawnPos(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), pos.getYaw());
	}

	@Override
	public short getWidth() {
		return (short) this.handle.width;
	}

	@Override
	public short getHeight() {
		return (short) this.handle.depth;
	}

	@Override
	public short getDepth() {
		return (short) this.handle.height;
	}

	@Override
	public short getWaterLevel() {
		return (short) this.handle.getWaterLevel();
	}

	@Override
	public byte[] getBlocks() {
		return this.handle.blocks;
	}

	@Override
	public byte getBlockIdAt(Position pos) {
		return this.getBlockIdAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
	}

	@Override
	public byte getBlockIdAt(int x, int y, int z) {
		return (byte) this.handle.getTile(x, y, z);
	}

	@Override
	public BlockType getBlockTypeAt(Position pos) {
		return this.getBlockTypeAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
	}

	@Override
	public BlockType getBlockTypeAt(int x, int y, int z) {
		return Blocks.fromId(this.getBlockIdAt(x, y, z));
	}

	@Override
	public Block getBlockAt(Position pos) {
		return new Block(pos);
	}

	@Override
	public Block getBlockAt(int x, int y, int z) {
		return this.getBlockAt(new Position(this, x, y, z));
	}

	@Override
	public boolean setBlockIdAt(Position pos, byte type) {
		return this.setBlockIdAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), type, true);
	}

	@Override
	public boolean setBlockIdAt(Position pos, byte type, boolean physics) {
		return this.setBlockIdAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), type, physics);
	}

	@Override
	public boolean setBlockIdAt(int x, int y, int z, byte type) {
		return this.setBlockIdAt(x, y, z, type, true);
	}

	@Override
	public boolean setBlockIdAt(int x, int y, int z, byte type, boolean physics) {
		if(physics) {
			return this.handle.setTile(x, y, z, type);
		} else {
			return this.handle.setTileNoUpdate(x, y, z, type);
		}
	}

	@Override
	public boolean setBlockAt(Position pos, BlockType type) {
		return this.setBlockAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), type, true);
	}

	@Override
	public boolean setBlockAt(Position pos, BlockType type, boolean physics) {
		return this.setBlockAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), type, physics);
	}

	@Override
	public boolean setBlockAt(int x, int y, int z, BlockType type) {
		return this.setBlockAt(x, y, z, type, true);
	}

	@Override
	public boolean setBlockAt(int x, int y, int z, BlockType type, boolean physics) {
		return this.setBlockIdAt(x, y, z, type.getId(), physics);
	}
	
	@Override
	public int getHighestBlockY(int x, int z) {
		for(int y = this.getHeight(); y >= 0; y--) {
			if(this.getBlockIdAt(x, y, z) != 0) return y;
		}
		
		return -1;
	}

	@Override
	public boolean isHighest(int x, int y, int z) {
		if(this.getHighestBlockY(x, z) <= y) return true;
		return false;
	}
	
	@Override
	public boolean isLit(int x, int y, int z) {
		return this.handle.isLit(x, y, z);
	}

	@Override
	public boolean isGenerating() {
		return this.generating;
	}

	@Override
	public void setGenerating(boolean generating) {
		this.generating = generating;
	}
	
	@Override
	public boolean treePhysics() {
		return this.handle.growTrees;
	}

	@Override
	public void sendToAll(Message message) {
		this.getPlayers().get(0).getSession().send(message);
	}

	@Override
	public void sendToAllExcept(Player skip, Message message) {
		if(skip.getPlayerId() != this.getPlayers().get(0).getPlayerId()) {
			this.getPlayers().get(0).getSession().send(message);
		}
	}

	public List<BlockEntity> getBlockEntities() {
		return new ArrayList<BlockEntity>(this.entities);
	}
	
	public BlockEntity getBlockEntityFromId(int id) {
		for(BlockEntity entity : this.entities) {
			if(entity.getEntityId() == id) return entity;
		}
		
		return null;
	}
	
	public BlockEntity getBlockEntity(Position pos) {
		for(BlockEntity entity : this.entities) {
			if(entity.getPosition().equals(pos)) return entity;
		}
		
		return null;
	}
	
	public BlockEntity spawnBlockEntity(BlockEntity entity, Position pos) {
		this.entities.add(entity);
		entity.setPosition(pos);
		
		return entity;
	}
	
	public void removeBlockEntity(BlockEntity entity) {
		this.removeBlockEntity(entity.getEntityId());
	}
	
	public void removeBlockEntity(int id) {
		for(BlockEntity entity : this.entities) {
			if(entity.getEntityId() == id) {
				if(entity.getController() != null) entity.getController().onDeath();
				EventFactory.callEvent(new EntityDeathEvent(entity));
				this.entities.remove(entity);
			}
		}
	}
	
	public com.mojang.minecraft.level.Level getHandle() {
		return this.handle;
	}

	@Override
	public void delayTick(Position pos, byte id) {
		this.handle.addToTickNextTick(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), id);
	}

	@Override
	public boolean growTree(int x, int y, int z) {
		return this.handle.maybeGrowTree(x, y, z);
	}

	@Override
	public NBTData getData() {
		return this.data;
	}

	@Override
	public int getSkyColor() {
		return this.handle.skyColor;
	}

	@Override
	public void setSkyColor(int color) {
		this.handle.skyColor = color;
	}

	@Override
	public int getFogColor() {
		return this.handle.fogColor;
	}

	@Override
	public void setFogColor(int color) {
		this.handle.fogColor = color;
	}

	@Override
	public int getCloudColor() {
		return this.handle.cloudColor;
	}

	@Override
	public void setCloudColor(int color) {
		this.handle.cloudColor = color;
	}

}
