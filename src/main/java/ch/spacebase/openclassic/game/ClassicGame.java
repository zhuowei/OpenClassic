package ch.spacebase.openclassic.game;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ch.spacebase.openclassic.api.Client;
import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.Game;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.Server;
import ch.spacebase.openclassic.api.command.Command;
import ch.spacebase.openclassic.api.command.CommandExecutor;
import ch.spacebase.openclassic.api.command.Sender;
import ch.spacebase.openclassic.api.config.Configuration;
import ch.spacebase.openclassic.api.event.EventFactory;
import ch.spacebase.openclassic.api.event.game.CommandNotFoundEvent;
import ch.spacebase.openclassic.api.event.game.PreCommandEvent;
import ch.spacebase.openclassic.api.level.generator.Generator;
import ch.spacebase.openclassic.api.pkg.PackageManager;
import ch.spacebase.openclassic.api.plugin.Plugin;
import ch.spacebase.openclassic.api.plugin.PluginManager;
import ch.spacebase.openclassic.api.scheduler.Scheduler;
import ch.spacebase.openclassic.game.scheduler.ClassicScheduler;

public abstract class ClassicGame implements Game {

	private final File directory;
	
	private Configuration config;
	private final ClassicScheduler scheduler = new ClassicScheduler();
	
	private final PluginManager pluginManager = new PluginManager();
	private PackageManager pkgManager;
	
	private final Map<Command, Plugin> commands = new HashMap<Command, Plugin>();
	private final Map<CommandExecutor, Plugin> executors = new HashMap<CommandExecutor, Plugin>();
	private final Map<String, Generator> generators = new HashMap<String, Generator>();
	
	public ClassicGame(File directory) {
		this.directory = directory;
		File file = new File(this.getDirectory(), "config.yml");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				OpenClassic.getLogger().severe("Failed to load config file!");
				e.printStackTrace();
			}
		}
		
		if(this instanceof Server) {
			OpenClassic.setServer((Server) this);
		} else {
			OpenClassic.setClient((Client) this);
		}
		
		this.pkgManager = new PackageManager();
		this.config = new Configuration(file);
		this.config.load();
	}
	
	@Override
	public PackageManager getPackageManager() {
		return this.pkgManager;
	}

	@Override
	public Scheduler getScheduler() {
		return this.scheduler;
	}

	@Override
	public PluginManager getPluginManager() {
		return this.pluginManager;
	}

	public void registerCommand(Plugin plugin, Command command) {
		this.commands.put(command, plugin);
	}
	
	public void registerExecutor(Plugin plugin, CommandExecutor executor) {
		this.executors.put(executor, plugin);
	}
	
	@Override
	public void unregisterCommands(Plugin plugin) {
		for(Command command : new ArrayList<Command>(this.commands.keySet())) {
			if(this.commands.get(command) != null && this.commands.get(command).getDescription().getName().equals(plugin.getDescription().getName())) {
				this.commands.remove(command);
			}
		}
	}

	@Override
	public void unregisterExecutors(Plugin plugin) {
		for(CommandExecutor executor : new ArrayList<CommandExecutor>(this.executors.keySet())) {
			if(this.executors.get(executor) != null && this.executors.get(executor).getDescription().getName().equals(plugin.getDescription().getName())) {
				this.executors.remove(executor);
			}
		}
	}

	@Override
	public void processCommand(Sender sender, String command) {
		if(command.length() == 0) return;
		PreCommandEvent event = EventFactory.callEvent(new PreCommandEvent(sender, command));
		if(event.isCancelled()) {
			return;
		}
		
		String split[] = event.getCommand().split(" ");
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
		
		CommandNotFoundEvent e = EventFactory.callEvent(new CommandNotFoundEvent(sender, command));
		if(e.showMessage()) {
			sender.sendMessage(Color.RED + "Unknown command.");
		}
	}

	public Collection<Command> getCommands() {
		return this.commands.keySet();
	}

	@Override
	public Collection<CommandExecutor> getCommandExecutors() {
		return this.executors.keySet();
	}

	@Override
	public Configuration getConfig() {
		return this.config;
	}

	public void registerGenerator(String name, Generator generator) {
		if(generator == null) return;
		this.generators.put(name, generator);
	}
	
	public Generator getGenerator(String name) {
		return this.generators.get(name);
	}
	
	public Map<String, Generator> getGenerators() {
		return new HashMap<String, Generator>(this.generators);
	}
	
	public boolean isGenerator(String name) {
		return this.getGenerator(name) != null;
	}

	@Override
	public File getDirectory() {
		return this.directory;
	}

	@Override
	public void reload() {
		this.config.save();
		this.config.load();
		
		for(Plugin plugin : this.pluginManager.getPlugins()) {
			plugin.reload();
		}
	}

}
