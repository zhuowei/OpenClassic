package ch.spacebase.openclassic.client;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.level.generator.LevelGenerator;

import ch.spacebase.openclassic.api.Client;
import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.ProgressBar;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.block.physics.FallingBlockPhysics;
import ch.spacebase.openclassic.api.block.physics.FlowerPhysics;
import ch.spacebase.openclassic.api.block.physics.GrassPhysics;
import ch.spacebase.openclassic.api.block.physics.HalfStepPhysics;
import ch.spacebase.openclassic.api.block.physics.LiquidPhysics;
import ch.spacebase.openclassic.api.block.physics.MushroomPhysics;
import ch.spacebase.openclassic.api.block.physics.SaplingPhysics;
import ch.spacebase.openclassic.api.block.physics.SpongePhysics;
import ch.spacebase.openclassic.api.command.Command;
import ch.spacebase.openclassic.api.command.CommandExecutor;
import ch.spacebase.openclassic.api.command.Sender;
import ch.spacebase.openclassic.api.config.Configuration;
import ch.spacebase.openclassic.api.data.NBTData;
import ch.spacebase.openclassic.api.event.EventFactory;
import ch.spacebase.openclassic.api.event.game.PreCommandEvent;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.MainScreen;
import ch.spacebase.openclassic.api.input.InputHelper;
import ch.spacebase.openclassic.api.level.Level;
import ch.spacebase.openclassic.api.level.LevelInfo;
import ch.spacebase.openclassic.api.level.generator.FlatLandGenerator;
import ch.spacebase.openclassic.api.level.generator.Generator;
import ch.spacebase.openclassic.api.pkg.PackageManager;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.plugin.Plugin;
import ch.spacebase.openclassic.api.plugin.PluginManager;
import ch.spacebase.openclassic.api.plugin.PluginManager.LoadOrder;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.api.scheduler.Scheduler;
import ch.spacebase.openclassic.api.sound.AudioManager;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.client.block.physics.TNTPhysics;
import ch.spacebase.openclassic.client.command.ClientCommands;
import ch.spacebase.openclassic.client.input.ClientInputHelper;
import ch.spacebase.openclassic.client.render.ClientRenderHelper;
import ch.spacebase.openclassic.client.scheduler.ClientScheduler;

// TODO: Fix images not 256x256. Custom data packet (int opcode, byte[] data)
public class ClassicClient implements Client {

	private boolean debug = false;
	
	private final Minecraft mc;
	private Configuration config;

	private final PluginManager pluginManager = new PluginManager();
	private final Scheduler scheduler = new ClientScheduler();
	private PackageManager packageManager;
	
	private final Map<String, Generator> generators = new HashMap<String, Generator>();
	private final Map<Command, Plugin> commands = new HashMap<Command, Plugin>();
	private final Map<CommandExecutor, Plugin> executors = new HashMap<CommandExecutor, Plugin>();
	
	public ClassicClient(Minecraft mc) {
		if(MinecraftStandalone.debug) debug = true;
		
		RenderHelper.setHelper(new ClientRenderHelper());
		InputHelper.setHelper(new ClientInputHelper());
		this.mc = mc;
	}
	
	public void init() {
		ConsoleHandler console = new ConsoleHandler();
		console.setFormatter(new DateOutputFormatter(new SimpleDateFormat("HH:mm:ss")));

		Logger logger = Logger.getLogger("");
		for (Handler handler : logger.getHandlers()) {
			logger.removeHandler(handler);
		}

		logger.addHandler(console);
		
		try {
			FileHandler handler = new FileHandler(this.getDirectory().getPath() + "/client.log");
			handler.setFormatter(new DateOutputFormatter(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")));
			OpenClassic.getLogger().addHandler(handler);
		} catch(IOException e) {
			OpenClassic.getLogger().severe("Failed to create log file handler!");
			e.printStackTrace();
		}

		OpenClassic.getLogger().info("Starting OpenClassic Client v" + Constants.CLIENT_VERSION + "...");
		
		this.packageManager = new PackageManager();
		
		this.config = new Configuration(new File(this.getDirectory(), "config.yml"));
		this.config.load();
		
		this.registerExecutor(null, new ClientCommands());
		this.registerGenerator("normal", new LevelGenerator());
		this.registerGenerator("flat", new FlatLandGenerator());
		
		VanillaBlock.SAND.setPhysics(new FallingBlockPhysics((byte) 12));
		VanillaBlock.GRAVEL.setPhysics(new FallingBlockPhysics((byte) 13));
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
		VanillaBlock.TNT.setPhysics(new TNTPhysics());
		
		this.pluginManager.loadPlugins(LoadOrder.PREWORLD);
		this.pluginManager.loadPlugins(LoadOrder.POSTWORLD);
	}
	
	@Override
	public PackageManager getPackageManager() {
		return this.packageManager;
	}

	@Override
	public Scheduler getScheduler() {
		return this.scheduler;
	}

	@Override
	public PluginManager getPluginManager() {
		return this.pluginManager;
	}
	
	@Override
	public void registerCommand(Plugin plugin, Command command) {
		this.commands.put(command, plugin);
	}
	
	@Override
	public void registerExecutor(Plugin plugin, CommandExecutor executor) {
		this.executors.put(executor, plugin);
	}
	
	@Override
	public void unregisterCommands(Plugin plugin) {
		for(Command command : this.commands.keySet()) {
			if(this.commands.get(command).getDescription().getName().equals(plugin.getDescription().getName())) {
				this.commands.remove(command);
			}
		}
	}

	@Override
	public void unregisterExecutors(Plugin plugin) {
		for(CommandExecutor executor : this.executors.keySet()) {
			if(this.executors.get(executor).getDescription().getName().equals(plugin.getDescription().getName())) {
				this.executors.remove(executor);
			}
		}
	}

	@Override
	public void processCommand(Sender sender, String command) {
		String split[] = command.split(" ");
		if(split.length == 0) return;
		
		PreCommandEvent event = EventFactory.callEvent(new PreCommandEvent(sender, command));
		if(event.isCancelled()) {
			return;
		}
		
		command = event.getCommand();
		
		for(CommandExecutor executor : this.executors.keySet()) {
			if(executor.getCommand(split[0]) != null) {
				try {
					Method method = executor.getCommand(split[0]);
					ch.spacebase.openclassic.api.command.annotation.Command annotation = method.getAnnotation(ch.spacebase.openclassic.api.command.annotation.Command.class);
					
					if(annotation.senders().length > 0) {
						boolean match = false;
						
						for(Class<? extends Sender> allowed : annotation.senders()) {
							if(allowed.isAssignableFrom(sender.getClass())) {
								match = true;
							}
						}
						
						if(!match) {
							if(annotation.senders().length == 1) {
								sender.sendMessage(Color.RED + "You must be a " + annotation.senders()[0].getSimpleName().toLowerCase() + " to use this command.");
							} else {
								sender.sendMessage(Color.RED + "You must be one of the following: " + Arrays.toString(annotation.senders()).toLowerCase() + " to use this command.");
							}
							
							return;
						}
					}
					
					if(!sender.hasPermission(annotation.permission())) {
						sender.sendMessage(Color.RED + "You don't have permission to use this command!");
						return;
					}
					
					if(split.length - 1 < annotation.min() || split.length - 1 > annotation.max()) {
						sender.sendMessage(Color.RED + "Usage: " + sender.getCommandPrefix() + split[0] + " " + annotation.usage());
						return;
					}
					
					method.invoke(executor, sender, split[0], Arrays.copyOfRange(split, 1, split.length));
				} catch (Exception e) {
					OpenClassic.getLogger().severe("Failed to invoke command \"" + split[0] + "\" on a command executor!");
					e.printStackTrace();
				}
				
				return;
			}
		}
		
		for(Command cmd : this.getCommands()) {
			if(Arrays.asList(cmd.getAliases()).contains(split[0])) {
				if(cmd.getSenders() != null && cmd.getSenders().length > 0) {
					boolean match = false;
					
					for(Class<? extends Sender> allowed : cmd.getSenders()) {
						if(sender.getClass() == allowed) {
							match = true;
						}
					}
					
					if(!match) {
						if(cmd.getSenders().length == 1) {
							sender.sendMessage(Color.RED + "You must be a " + cmd.getSenders()[0].getName().toLowerCase() + " to use this command.");
						} else {
							sender.sendMessage(Color.RED + "You must be one of the following: " + Arrays.toString(cmd.getSenders()).toLowerCase() + " to use this command.");
						}
						return;
					}
				}
				
				if(!sender.hasPermission(cmd.getPermission())) {
					sender.sendMessage(Color.RED + "You don't have permission to use this command!");
					return;
				}
				
				if((split.length - 1) < cmd.getMinArgs() || (split.length - 1) > cmd.getMaxArgs()) {
					sender.sendMessage(Color.RED + "Usage: " + sender.getCommandPrefix() + split[0] + " " + cmd.getUsage());
					return;
				}
				
				cmd.execute(sender, split[0], Arrays.copyOfRange(split, 1, split.length));
				return;
			}
			
			break;
		}
		
		sender.sendMessage(Color.RED + "Unknown command.");
	}

	@Override
	public Collection<Command> getCommands() {
		return this.commands.keySet();
	}

	@Override
	public void shutdown() {
		this.mc.shutdown();
	}

	@Override
	public Level createLevel(LevelInfo info, Generator generator) {
		if(generator instanceof LevelGenerator) {
			((LevelGenerator) generator).setInfo(info.getName(), this.mc.data != null ? this.mc.data.username : "unknown", info.getWidth(), info.getHeight(), info.getDepth());
		}
		
		com.mojang.minecraft.level.Level level = new com.mojang.minecraft.level.Level();
		level.name = info.getName();
		level.creator = this.mc.data != null ? this.mc.data.username : "unknown";
		level.createTime = System.currentTimeMillis();
		level.setData(info.getWidth(), info.getHeight(), info.getDepth(), new byte[info.getWidth() * info.getHeight() * info.getDepth()]);
		generator.generate(level.openclassic);

		if(level.openclassic.getSpawn() == null) {
			level.openclassic.setSpawn(generator.findSpawn(level.openclassic));
		}

		if(info.getSpawn() != null) level.openclassic.setSpawn(info.getSpawn());
		level.openclassic.data = new NBTData(level.name);
		level.openclassic.data.load(OpenClassic.getGame().getDirectory().getPath() + "/levels/" + level.name + ".nbt");

		this.mc.mode.prepareLevel(level);
		this.mc.setLevel(level);
		return level.openclassic;
	}

	@Override
	public boolean isRunning() {
		return this.mc.running;
	}

	@Override
	public Configuration getConfig() {
		return this.config;
	}

	@Override
	public boolean isInDebugMode() {
		return this.debug;
	}

	@Override
	public void registerGenerator(String name, Generator generator) {
		this.generators.put(name, generator);
	}

	@Override
	public Generator getGenerator(String name) {
		return this.generators.get(name);
	}
	
	@Override
	public Map<String, Generator> getGenerators() {
		return new HashMap<String, Generator>(this.generators);
	}

	@Override
	public boolean isGenerator(String name) {
		return this.generators.containsKey(name);
	}

	@Override
	public Player getPlayer() {
		return this.mc.player.openclassic;
	}

	@Override
	public Level getLevel() {
		if(this.mc.level == null) return null;
		return this.mc.level.openclassic;
	}

	@Override
	public Level openLevel(String name) {
		if(this.mc.level != null && this.mc.level.name.equals(name)) return this.mc.level.openclassic;
		return this.mc.levelIo.load(name).openclassic;
	}

	@Override
	public void saveLevel() {
		if(this.mc.level == null) return;
		this.mc.levelIo.save(this.mc.level);
	}
	
	@Override
	public void exitLevel() {
		this.exitLevel(true);
	}
	
	@Override
	public void exitLevel(boolean save) {
		if(this.mc.level == null) return;
		
		if(save) this.saveLevel();
		this.mc.stopGame(true);
	}

	@Override
	public AudioManager getAudioManager() {
		return this.mc.audio;
	}
	
	public Minecraft getMinecraft() {
		return this.mc;
	}

	@Override
	public void setCurrentScreen(GuiScreen screen) {
		this.mc.setCurrentScreen(screen);
	}

	@Override
	public GuiScreen getCurrentScreen() {
		return this.mc.currentScreen;
	}

	@Override
	public boolean isInGame() {
		return this.mc.ingame;
	}

	@Override
	public MainScreen getMainScreen() {
		return this.mc.hud;
	}
	
	@Override
	public File getDirectory() {
		return this.mc.dir;
	}

	@Override
	public boolean isConnectedToOpenClassic() {
		return this.mc.openclassicServer;
	}

	@Override
	public ProgressBar getProgressBar() {
		return this.mc.progressBar;
	}
	
	private class DateOutputFormatter extends Formatter {
		private final SimpleDateFormat date;

		public DateOutputFormatter(SimpleDateFormat date) {
			this.date = date;
		}

		@Override
		public String format(LogRecord record) {
			StringBuilder builder = new StringBuilder();

			builder.append(date.format(record.getMillis()));
			builder.append(" [");
			builder.append(record.getLevel().getLocalizedName().toUpperCase());
			builder.append("] ");
			builder.append(formatMessage(record));
			builder.append('\n');

			if (record.getThrown() != null) {
				StringWriter writer = new StringWriter();
				record.getThrown().printStackTrace(new PrintWriter(writer));
				builder.append(writer.toString());
			}

			return builder.toString();
		}
	}

}
