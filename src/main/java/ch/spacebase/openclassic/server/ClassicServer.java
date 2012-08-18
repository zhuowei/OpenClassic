package ch.spacebase.openclassic.server;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultListModel;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.HeartbeatManager;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.Server;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.block.physics.FallingBlockPhysics;
import ch.spacebase.openclassic.api.block.physics.FlowerPhysics;
import ch.spacebase.openclassic.api.block.physics.GrassPhysics;
import ch.spacebase.openclassic.api.block.physics.HalfStepPhysics;
import ch.spacebase.openclassic.api.block.physics.LiquidPhysics;
import ch.spacebase.openclassic.api.block.physics.MushroomPhysics;
import ch.spacebase.openclassic.api.block.physics.SaplingPhysics;
import ch.spacebase.openclassic.api.block.physics.SpongePhysics;
import ch.spacebase.openclassic.api.command.Console;
import ch.spacebase.openclassic.api.event.EventFactory;
import ch.spacebase.openclassic.api.event.level.LevelCreateEvent;
import ch.spacebase.openclassic.api.event.level.LevelLoadEvent;
import ch.spacebase.openclassic.api.event.level.LevelSaveEvent;
import ch.spacebase.openclassic.api.event.level.LevelUnloadEvent;
import ch.spacebase.openclassic.api.level.Level;
import ch.spacebase.openclassic.api.level.LevelInfo;
import ch.spacebase.openclassic.api.level.generator.FlatLandGenerator;
import ch.spacebase.openclassic.api.level.generator.Generator;
import ch.spacebase.openclassic.api.network.msg.Message;
import ch.spacebase.openclassic.api.network.msg.PlayerDisconnectMessage;
import ch.spacebase.openclassic.api.permissions.PermissionManager;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.plugin.Plugin;
import ch.spacebase.openclassic.api.plugin.PluginManager.LoadOrder;
import ch.spacebase.openclassic.api.sound.AudioManager;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.server.command.ServerCommands;
import ch.spacebase.openclassic.game.ClassicGame;
import ch.spacebase.openclassic.game.io.OpenClassicLevelFormat;
import ch.spacebase.openclassic.game.scheduler.ClassicScheduler;
import ch.spacebase.openclassic.server.level.ServerLevel;
import ch.spacebase.openclassic.server.network.ClassicPipelineFactory;
import ch.spacebase.openclassic.server.network.SessionRegistry;
import ch.spacebase.openclassic.server.sound.ServerAudioManager;
import ch.spacebase.openclassic.server.ui.ConsoleManager;
import ch.spacebase.openclassic.server.ui.EmbeddedConsoleManager;
import ch.spacebase.openclassic.server.ui.GuiConsoleManager;
import ch.spacebase.openclassic.server.ui.TextConsoleManager;

// TODO: Server-side GUIs
public class ClassicServer extends ClassicGame implements Server {
	
	/**
	 * The server's executor service.
	 */
    private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
    	private int nextId = 0;
    	
    	@Override
		public Thread newThread(Runnable r) {
    		this.nextId++;
			return new Thread(r, "Server-" + this.nextId);
		}
    });
	
	/**
	 * Console manager
	 */
	private ConsoleManager console;
	
	/**
	 * The {@link ServerBootstrap} used to initialize Netty.
	 */
	private final ServerBootstrap bootstrap = new ServerBootstrap();

	/**
	 * A group containing all of the channels.
	 */
	private final ChannelGroup group = new DefaultChannelGroup();

	/**
	 * The network executor service - Netty dispatches events to this thread pool.
	 */
	private final ExecutorService executor = Executors.newCachedThreadPool();

	/**
	 * A list of all the active {@link ServerSession}s.
	 */
	private final SessionRegistry sessions = new SessionRegistry();
	
	/**
	 * The server's persistence manager.
	 */
	private final PersistanceManager persistenceManager = new PersistanceManager();
	
	/**
	 * The server's audio manager.
	 */
	private final AudioManager audio = new ServerAudioManager();

	/**
	 * The server's permissions manager.
	 */
	private PermissionManager permManager = new PermissionManager();
	
	/**
	 * Whether the server is running or not.
	 */
	private boolean running = false;

	/**
	 * The list of levels currently loaded on the server.
	 */
	private List<Level> levels = new ArrayList<Level>();

	public ClassicServer() {
		this(new File("."));
	}
	
	public ClassicServer(File directory) {
		super(directory);
	}
	
	public void start(String[] args) {
		if(this.isRunning()) return;
		this.running = true;
		
		OpenClassic.setServer(this);
		OpenClassic.getLogger().info("Starting OpenClassic v" + Constants.SERVER_VERSION + "...");
		
		ChannelFactory factory = new NioServerSocketChannelFactory(executor, executor);
		this.bootstrap.setFactory(factory);
		
		ChannelPipelineFactory pipelineFactory = new ClassicPipelineFactory();
		this.bootstrap.setPipelineFactory(pipelineFactory);
		this.setupConfig();
		
		if(Arrays.asList(args).contains("gui")) {
			this.console = new GuiConsoleManager();
		} else if(Arrays.asList(args).contains("embedded")) {
			this.console = new EmbeddedConsoleManager();
		} else {
			this.console = new TextConsoleManager();
		}
		
		this.console.setup();
		
		this.bind(new InetSocketAddress(this.getPort()));
		
		this.persistenceManager.load();
		this.permManager.load();
		
		this.registerExecutor(null, new ServerCommands());
		
		this.registerGenerator("flat", new FlatLandGenerator());
		
		VanillaBlock.SAND.setPhysics(new FallingBlockPhysics((byte) 12));
		VanillaBlock.GRAVEL.setPhysics(new FallingBlockPhysics((byte) 12));
		VanillaBlock.ROSE.setPhysics(new FlowerPhysics());
		VanillaBlock.DANDELION.setPhysics(new FlowerPhysics());
		VanillaBlock.GRASS.setPhysics(new GrassPhysics());
		VanillaBlock.WATER.setPhysics(new LiquidPhysics((byte) 8));
		VanillaBlock.LAVA.setPhysics(new LiquidPhysics((byte) 10));
		VanillaBlock.RED_MUSHROOM.setPhysics(new MushroomPhysics());
		VanillaBlock.BROWN_MUSHROOM.setPhysics(new MushroomPhysics());
		VanillaBlock.SAPLING.setPhysics(new SaplingPhysics());
		VanillaBlock.SPONGE.setPhysics(new SpongePhysics());
		VanillaBlock.SLAB.setPhysics(new HalfStepPhysics());
		
		this.getPluginManager().loadPlugins(LoadOrder.PREWORLD);
		
		File file = new File(this.getDirectory(), "levels");
		if(!file.exists()) {
			file.mkdirs();
		}
		
		this.loadLevel(this.getConfig().getString("options.default-level", "main"));
		this.getPluginManager().loadPlugins(LoadOrder.POSTWORLD);
		
        this.exec.scheduleAtFixedRate(new Runnable() {	
            public void run() {
                try {
                    tick();
                } catch (Exception e) {
                    OpenClassic.getLogger().log(java.util.logging.Level.SEVERE, "Error while ticking: {0}", e);
                    e.printStackTrace();
                }
            }
        }, 0, 1000 / Constants.TICKS_PER_SECOND, TimeUnit.MILLISECONDS);
		
		this.getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				HeartbeatManager.beat();
			}
		}, 450, 450);
		HeartbeatManager.beat();
		
		this.getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				OpenClassic.getLogger().info("Saving levels...");
				saveLevels();
			}
		}, 3000, 3000);
	}
	
	private void tick() {
		this.getSessionRegistry().tick();
		for(Level level : this.getLevels()) {
			if(level.getPlayers().size() == 0 && !this.getDefaultLevel().getName().equals(level.getName())) {
				this.unloadLevel(level.getName(), false);
			} else {
				((ServerLevel) level).tick();
			}
		}
		
		for(Plugin plugin : this.getPluginManager().getPlugins()) {
			plugin.tick();
		}
		
		((ClassicScheduler) this.getScheduler()).tick();
	}
	
	public void shutdown() {
		if(!this.isRunning()) return;
		this.running = false;
		OpenClassic.getLogger().info("Stopping the server...");
		
		((ClassicScheduler) this.getScheduler()).stop();
		this.getPluginManager().disablePlugins();
		this.getPluginManager().clearPlugins();
		HeartbeatManager.clearBeats();
		HeartbeatManager.setURL("");
		
		this.sendToAll(new PlayerDisconnectMessage("Server shutting down."));
		
		OpenClassic.getLogger().info("Saving levels...");
		this.saveLevels();
		
		OpenClassic.getLogger().info("Saving data...");
		this.getConfig().save();
		this.persistenceManager.save();
		
		OpenClassic.getLogger().info("Closing connections...");
        this.group.close();
        this.bootstrap.getFactory().releaseExternalResources();
		
		OpenClassic.getLogger().info("Stopping console...");
		this.console.stop();
		OpenClassic.setServer(null);
	}
	
	public ChannelGroup getChannelGroup() {
		return this.group;
	}

	public SessionRegistry getSessionRegistry() {
		return this.sessions;
	}
	
	public void bind(SocketAddress address) {
		OpenClassic.getLogger().info("Binding to address: " + address + "...");
		
		try {
			this.group.add(this.bootstrap.bind(address));
		} catch(ChannelException e) {
			OpenClassic.getLogger().severe("Failed to bind to address! (is it already in use?)");
			this.shutdown();
		}
	}
	
	public void broadcastMessage(String message) {
		OpenClassic.getLogger().info(message);
		
		for(Player player : this.getPlayers()) {
			player.sendMessage(message);
		}
	}
	
	public List<Player> getPlayers() {
		List<Player> players = new ArrayList<Player>();
		
		for(Level level : this.levels) {
			players.addAll(level.getPlayers());
		}
		
		return players;
	}
	
	public Player getPlayer(String name) {
		for(Player player : this.getPlayers()) {
			if(player.getName().equalsIgnoreCase(name)) return player;
		}
		
		return null;
	}
	
	public List<Player> matchPlayer(String name) {
		List<Player> result = new ArrayList<Player>();
		
		for(Player player : this.getPlayers()) {
			if(player.getName().toLowerCase().contains(name.toLowerCase()) && !result.contains(player)) result.add(player);
		}
		
		return result;
	}
	
	public long getURLSalt() {
		return HeartbeatManager.getSalt();
	}
	
	public String getMotd() {
		return getConfig().getString("info.motd", "Welcome to my OpenClassic Server!");
	}
	
	public void setMotd(String motd) {
		getConfig().setValue("info.motd", motd);
	}
	
	public String getServerName() {
		return getConfig().getString("info.name", "OpenClassic Server");
	}
	
	public void setServerName(String name) {
		getConfig().setValue("info.name", name);
	}
	
	public int getMaxPlayers() {
		return getConfig().getInteger("options.max-players", 20);
	}
	
	public void setMaxPlayers(int max) {
		getConfig().setValue("options.max-players", max);
	}
	
	public int getPort() {
		return getConfig().getInteger("options.port", 25565);
	}
	
	public void setPort(int port) {
		getConfig().setValue("options.port", port);
	}
	
	public boolean isPublic() {
		return getConfig().getBoolean("options.public", true);
	}
	
	public void setPublic(boolean serverPublic) {
		getConfig().setValue("options.public", serverPublic);
	}
	
	public boolean isOnlineMode() {
		return getConfig().getBoolean("options.online-mode", true);
	}
	
	public void setOnlineMode(boolean online) {
		getConfig().setValue("options.online-mode", online);
	}
	
	public boolean doesUseWhitelist() {
		return getConfig().getBoolean("options.whitelist", false);
	}
	
	public void setUseWhitelist(boolean whitelist) {
		getConfig().setValue("options.whitelist", whitelist);
	}
	
	public boolean isWhitelisted(String player) {
		return this.persistenceManager.isWhitelisted(player);
	}
	
	public boolean isBanned(String player) {
		return this.persistenceManager.isBanned(player);
	}
	
	public boolean isIpBanned(String address) {
		return this.persistenceManager.isIpBanned(address);
	}
	
	public void banPlayer(String player) {
		this.persistenceManager.banPlayer(player);
	}
	
	public void banPlayer(String player, String reason) {
		this.persistenceManager.banPlayer(player, reason);
	}
	
	public void unbanPlayer(String player) {
		this.persistenceManager.unbanPlayer(player);
	}
	
	public void banIp(String address) {
		this.persistenceManager.banIp(address);
	}
	
	public void banIp(String address, String reason) {
		this.persistenceManager.banIp(address, reason);
	}
	
	public void unbanIp(String address) {
		this.persistenceManager.unbanIp(address);
	}
	
	public void whitelist(String player) {
		this.persistenceManager.whitelist(player);
	}
	
	public void unwhitelist(String player) {
		this.persistenceManager.unwhitelist(player);
	}
	
	public String getBanReason(String player) {
		return this.persistenceManager.getBanReason(player);
	}
	
	public String getIpBanReason(String address) {
		return this.persistenceManager.getIpBanReason(address);
	}
	
	public List<String> getBannedPlayers() {
		return this.persistenceManager.getBannedPlayers();
	}
	
	public List<String> getBannedIps() {
		return this.persistenceManager.getBannedIps();
	}
	
	public PermissionManager getPermissionManager() {
		return this.permManager;
	}

	private void setupConfig() {
		this.getConfig().applyDefault("info.name", "OpenClassic Server");
		this.getConfig().applyDefault("info.motd", "Welcome to my OpenClassic Server!");
		this.getConfig().applyDefault("options.port", 25565);
		this.getConfig().applyDefault("options.public", true);
		this.getConfig().applyDefault("options.max-players", 20);
		this.getConfig().applyDefault("options.online-mode", true);
		this.getConfig().applyDefault("options.whitelist", false);
		this.getConfig().applyDefault("options.allow-flight", false);
		this.getConfig().applyDefault("options.default-level", "main");
		this.getConfig().applyDefault("physics.enabled", true);
		this.getConfig().applyDefault("physics.falling", true);
		this.getConfig().applyDefault("physics.flower", true);
		this.getConfig().applyDefault("physics.mushroom", true);
		this.getConfig().applyDefault("physics.trees", true);
		this.getConfig().applyDefault("physics.sponge", true);
		this.getConfig().applyDefault("physics.liquid", true);
		this.getConfig().applyDefault("physics.grass", true);
	}

	public ServerLevel createLevel(LevelInfo info, Generator generator) {
		ServerLevel level = new ServerLevel(info);
		byte[] data = new byte[info.getWidth() * info.getHeight() * info.getDepth()];
		generator.generate(level, data);
		level.setWorldData(info.getWidth(), info.getHeight(), info.getDepth(), data);
		
		if(level.getSpawn() == null) {
			level.setSpawn(generator.findSpawn(level));
		}

		try {
			OpenClassicLevelFormat.save(level);
		} catch (IOException e) {
			OpenClassic.getLogger().severe("Failed to save newly created world!");
			e.printStackTrace();
		}
		
		this.levels.add(level);
		EventFactory.callEvent(new LevelCreateEvent(level));
		OpenClassic.getLogger().info("Level \"" + level.getName() + "\" was successfully created!");

		return level;
	}

	public Level loadLevel(String name) {
		return this.loadLevel(name, true);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Level loadLevel(String name, boolean create) {
		if(this.getLevel(name) != null) return this.getLevel(name);
		
		try {
			Level level = OpenClassicLevelFormat.load(name, create);
			if(level == null) return null;
			this.levels.add(level);
			
			if(((ClassicServer) OpenClassic.getServer()).getConsoleManager() instanceof GuiConsoleManager) {
				DefaultListModel model = ((GuiConsoleManager) ((ClassicServer) OpenClassic.getServer()).getConsoleManager()).getFrame().levels;
				model.add(model.size(), level.getName());
				if(model.capacity() == model.size()) model.setSize(model.getSize() + 1);
			}
			
			EventFactory.callEvent(new LevelLoadEvent(level));
			this.broadcastMessage(Color.BLUE + "Level \"" + name + "\" has been loaded!");
			return level;
		} catch (IOException e) {
			OpenClassic.getLogger().severe("Failed to load level \"" + name + "\"!");
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void unloadLevel(String name) {
		this.unloadLevel(name, true);
	}
	
	@SuppressWarnings("rawtypes")
	public void unloadLevel(String name, boolean announce) {
		if(this.getLevel(name) != null) {
			if(this.getDefaultLevel().getName().equals(name)) {
				if(announce) this.broadcastMessage(Color.RED + "Cannot unload the main level!");
				return;
			}
			
			if(EventFactory.callEvent(new LevelUnloadEvent(this.getLevel(name))).isCancelled()) {
				return;
			}
			
			Level level = this.getLevel(name);
			for(Player player : level.getPlayers()) {
				player.moveTo(this.getDefaultLevel().getSpawn());
			}
			
			this.saveLevel(level);
			((ServerLevel) level).clearPhysics();
			((ServerLevel) level).dispose();
			this.levels.remove(level);
			
			if(((ClassicServer) OpenClassic.getServer()).getConsoleManager() instanceof GuiConsoleManager) {
				DefaultListModel model = ((GuiConsoleManager) ((ClassicServer) OpenClassic.getServer()).getConsoleManager()).getFrame().levels;
				if(model.indexOf(level.getName()) != -1) {
					model.remove(model.indexOf(level.getName()));
				}
			}
			
			if(announce) this.broadcastMessage(Color.BLUE + "Level \"" + name + "\" has been unloaded!");
		}
	}
	
	public Level getDefaultLevel() {
		for(Level level : this.levels) {
			if(level.getName().equalsIgnoreCase(this.getConfig().getString("options.default-level", "main"))) return level;
		}
		
		return (this.levels.size() > 0) ? this.levels.get(0) : null;
	}
	
	public Level getLevel(String name) {
		for(Level level : this.levels) {
			if(level.getName().equalsIgnoreCase(name)) return level;
		}
		
		return null;
	}
	
	public void saveLevels() {
		for(Level level : this.levels) {
			this.saveLevel(level);
		}
	}
	
	public void saveLevel(Level level) {
		level.getData().save(OpenClassic.getGame().getDirectory().getPath() + "/levels/" + level.getName() + ".nbt");
		
		try {
			if(EventFactory.callEvent(new LevelSaveEvent(level)).isCancelled()) {
				return;
			}
			
			OpenClassicLevelFormat.save(level);
		} catch(IOException e) {
			OpenClassic.getLogger().severe("Failed to save level " + level.getName() + "!");
			e.printStackTrace();
		}
	}
	
	public void saveLevel(String name) {
		if(this.getLevel(name) != null) {
			this.saveLevel(this.getLevel(name));
		}
	}

	public List<Level> getLevels() {
		return new ArrayList<Level>(this.levels);
	}
	
	public void sendToAll(Message msg) {
		for(Level level : this.levels) {
			level.sendToAll(msg);
		}
	}
	
	public void sendToAllExcept(Player player, Message msg) {
		for(Level level : this.levels) {
			level.sendToAllExcept(player, msg);
		}
	}
	
	public boolean isRunning() {
		return this.running;
	}
	
	@Override
	public String toString() {
		return "OpenClassic{running=" + this.isRunning() + "}";
	}
	
	public ConsoleManager getConsoleManager() {
		return this.console;
	}

	@Override
	public AudioManager getAudioManager() {
		return this.audio;
	}

	@Override
	public List<String> getWhitelistedPlayers() {
		return this.persistenceManager.getWhitelistedPlayers();
	}

	@Override
	public boolean isFlightAllowed() {
		return this.getConfig().getBoolean("options.allow-flight", false);
	}

	@Override
	public void setAllowFlight(boolean flight) {
		this.getConfig().setValue("options.allow-flight", flight);
	}
	
	@Override
	public Console getConsoleSender() {
		return TextConsoleManager.SENDER;
	}

	@Override
	public Player getPlayer(byte id) {
		for(Player player : this.getPlayers()) {
			if(player.getPlayerId() == id) return player;
		}
		
		return null;
	}

}