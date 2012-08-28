package ch.spacebase.openclassic.server.player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.GZIPOutputStream;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.custom.CustomBlock;
import ch.spacebase.openclassic.api.data.NBTData;
import ch.spacebase.openclassic.api.event.EventFactory;
import ch.spacebase.openclassic.api.event.player.PlayerTeleportEvent;
import ch.spacebase.openclassic.api.level.Level;
import ch.spacebase.openclassic.api.network.msg.IdentificationMessage;
import ch.spacebase.openclassic.api.network.msg.LevelDataMessage;
import ch.spacebase.openclassic.api.network.msg.LevelFinalizeMessage;
import ch.spacebase.openclassic.api.network.msg.LevelInitializeMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerChatMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerDespawnMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerSpawnMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerTeleportMessage;
import ch.spacebase.openclassic.api.network.msg.custom.LevelColorMessage;
import ch.spacebase.openclassic.api.permissions.Group;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.player.Session;
import ch.spacebase.openclassic.api.plugin.RemotePluginInfo;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.server.ClassicServer;

public class ServerPlayer implements Player {
	
	private byte playerId;
	private Position pos;
	private String name;
	private String displayName;
	private ServerSession session;
	private byte placeMode = 0;
	//private int airTicks = 0;
	private ClientInfo client = new ClientInfo(this);
	private NBTData data;
	private List<String> hidden = new CopyOnWriteArrayList<String>();
	
	public boolean teleported = false;
	private boolean sendingLevel = false;
	
	public ServerPlayer(String name, Position pos, ServerSession session) {
		this.name = name;
		this.displayName = name;
		this.pos = pos;
		this.session = session;
		this.data = new NBTData(this.name);
		this.data.load(OpenClassic.getServer().getDirectory().getPath() + "/players/" + this.name + ".nbt");
		
		session.setPlayer(this);
		
		this.playerId = (byte) (((ClassicServer) OpenClassic.getGame()).getSessionRegistry().size());
	}
	
	public Session getSession() {
		return this.session;
	}
	
	public byte getPlayerId() {
		return this.playerId;
	}
	
	public Position getPosition() {
		return this.pos;
	}
	
	public void setPosition(Position pos) {
		this.pos = pos;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getDisplayName() {
		return this.displayName;
	}
	
	public void setDisplayName(String name) {
		this.displayName = name;
	}
	
	public byte getPlaceMode() {
		return this.placeMode;
	}
	
	public void setPlaceMode(int type) {
		this.placeMode = (byte) type;
	}
	
	public void moveTo(Position pos) {
		this.moveTo(pos.getLevel(), pos.getX(), pos.getY(), pos.getZ(), pos.getYaw(), pos.getPitch());
	}
	
	public void moveTo(double x, double y, double z) {
		this.moveTo(this.pos.getLevel(), x, y, z);
	}
	
	public void moveTo(double x, double y, double z, byte yaw, byte pitch) {
		this.moveTo(this.pos.getLevel(), x, y, z, yaw, pitch);
	}
	
	public void moveTo(Level level, double x, double y, double z) {
		this.moveTo(this.pos.getLevel(), x, y, z, this.pos.getYaw(), this.pos.getPitch());
	}
	
	public void moveTo(Level level, double x, double y, double z, byte yaw, byte pitch) {
		Position to = new Position(level, x, y, z, yaw, pitch);
		Level old = this.pos.getLevel();
		
		PlayerTeleportEvent event = EventFactory.callEvent(new PlayerTeleportEvent(this, this.getPosition(), to));
		if(event.isCancelled()) return;
		
		this.pos = event.getTo();
		this.teleported = true;
		if(!old.getName().equals(this.pos.getLevel().getName())) {
			this.pos.getLevel().addPlayer(this);
			old.removePlayer(this.getName());
			old.sendToAllExcept(this, new PlayerDespawnMessage(this.getPlayerId()));
			this.session.send(new IdentificationMessage(Constants.PROTOCOL_VERSION, "Sending to " + this.pos.getLevel().getName() + "...", "", this.getGroup().hasPermission("openclassic.commands.solid") ? Constants.OP : Constants.NOT_OP));
			this.sendLevel(this.pos.getLevel());
		} else {
			this.getSession().send(new PlayerTeleportMessage((byte) -1, this.getPosition().getX(), this.getPosition().getY(), this.getPosition().getZ(), this.getPosition().getYaw(), this.getPosition().getPitch()));
			this.getPosition().getLevel().sendToAllExcept(this, new PlayerTeleportMessage(this.getPlayerId(), this.getPosition().getX(), this.getPosition().getY() + 0.59375, this.getPosition().getZ(), this.getPosition().getYaw(), this.getPosition().getPitch()));
		}
	}
	
	public Group getGroup() {
		return OpenClassic.getServer().getPermissionManager().getPlayerGroup(this.getName());
	}
	
	public void setGroup(Group group) {
		OpenClassic.getServer().getPermissionManager().setPlayerGroup(this.getName(), group);
	}
	
	public SocketAddress getAddress() {
		return this.session.getAddress();
	}
	
	public String getIp() {
		return this.session.getAddress().toString().replace("/", "").split(":")[0];
	}
	
	@Override
	public boolean hasPermission(String permission) {
		return this.getGroup() != null && this.getGroup().hasPermission(permission);
	}
	
	@Override
	public String getCommandPrefix() {
		return "/";
	}
	
	public void disconnect(String reason) {
		this.session.disconnect(reason);
	}
	
	public void tick() {
		// Experimental
		/* if(!OpenClassic.getServer().getConfig().getBoolean("options.allow-flight", false)) {
			if(this.pos.getLevel().getBlockTypeAt(this.pos.getBlockX(), this.pos.getBlockY() - 2, this.pos.getBlockZ()) == BlockType.AIR) {
				this.airTicks++;	
			} else if(this.airTicks != 0) {
				this.airTicks = 0;
			}
			
			if(this.airTicks > 300) {
				this.session.disconnect("Flying is not allowed on this server.");
			}
		} */
	}
	
	public void destroy() {
		this.getPosition().getLevel().removePlayer(this.getName());
		this.playerId = 0;
		this.pos = null;
		this.session = null;
	}
	
	@Override
	public void sendMessage(String message) {		
		this.getSession().send(new PlayerChatMessage(this.getPlayerId(), message));
	}
	
	public void sendLevel(final Level level) {
		final Player player = this;
		OpenClassic.getGame().getScheduler().scheduleAsyncTask(OpenClassic.getGame(), new Runnable() {
			@Override
			public void run() {
				while(sendingLevel) {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				sendingLevel = true;
				
				try {
					session.send(new LevelInitializeMessage());
					
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					GZIPOutputStream gzip = new GZIPOutputStream(out);
					DataOutputStream dataOut = new DataOutputStream(gzip);
					
					byte[] b = level.getBlocks();
					if(!hasCustomClient()) {
						for(int index = 0; index < b.length; index++) {
							if(Blocks.fromId(b[index]) instanceof CustomBlock) {
								b[index] = ((CustomBlock) Blocks.fromId(b[index])).getFallback().getId();
							}
						}
					}
					
					dataOut.writeInt(b.length);
					dataOut.write(b);
					
					dataOut.close();
					gzip.close();

					byte[] data = out.toByteArray();
					
					out.close();

					double numChunks = data.length / 1024;
					double sent = 0;
					
					for (int chunkStart = 0; chunkStart < data.length; chunkStart += 1024) {
						byte[] chunkData = new byte[1024];

						short length = 1024;
						if (data.length - chunkStart < length)
							length = (short) (data.length - chunkStart);

						System.arraycopy(data, chunkStart, chunkData, 0, length);

						session.send(new LevelDataMessage(length, chunkData, (byte) ((sent / numChunks) * 255)));
						sent++;
					}
					
					session.send(new LevelFinalizeMessage(level.getWidth(), level.getHeight(), level.getDepth()));
					moveTo(level.getSpawn());
					
					level.sendToAllExcept(player, new PlayerSpawnMessage(player.getPlayerId(), player.getName(), player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ(), player.getPosition().getYaw(), player.getPosition().getPitch()));
					for (Player p : level.getPlayers()) {
						if(p.getPlayerId() == getPlayerId()) continue;
						
						session.send(new PlayerSpawnMessage(p.getPlayerId(), p.getName(), p.getPosition().getX(), p.getPosition().getY(), p.getPosition().getZ(), p.getPosition().getYaw(), p.getPosition().getPitch()));
					}
					
					if(hasCustomClient()) {
						session.send(new LevelColorMessage("sky", level.getSkyColor()));
						session.send(new LevelColorMessage("fog", level.getFogColor()));
						session.send(new LevelColorMessage("cloud", level.getCloudColor()));
					}
				} catch (Exception e) {
					session.disconnect("Failed to send level!");
					OpenClassic.getLogger().severe("Failed to send level " + level.getName() + " to player " + getName() + "!");
					e.printStackTrace();
				}
				
				sendingLevel = false;
			}
		});
	}

	public ClientInfo getClientInfo() {
		return this.client;
	}

	@Override
	public boolean hasCustomClient() {
		return this.client.isCustom();
	}

	@Override
	public String getClientVersion() {
		return this.client.getVersion();
	}
	
	@Override
	public NBTData getData() {
		return this.data;
	}

	@Override
	public List<RemotePluginInfo> getPlugins() {
		return this.client.getPlugins();
	}

	@Override
	public void chat(String message) {
		this.session.messageReceived(new PlayerChatMessage((byte) -1, message));
	}

	@Override
	public void hidePlayer(Player player) {
		this.getSession().send(new PlayerDespawnMessage(player.getPlayerId()));
		this.hidden.add(player.getName());
	}

	@Override
	public void showPlayer(Player player) {
		this.hidden.remove(player.getName());
		this.getSession().send(new PlayerSpawnMessage(player.getPlayerId(), player.getName(), player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ(), player.getPosition().getYaw(), player.getPosition().getPitch()));
	}

	@Override
	public boolean canSee(Player player) {
		return this.hidden.contains(player.getName());
	}

	@Override
	public String getLanguage() {
		return this.client.getLanguage().equals("") ? OpenClassic.getGame().getLanguage() : this.client.getLanguage();
	}
	
}
