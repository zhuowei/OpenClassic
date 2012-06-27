package ch.spacebase.openclassic.client.command;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.command.CommandExecutor;
import ch.spacebase.openclassic.api.command.Sender;
import ch.spacebase.openclassic.api.command.annotation.Command;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.client.util.GeneralUtils;


public class ClientCommands extends CommandExecutor {

	@Command(aliases = {"help"}, desc = "Shows a list of commands and what they do.", permission = "openclassic.commands.help")
	public void help(Sender sender, String command, String args[]) {
		int page = 1;
		if(args.length > 0) {
			try {
				page = Integer.parseInt(args[0]);
			} catch(NumberFormatException e) {
				sender.sendMessage(Color.RED + "Invalid page number.");
				return;
			}
		}
		
		if(page < 1) {
			sender.sendMessage(Color.RED + "Invalid page number.");
			return;
		}
		
		List<Command> available = new ArrayList<Command>();
		for(Method method : this.getCommands()) {
			Command cmd = method.getAnnotation(Command.class);
			
			boolean match = false;
			
			if(cmd.senders().length > 0) {	
				for(Class<? extends Sender> allowed : cmd.senders()) {
					if(allowed.isAssignableFrom(sender.getClass())) {
						match = true;
					}
				}
			} else {
				match = true;
			}
			
			if(sender.hasPermission(cmd.permission()) && match) {
				available.add(cmd);
			}
		}
		
		int pages = (int) Math.ceil((double) available.size() / 17);
		if(page > pages) {
			sender.sendMessage(Color.RED + "Page not found.");
			return;
		}
		
		sender.sendMessage(Color.BLUE + "Available commands (page " + page + " of " + pages + "): ");
		
		for(int index = (page - 1) * 17; index < ((page - 1) * 17) + 17; index++) {
			if(index >= available.size()) break;
			Command cmd = available.get(index);
			
			String aliases = cmd.aliases()[0];
			if(cmd.aliases().length > 1) {
				aliases = Arrays.toString(cmd.aliases());
			}
			
			sender.sendMessage(Color.AQUA + sender.getCommandPrefix() + aliases + " - " + cmd.desc() + Color.AQUA + " - " + sender.getCommandPrefix() + aliases + " " + cmd.usage());
		}
	}
	
	@Command(aliases = {"pkg"}, desc = "Manages installed packages on the server.", permission = "openclassic.commands.pkg", min = 1, max = 3, usage = "<option> [args]")
	public void pkg(Sender sender, String command, String args[]) {
		if(args[0].equalsIgnoreCase("install")) {
			if(args.length < 2) {
				sender.sendMessage(Color.RED + "Usage: /pkg install <name>");
				return;
			}
			
			OpenClassic.getGame().getPackageManager().install(args[1], sender);
		} else if(args[0].equalsIgnoreCase("remove")) {
			if(args.length < 2) {
				sender.sendMessage(Color.RED + "Usage: /pkg remove <name>");
				return;
			}
			
			OpenClassic.getGame().getPackageManager().remove(args[1], sender);
		} else if(args[0].equalsIgnoreCase("update")) {
			if(args.length < 2) {
				sender.sendMessage(Color.RED + "Usage: /pkg update <name>");
				return;
			}
			
			OpenClassic.getGame().getPackageManager().update(args[1], sender);
		} else if(args[0].equalsIgnoreCase("add-source")) {
			if(args.length < 3) {
				sender.sendMessage(Color.RED + "Usage: /pkg add-source <id> <url>");
				return;
			}
			
			OpenClassic.getGame().getPackageManager().addSource(args[1], args[2], sender);
		} else if(args[0].equalsIgnoreCase("remove-source")) {
			if(args.length < 2) {
				sender.sendMessage(Color.RED + "Usage: /pkg remove-source <id>");
				return;
			}
			
			OpenClassic.getGame().getPackageManager().removeSource(args[1], sender);
		} else if(args[0].equalsIgnoreCase("update-sources")) {
			OpenClassic.getGame().getPackageManager().updateSources(sender);
		} else {
			sender.sendMessage(Color.RED + "Invalid operation. Valid operations are: install, remove, update, add-source, remove-source, update-sources.");
		}
	}
	
	@Command(aliases = {"reload"}, desc = "Reloads OpenClassic.", permission = "openclassic.commands.reload")
	public void reload(Sender sender, String command, String args[]) {
		sender.sendMessage(Color.AQUA + "Reloading OpenClassic...");
		OpenClassic.getGame().reload();
		sender.sendMessage(Color.GREEN + "Reload complete.");
	}
	
	@Command(aliases = {"setspawn"}, desc = "Sets the spawn to your location", permission = "openclassic.commands.setspawn", senders = {Player.class})
	public void setspawn(Sender sender, String command, String args[]) {
		Player player = (Player) sender;
			
		player.getPosition().getLevel().setSpawn(player.getPosition());
		player.sendMessage(Color.GREEN + "Spawn set to " + player.getPosition().getX() + ", " + player.getPosition().getY() + ", " + player.getPosition().getZ() + "!");
	}
	
	@Command(aliases = {"solid", "bedrock"}, desc = "Toggles bedrock placement mode.", permission = "openclassic.commands.solid", senders = {Player.class})
	public void solid(Sender sender, String command, String args[]) {
		Player player = (Player) sender;
		if(player.getPlaceMode() != VanillaBlock.BEDROCK.getId()) {
			player.setPlaceMode(VanillaBlock.BEDROCK.getId());
			GeneralUtils.getMinecraft().player.userType = Constants.OP;
			player.sendMessage(Color.GREEN + "Bedrock mode enabled.");
		} else {
			player.setPlaceMode(0);
			GeneralUtils.getMinecraft().player.userType = Constants.NOT_OP;
			player.sendMessage(Color.RED + "Bedrock mode disabled.");
		}
	}
	
	@Command(aliases = {"water"}, desc = "Toggles water placement mode.", permission = "openclassic.commands.water", senders = {Player.class})
	public void water(Sender sender, String command, String args[]) {
		Player player = (Player) sender;
		if(player.getPlaceMode() != VanillaBlock.WATER.getId()) {
			player.setPlaceMode(VanillaBlock.WATER.getId());
			player.sendMessage(Color.GREEN + "Water mode enabled.");
		} else {
			player.setPlaceMode(0);
			player.sendMessage(Color.RED + "Water mode disabled.");
		}
	}
	
	@Command(aliases = {"stillwater"}, desc = "Toggles still water placement mode.", permission = "openclassic.commands.stillwater", senders = {Player.class})
	public void stillwater(Sender sender, String command, String args[]) {
		Player player = (Player) sender;
		if(player.getPlaceMode() != VanillaBlock.STATIONARY_WATER.getId()) {
			player.setPlaceMode(VanillaBlock.STATIONARY_WATER.getId());
			player.sendMessage(Color.GREEN + "Still Water mode enabled.");
		} else {
			player.setPlaceMode(0);
			player.sendMessage(Color.RED + "Still Water mode disabled.");
		}
	}
	
	@Command(aliases = {"lava"}, desc = "Toggles lava placement mode.", permission = "openclassic.commands.lava", senders = {Player.class})
	public void lava(Sender sender, String command, String args[]) {
		Player player = (Player) sender;
		if(player.getPlaceMode() != VanillaBlock.LAVA.getId()) {
			player.setPlaceMode(VanillaBlock.LAVA.getId());
			player.sendMessage(Color.GREEN + "Lava mode enabled.");
		} else {
			player.setPlaceMode(0);
			player.sendMessage(Color.RED + "Lava mode disabled.");
		}
	}
	
	@Command(aliases = {"stilllava"}, desc = "Toggles still lava placement mode.", permission = "openclassic.commands.stilllava", senders = {Player.class})
	public void stilllava(Sender sender, String command, String args[]) {
		Player player = (Player) sender;
		if(player.getPlaceMode() != VanillaBlock.STATIONARY_LAVA.getId()) {
			player.setPlaceMode(VanillaBlock.STATIONARY_LAVA.getId());
			player.sendMessage(Color.GREEN + "Still Lava mode enabled.");
		} else {
			player.setPlaceMode(0);
			player.sendMessage(Color.RED + "Still Lava mode disabled.");
		}
	}
	
}
