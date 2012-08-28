package ch.spacebase.openclassic.server.level;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.block.Block;
import ch.spacebase.openclassic.api.block.BlockFace;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.block.physics.FallingBlockPhysics;
import ch.spacebase.openclassic.api.block.physics.FlowerPhysics;
import ch.spacebase.openclassic.api.block.physics.GrassPhysics;
import ch.spacebase.openclassic.api.block.physics.LiquidPhysics;
import ch.spacebase.openclassic.api.block.physics.MushroomPhysics;
import ch.spacebase.openclassic.api.block.physics.SaplingPhysics;
import ch.spacebase.openclassic.api.block.physics.SpongePhysics;
import ch.spacebase.openclassic.api.data.NBTData;
import ch.spacebase.openclassic.api.entity.BlockEntity;
import ch.spacebase.openclassic.api.event.EventFactory;
import ch.spacebase.openclassic.api.event.block.BlockPhysicsEvent;
import ch.spacebase.openclassic.api.event.entity.EntityDeathEvent;
import ch.spacebase.openclassic.api.event.level.SpawnChangeEvent;
import ch.spacebase.openclassic.api.level.Level;
import ch.spacebase.openclassic.api.level.LevelInfo;
import ch.spacebase.openclassic.api.network.msg.BlockChangeMessage;
import ch.spacebase.openclassic.api.network.msg.Message;
import ch.spacebase.openclassic.api.network.msg.PlayerDespawnMessage;
import ch.spacebase.openclassic.api.network.msg.custom.LevelColorMessage;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.api.util.set.TripleIntHashMap;
import ch.spacebase.openclassic.server.player.ServerPlayer;

public class ServerLevel implements Level {

	private static final Random rand = new Random();
	
	private long creationTime;
	private short width;
	private short height;
	private short depth;
	private byte[] blocks;
	private Position spawn;
	private String name;
	private String author;
	private short waterLevel;
	private boolean generating;
	private int skyColor = 10079487;
	private int fogColor = 16777215;
	private int cloudColor = 16777215;
	private List<Player> players = new ArrayList<Player>();
	private List<BlockEntity> entities = new ArrayList<BlockEntity>();

	private boolean physics = OpenClassic.getGame().getConfig().getBoolean("physics.enabled", true);
	private TripleIntHashMap<Integer> physicsQueue = new TripleIntHashMap<Integer>();
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	
	private NBTData data;
	
	public ServerLevel() {
        this.executor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    physics();
                } catch (Exception e) {
                    OpenClassic.getLogger().log(java.util.logging.Level.SEVERE, "Error while ticking physics: {0}", e);
                    e.printStackTrace();
                }
            }
        }, 0, 1000 / Constants.PHYSICS_PER_SECOND, TimeUnit.MILLISECONDS);
	}

	public ServerLevel(LevelInfo info) {
		this.name = info.getName();
		this.author = "";
		this.creationTime = System.currentTimeMillis();

		this.spawn = info.getSpawn();
		if(this.spawn != null) this.spawn.setLevel(this);

		this.width = info.getWidth();
		this.height = info.getHeight();
		this.depth = info.getDepth();
		this.waterLevel = (short) (this.height / 2);
		this.blocks = new byte[this.width * this.depth * this.height];
		
		this.data = new NBTData(this.name);
		this.data.load(OpenClassic.getGame().getDirectory().getPath() + "/levels/" + this.name + ".nbt");
        this.executor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    physics();
                } catch (Exception e) {
                    OpenClassic.getLogger().log(java.util.logging.Level.SEVERE, "Error while ticking: {0}", e);
                    e.printStackTrace();
                }
            }
        }, 0, 1000 / Constants.PHYSICS_PER_SECOND, TimeUnit.MILLISECONDS);
	}
	
	public boolean getPhysicsEnabled() {
		return this.physics;
	}
	
	public void setPhysicsEnabled(boolean enabled) {
		this.physics = enabled;
	}
	
	public void addPlayer(Player player) {
		this.players.add(player);
	}
	
	public void removePlayer(String name) {
		for(Player player : this.getPlayers()) {
			if(player.getName().equalsIgnoreCase(name)) {
				this.players.remove(player);
				this.sendToAllExcept(player, new PlayerDespawnMessage(player.getPlayerId()));
			}
		}
	}
	
	public List<Player> getPlayers() {
		return new ArrayList<Player>(this.players);
	}
	
	public void tick() {
		for(Player player : this.players) {
			((ServerPlayer) player).tick();
		}
		
		for(BlockEntity entity : this.entities) {
			if(entity.getController() != null) entity.getController().tick();
		}
	}
	
	// TODO: Idle physics like grass, flower, etc
	public void physics() {
		if(this.physics) {
			String updates[];
			Integer ticks[];
			synchronized(this.physicsQueue) {
				updates = this.physicsQueue.keySet().toArray(new String[this.physicsQueue.size()]);
				ticks = this.physicsQueue.values().toArray(new Integer[this.physicsQueue.size()]);
				this.physicsQueue.clear();
			}
			
			for (int count = 0; count < updates.length; count++) {
				int x = TripleIntHashMap.key1(updates[count]);
				int y = TripleIntHashMap.key2(updates[count]);
				int z = TripleIntHashMap.key3(updates[count]);
				
				Block block = this.getBlockAt(x, y, z);
				int tick = ticks[count];
				tick--;
				if(tick > 0) {
					synchronized(this.physicsQueue) {
						this.physicsQueue.put(x, y, z, tick);
					}
					
					continue;
				}
				
				if(physicsAllowed(block)) {
					block.getType().getPhysics().update(block);
				}
			}
		}
	}
	
	public void clearPhysics() {
		synchronized(this.physicsQueue) {
			this.physicsQueue.clear();
		}
	}
	
	public void dispose() {
		this.executor.shutdown();
	}
	
	public void updatePhysics(int x, int y, int z) {
		this.updatePhysics(x, y, z, true);
	}
	
	public void updatePhysics(int x, int y, int z, boolean around) {
		if(!this.physics) return;
		
		synchronized(this.physicsQueue) {
			this.physicsQueue.put(x, y, z, this.getBlockTypeAt(x, y, z).getTickDelay());
			
			if(around) {
				//this.physicsQueue.add(x + 1, y, z);
				//this.physicsQueue.add(x - 1, y, z);
				//this.physicsQueue.add(x, y + 1, z);
				//this.physicsQueue.add(x, y - 1, z);
				//this.physicsQueue.add(x, y, z + 1);
				//this.physicsQueue.add(x, y, z - 1);
			}
		}
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		if(this.name != null && !this.name.equals("")) return;
		
		this.name = name;
		this.data = new NBTData(this.name);
		this.data.load(OpenClassic.getGame().getDirectory().getPath() + "/levels/" + this.name + ".nbt");
	}

	public String getAuthor() {
		return this.author;
	}

	public void setAuthor(String author) {
		if(this.author != null && !this.author.equals("")) return;
		
		this.author = author;
	}

	public long getCreationTime() {
		return this.creationTime;
	}

	public void setCreationTime(long time) {
		if(this.creationTime != 0) return;
		
		this.creationTime = time;
	}

	public Position getSpawn() {
		return this.spawn;
	}

	public void setSpawn(Position spawn) {
		Position old = this.spawn;
		this.spawn = spawn;
		EventFactory.callEvent(new SpawnChangeEvent(this, old));
	}

	public short getWidth() {
		return this.width;
	}

	public short getHeight() {
		return this.height;
	}

	public short getDepth() {
		return this.depth;
	}

	public short getWaterLevel() {
		return this.waterLevel;
	}

	public byte[] getBlocks() {
		return Arrays.copyOf(this.blocks, this.blocks.length);
	}

	public void setWorldData(short width, short height, short depth, byte[] blocks) {
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.waterLevel = (short) (this.height / 2);

		this.blocks = blocks;
	}

	public byte getBlockIdAt(Position pos) {
		return this.getBlockIdAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
	}
	
	public byte getBlockIdAt(int x, int y, int z) {
		if (x < 0 || y < 0 || z < 0 || x >= this.width || y >= this.height || z >= this.depth)
			return 0;
		
		return blocks[coordsToBlockIndex(x, y, z)];
	}
	
	public BlockType getBlockTypeAt(Position pos) {
		return this.getBlockTypeAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
	}
	
	public BlockType getBlockTypeAt(int x, int y, int z) {
		if (x < 0 || y < 0 || z < 0 || x >= this.width || y >= this.height || z >= this.depth)
			return VanillaBlock.AIR;
		
		return Blocks.fromId(this.getBlockIdAt(x, y, z));
	}
	
	public Block getBlockAt(Position pos) {
		if (pos.getBlockX() < 0 || pos.getBlockY() < 0 || pos.getBlockZ() < 0 || pos.getBlockX() >= this.width || pos.getBlockY() >= this.height || pos.getBlockZ() >= this.depth)
			return null;
		
		return new Block(pos);
	}
	
	public Block getBlockAt(int x, int y, int z) {
		return this.getBlockAt(new Position(this, x, y, z));
	}
	
	public boolean setBlockIdAt(Position pos, byte type) {
		return this.setBlockIdAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), type);
	}
	
	public boolean setBlockIdAt(Position pos, byte type, boolean physics) {
		return this.setBlockIdAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), type, physics);
	}
	
	public boolean setBlockIdAt(int x, int y, int z, byte type) {
		return this.setBlockIdAt(x, y, z, type, true);
	}
	
	public boolean setBlockIdAt(int x, int y, int z, byte type, boolean physics) {
		if (x < 0 || y < 0 || z < 0 || x >= this.width || y >= this.height || z >= this.depth)
			return false;
		
		if(!this.generating && type == VanillaBlock.AIR.getId() && (x == 0 || x == this.getWidth() - 1 || z == 0 || z == this.getDepth() - 1) && y <= this.getWaterLevel() - 1 && y > this.getWaterLevel() - 3) {
			type = VanillaBlock.WATER.getId();
		}
		
		blocks[coordsToBlockIndex(x, y, z)] = type;
		this.sendToAll(new BlockChangeMessage((short) x, (short) y, (short) z, type));
		
		if(physics && !this.generating) {
			for(BlockFace face : BlockFace.values()) {
				Block block = this.getBlockAt(x + face.getModX(), y + face.getModY(), z + face.getModZ());
				if(block != null && block.getType() != null && block.getType().getPhysics() != null) {
					block.getType().getPhysics().onNeighborChange(block, this.getBlockAt(x, y, z));
				}
			}
		}
		
		return true;
	}
	
	public boolean setBlockAt(Position pos, BlockType type) {
		return this.setBlockAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), type);
	}
	
	public boolean setBlockAt(Position pos, BlockType type, boolean physics) {
		return this.setBlockAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), type, physics);
	}
	
	public boolean setBlockAt(int x, int y, int z, BlockType type) {
		return this.setBlockAt(x, y, z, type, true);
	}
	
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
		boolean lit = false;
		
		for(int curr = y; curr <= this.getHeight(); curr++) {
			if(!this.canLightPass(this.getBlockAt(x, curr, z))) lit = false;
		}
		
		return lit;
	}
	
	public boolean canLightPass(Block block) {
		return block == null || block.getType() == VanillaBlock.AIR || block.getType() == VanillaBlock.DANDELION || block.getType() == VanillaBlock.ROSE || block.getType() == VanillaBlock.RED_MUSHROOM || block.getType() == VanillaBlock.BROWN_MUSHROOM || block.getType() == VanillaBlock.GLASS || block.getType() == VanillaBlock.LEAVES;
	}
	
	public boolean isGenerating() {
		return this.generating;
	}
	
	public void setGenerating(boolean generating) {
		this.generating = generating;
	}
	
	public boolean treePhysics() {
		return OpenClassic.getGame().getConfig().getBoolean("physics.trees", true);
	}

	public int coordsToBlockIndex(int x, int y, int z) {
		if (x < 0 || y < 0 || z < 0 || x >= this.width || y >= this.height || z >= this.depth)
			return -1;

		return x + (z * this.width) + (y * this.width * this.depth);
	}

	public Position blockIndexToCoords(int index) {
		if (index < 0)
			return null;

		int y = index / this.width / this.depth;
		index -= y * this.width * this.depth;

		int z = index / this.width;
		int x = index - z * this.width;

		return new Position(this, x, y, z);
	}
	
	public static int coordsToBlockIndex(int x, int y, int z, int width, int height, int depth) {
		if (x < 0 || y < 0 || z < 0 || x >= width || y >= height || z >= depth)
			return -1;

		return x + (z * width) + (y * width * depth);
	}

	public static Position blockIndexToCoords(int index, ServerLevel level, int width, int height, int depth) {
		if (index < 0)
			return null;

		int y = index / width / depth;
		index -= y * width * depth;

		int z = index / width;
		int x = index - z * width;

		return new Position(level, x, y, z);
	}
	
	public void sendToAll(Message message) {
		for(Player player : this.getPlayers()) {
			player.getSession().send(message);
		}
	}
	
	public void sendToAllExcept(Player skip, Message message) {
		for(Player player : this.getPlayers()) {
			if(player.getPlayerId() == skip.getPlayerId()) continue;
			
			player.getSession().send(message);
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
	
	private static boolean physicsAllowed(Block block) {
		if(block.getType().getPhysics() == null) return false;
		
		BlockPhysicsEvent event = EventFactory.callEvent(new BlockPhysicsEvent(block));
		if(block.getType().getPhysics() instanceof FallingBlockPhysics) {
			return OpenClassic.getGame().getConfig().getBoolean("physics.falling", true) && !event.isCancelled();
		}
		
		if(block.getType().getPhysics() instanceof FlowerPhysics) {
			return OpenClassic.getGame().getConfig().getBoolean("physics.flower", true) && !event.isCancelled();
		}
		
		if(block.getType().getPhysics() instanceof MushroomPhysics) {
			return OpenClassic.getGame().getConfig().getBoolean("physics.mushroom", true) && !event.isCancelled();
		}
		
		if(block.getType().getPhysics() instanceof SaplingPhysics) {
			return OpenClassic.getGame().getConfig().getBoolean("physics.trees", true) && !event.isCancelled();
		}
	
		if(block.getType().getPhysics() instanceof SpongePhysics) {
			return OpenClassic.getGame().getConfig().getBoolean("physics.sponge", true) && !event.isCancelled();
		}
		
		if(block.getType().getPhysics() instanceof LiquidPhysics) {
			return OpenClassic.getGame().getConfig().getBoolean("physics.liquid", true) && !event.isCancelled();
		}
		
		if(block.getType().getPhysics() instanceof GrassPhysics) {
			return OpenClassic.getGame().getConfig().getBoolean("physics.grass", true) && !event.isCancelled();
		}
		
		return true;
	}

	@Override
	public void delayTick(Position pos, byte id) {
		this.updatePhysics(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), false);
	}
	
	public boolean growTree(int x, int y, int z) {
		int logHeight = rand.nextInt(3) + 4;
		boolean freespace = true;

		for (int currY = y; currY <= y + 1 + logHeight; currY++) {
			byte leaf = 1;
			if (currY == y) {
				leaf = 0;
			}

			if (currY >= y + 1 + logHeight - 2) {
				leaf = 2;
			}

			for (int currX = x - leaf; currX <= x + leaf && freespace; ++currX) {
				for (int currZ = z - leaf; currZ <= z + leaf && freespace; ++currZ) {
					if (currX >= 0 && currY >= 0 && currZ >= 0 && currX < this.width && currY < this.depth && currZ < this.height) {
						if (this.getBlockTypeAt(currX, currY, currZ) != VanillaBlock.AIR) {
							freespace = false;
						}
					} else {
						freespace = false;
					}
				}
			}
		}

		if (!freespace) {
			return false;
		} else if (this.getBlockTypeAt(x, y, z) == VanillaBlock.GRASS && y < this.depth - logHeight - 1) {
			this.setBlockAt(x, y - 1, z, VanillaBlock.DIRT);
			for (int count = y - 3 + logHeight; count <= y + logHeight; ++count) {
				int var8 = count - (y + logHeight);
				int leafMax = 1 - var8 / 2;

				for (int currX = x - leafMax; currX <= x + leafMax; ++currX) {
					int diffX = currX - x;

					for (int currZ = z - leafMax; currZ <= z + leafMax; ++currZ) {
						int diffZ = currZ - z;
						if (Math.abs(diffX) != leafMax || Math.abs(diffZ) != leafMax || rand.nextInt(2) != 0 && var8 != 0) {
							this.setBlockAt(currX, count, currZ, VanillaBlock.LEAVES);
						}
					}
				}
			}

			for (int count = 0; count < logHeight; ++count) {
				this.setBlockAt(x, y + count, z, VanillaBlock.LOG);
			}

			return true;
		} else {
			return false;
		}
	}
	
	public NBTData getData() {
		return this.data;
	}

	@Override
	public int getSkyColor() {
		return this.skyColor;
	}

	@Override
	public void setSkyColor(int color) {
		this.skyColor = color;
		this.sendToAll(new LevelColorMessage("sky", color));
	}

	@Override
	public int getFogColor() {
		return this.fogColor;
	}

	@Override
	public void setFogColor(int color) {
		this.fogColor = color;
		this.sendToAll(new LevelColorMessage("fog", color));
	}

	@Override
	public int getCloudColor() {
		return this.cloudColor;
	}

	@Override
	public void setCloudColor(int color) {
		this.cloudColor = color;
		this.sendToAll(new LevelColorMessage("cloud", color));
	}
	
}
