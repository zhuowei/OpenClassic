package ch.spacebase.openclassic.server.command;

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
import ch.spacebase.openclassic.api.level.Level;
import ch.spacebase.openclassic.api.level.LevelInfo;
import ch.spacebase.openclassic.api.permissions.Group;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.server.level.ServerLevel;

// TODO: Translate descriptions
public class ServerCommands extends CommandExecutor {

	@Command(aliases = {"help"}, desc = "Shows a list of commands and what they do.", permission = "openclassic.commands.help")
	public void help(Sender sender, String command, String args[]) {
		int page = 1;
		if(args.length > 0) {
			try {
				page = Integer.parseInt(args[0]);
			} catch(NumberFormatException e) {
				sender.sendMessage(Color.RED + OpenClassic.getGame().getTranslator().translate("help.invalid-page"));
				return;
			}
		}
		
		if(page < 1) {
			sender.sendMessage(Color.RED + OpenClassic.getGame().getTranslator().translate("help.invalid-page"));
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
			sender.sendMessage(OpenClassic.getGame().getTranslator().translate("help.page-not-found"));
			return;
		}
		
		sender.sendMessage(Color.BLUE + String.format(OpenClassic.getGame().getTranslator().translate("help.pages"), page, pages) + " ");
		
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
				sender.sendMessage(Color.RED + OpenClassic.getGame().getTranslator().translate("command.usage") + ": /pkg install <name>");
				return;
			}
			
			OpenClassic.getGame().getPackageManager().install(args[1], sender);
		} else if(args[0].equalsIgnoreCase("remove")) {
			if(args.length < 2) {
				sender.sendMessage(Color.RED + OpenClassic.getGame().getTranslator().translate("command.usage") + ": /pkg remove <name>");
				return;
			}
			
			OpenClassic.getGame().getPackageManager().remove(args[1], sender);
		} else if(args[0].equalsIgnoreCase("update")) {
			if(args.length < 2) {
				sender.sendMessage(Color.RED + OpenClassic.getGame().getTranslator().translate("command.usage") + ": /pkg update <name>");
				return;
			}
			
			OpenClassic.getGame().getPackageManager().update(args[1], sender);
		} else if(args[0].equalsIgnoreCase("add-source")) {
			if(args.length < 3) {
				sender.sendMessage(Color.RED + OpenClassic.getGame().getTranslator().translate("command.usage") + ": /pkg add-source <id> <url>");
				return;
			}
			
			OpenClassic.getGame().getPackageManager().addSource(args[1], args[2], sender);
		} else if(args[0].equalsIgnoreCase("remove-source")) {
			if(args.length < 2) {
				sender.sendMessage(Color.RED + OpenClassic.getGame().getTranslator().translate("command.usage") + ": /pkg remove-source <id>");
				return;
			}
			
			OpenClassic.getGame().getPackageManager().removeSource(args[1], sender);
		} else if(args[0].equalsIgnoreCase("update-sources")) {
			OpenClassic.getGame().getPackageManager().updateSources(sender);
		} else {
			sender.sendMessage(Color.RED + String.format(OpenClassic.getGame().getTranslator().translate("pkg.invalid-operation"), "install, remove, update, add-source, remove-source, update-sources"));
		}
	}
	
	@Command(aliases = {"reload"}, desc = "Reloads OpenClassic.", permission = "openclassic.commands.reload")
	public void reload(Sender sender, String command, String args[]) {
		sender.sendMessage(Color.AQUA + OpenClassic.getGame().getTranslator().translate("reload.reloading"));
		OpenClassic.getGame().reload();
		sender.sendMessage(Color.GREEN + OpenClassic.getGame().getTranslator().translate("reload.complete"));
	}
	
	@Command(aliases = {"stop"}, desc = "Stops the server", permission = "openclassic.commands.stop")
	public void stop(Sender sender, String command, String args[]) {
		OpenClassic.getServer().broadcastMessage(Color.RED + OpenClassic.getGame().getTranslator().translate("stop"));
		OpenClassic.getServer().shutdown();
	}
	
	@Command(aliases = {"setspawn"}, desc = "Sets the spawn to your location", permission = "openclassic.commands.setspawn", senders = {Player.class})
	public void setspawn(Sender sender, String command, String args[]) {
		Player player = (Player) sender;
		
		player.getPosition().getLevel().setSpawn(player.getPosition());
		player.sendMessage(Color.GREEN + String.format(OpenClassic.getGame().getTranslator().translate("spawn.set"), player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ()));
	}
	
	@Command(aliases = {"ban"}, desc = "Bans a player.", permission = "openclassic.commands.ban", min = 1, usage = "<player> [reason]")
	public void ban(Sender sender, String command, String args[]) {
		Player player = null;
		
		List<Player> players = OpenClassic.getServer().matchPlayer(args[0]);
		if(players.size() > 0) {
			if(players.size() > 1) {
				sender.sendMessage(Color.RED + OpenClassic.getGame().getTranslator().translate("command.multiple-players"));
				return;
			}
			
			player = players.get(0);
		}
		
		Group group = OpenClassic.getServer().getPermissionManager().getPlayerGroup(player != null ? player.getName() : args[0]);
		if(sender instanceof Player && group != null && !OpenClassic.getServer().getPermissionManager().getPlayerGroup(sender.getName()).isSubGroup(group)) {
			sender.sendMessage(Color.RED + OpenClassic.getGame().getTranslator().translate("command.deny-higher-group"));
			return;
		}
		
		if(args.length < 2) {
			OpenClassic.getServer().banPlayer(player != null ? player.getName() : args[0]);
			if(player != null) player.getSession().disconnect("You have been banned from this server.");
		} else {
			StringBuilder build = new StringBuilder();
			for(int index = 1; index < args.length; index++) {
				build.append(args[index] + " ");
			}
			
			OpenClassic.getServer().banPlayer((player != null ? player.getName() : args[0]), build.toString().trim());
			if(player != null) player.getSession().disconnect(build.toString().trim());
		}
		
		sender.sendMessage(Color.GREEN + String.format(OpenClassic.getGame().getTranslator().translate("ban.banned"), (player != null ? player.getName() : args[0]) + Color.GREEN));
	}
	
	@Command(aliases = {"unban"}, desc = "Unbans a player.", permission = "openclassic.commands.unban", min = 1, usage = "<player>")
	public void unban(Sender sender, String command, String args[]) {
		OpenClassic.getServer().unbanPlayer(args[0]);
		sender.sendMessage(Color.GREEN + String.format(OpenClassic.getGame().getTranslator().translate("ban.unbanned"), args[0] + Color.GREEN));
	}
	
	@Command(aliases = {"banip"}, desc = "Bans a player's IP.", permission = "openclassic.commands.banip", min = 1, usage = "<address> [reason]")
	public void banip(Sender sender, String command, String args[]) {
		if(args.length < 2) {
			OpenClassic.getServer().banIp(args[0]);
			
			for(Player player : OpenClassic.getServer().getPlayers()) {
				if(player.getIp().equals(args[0])) player.getSession().disconnect("You have been IP banned from this server.");
			}
		} else {
			StringBuilder build = new StringBuilder();
			for(int index = 1; index < args.length; index++) {
				build.append(args[index] + " ");
			}
			
			OpenClassic.getServer().banIp(args[0], build.toString().trim());
			
			for(Player player : OpenClassic.getServer().getPlayers()) {
				if(player.getIp().equals(args[0])) player.getSession().disconnect(build.toString());
			}
		}
		
		sender.sendMessage(Color.GREEN + String.format(OpenClassic.getGame().getTranslator().translate("ban.banned"), args[0] + Color.GREEN));
	}
	
	@Command(aliases = {"unbanip"}, desc = "Unbans a player's IP.", permission = "openclassic.commands.unbanip", min = 1, usage = "<address>")
	public void unbanip(Sender sender, String command, String args[]) {
		OpenClassic.getServer().unbanIp(args[0]);
		sender.sendMessage(Color.GREEN + String.format(OpenClassic.getGame().getTranslator().translate("ban.unbanned"), args[0] + Color.GREEN));
	}
	
	@Command(aliases = {"kick"}, desc = "Kicks a player.", permission = "openclassic.commands.kick", min = 1, usage = "<player> [reason]")
	public void kick(Sender sender, String command, String args[]) {
		List<Player> players = OpenClassic.getServer().matchPlayer(args[0]);
		
		if(players.size() > 0) {
			if(players.size() > 1) {
				sender.sendMessage(Color.RED + OpenClassic.getGame().getTranslator().translate("command.multiple-players"));
				return;
			}
			
			Player player = players.get(0);
			Group group = OpenClassic.getServer().getPermissionManager().getPlayerGroup(player.getName());
			if(sender instanceof Player && group != null && !OpenClassic.getServer().getPermissionManager().getPlayerGroup(sender.getName()).isSubGroup(group)) {
				sender.sendMessage(Color.RED + OpenClassic.getGame().getTranslator().translate("command.deny-higher-group"));
				return;
			}
			
			if(args.length < 2) {
				player.disconnect("Kicked by " + sender.getName() + ".");
			} else {
				StringBuilder build = new StringBuilder();
				for(int index = 1; index < args.length; index++) {
					build.append(args[index] + " ");
				}
				
				player.disconnect("Kicked by " + sender.getName() + ", Reason: " + build.toString().trim());
			}
			
			sender.sendMessage(Color.GREEN + String.format(OpenClassic.getGame().getTranslator().translate("kick.kicked"), player.getName() + Color.GREEN));
		} else {
			sender.sendMessage(Color.RED + OpenClassic.getGame().getTranslator().translate("command.player-not-found"));
		}
	}
	
	@Command(aliases = {"whitelist"}, desc = "Whitelists a player.", permission = "openclassic.commands.whitelist", min = 1, usage = "<player>")
	public void whitelist(Sender sender, String command, String args[]) {
		OpenClassic.getServer().whitelist(args[0]);
		sender.sendMessage(Color.GREEN + String.format(OpenClassic.getGame().getTranslator().translate("whitelist.whitelisted"), args[0] + Color.GREEN));
	}
	
	@Command(aliases = {"unwhitelist"}, desc = "Unwhitelists a player.", permission = "openclassic.commands.unwhitelist", min = 1, usage = "<player>")
	public void unwhitelist(Sender sender, String command, String args[]) {
		Group group = OpenClassic.getServer().getPermissionManager().getPlayerGroup(args[0]);
		if(sender instanceof Player && group != null && !OpenClassic.getServer().getPermissionManager().getPlayerGroup(sender.getName()).isSubGroup(group)) {
			sender.sendMessage(Color.RED + OpenClassic.getGame().getTranslator().translate("command.deny-higher-group"));
			return;
		}
		
		OpenClassic.getServer().unwhitelist(args[0]);
		sender.sendMessage(Color.GREEN + String.format(OpenClassic.getGame().getTranslator().translate("whitelist.unwhitelisted"), args[0] + Color.GREEN));
	}
	
	@Command(aliases = {"setgroup"}, desc = "Sets the player's group.", permission = "openclassic.commands.setgroup", min = 2, usage = "<player> <group>")
	public void setgroup(Sender sender, String command, String args[]) {
		Group group = OpenClassic.getServer().getPermissionManager().getGroup(args[1]);
		if(group == null) {
			sender.sendMessage(Color.RED + OpenClassic.getGame().getTranslator().translate("group.invalid"));
			return;
		}
		
		if(sender instanceof Player && !OpenClassic.getServer().getPermissionManager().getPlayerGroup(sender.getName()).isSubGroup(group)) {
			sender.sendMessage(Color.RED + OpenClassic.getGame().getTranslator().translate("group.cant-set"));
			return;
		}
		
		List<Player> players = OpenClassic.getServer().matchPlayer(args[0]);
		
		if(players.size() > 0) {
			if(players.size() > 1) {
				sender.sendMessage(Color.RED + OpenClassic.getGame().getTranslator().translate("command.multiple-players"));
				return;
			}
			
			Player player = players.get(0);
			
			player.setGroup(group);
			player.sendMessage(Color.GREEN + String.format(OpenClassic.getGame().getTranslator().translate("group.set-notify"), args[1]));
			
			sender.sendMessage(Color.GREEN + String.format(OpenClassic.getGame().getTranslator().translate("group.set"), player.getDisplayName() + Color.GREEN, args[1]));
		} else {
			sender.sendMessage(Color.RED + OpenClassic.getGame().getTranslator().translate("command.player-not-found"));
		}
	}
	
	@Command(aliases = {"tp"}, desc = "Teleports you or another player to a player.", permission = "openclassic.commands.tp", min = 1, usage = "<tpto> [player]")
	public void tp(Sender sender, String command, String args[]) {		
		List<Player> players = OpenClassic.getServer().matchPlayer(args[0]);
			
		if(players.size() > 0) {
			if(players.size() > 1) {
				sender.sendMessage(Color.RED + OpenClassic.getGame().getTranslator().translate("command.multiple-players"));
				return;
			}
				
			Player player = players.get(0);
				
			if(args.length < 2) {
				if(sender instanceof Player) {
					((Player) sender).moveTo(player.getPosition());
					sender.sendMessage(Color.GREEN + String.format(OpenClassic.getGame().getTranslator().translate("teleport.teleport-to"), args[0]));
				} else {
					sender.sendMessage(Color.RED + OpenClassic.getGame().getTranslator().translate("command.player-only"));
				}
			} else {
				players = OpenClassic.getServer().matchPlayer(args[0]);
				
				if(players.size() > 0) {
					if(players.size() > 1) {
							sender.sendMessage(Color.RED + OpenClassic.getGame().getTranslator().translate("command.multiple-players"));
						return;
					}
						
					Player player2 = players.get(0);
					player2.moveTo(player.getPosition());
					player2.sendMessage(String.format(OpenClassic.getGame().getTranslator().translate("teleport.teleported-to"), sender.getDisplayName() + Color.GREEN, player.getDisplayName()));
					sender.sendMessage(String.format(OpenClassic.getGame().getTranslator().translate("teleport.teleported-to-notify"), player2.getDisplayName(), player.getDisplayName()));
				} else {
					sender.sendMessage(Color.RED + OpenClassic.getGame().getTranslator().translate("command.second-not-found"));
				}
			}
		} else {
			sender.sendMessage(Color.RED + OpenClassic.getGame().getTranslator().translate("command.player-not-found"));
		}
	}
	
	@Command(aliases = {"tphere"}, desc = "Teleports a player to you.", permission = "openclassic.commands.tphere", min = 1, usage = "<player>", senders = {Player.class})
	public void tphere(Sender sender, String command, String args[]) {
		List<Player> players = OpenClassic.getServer().matchPlayer(args[0]);
				
		if(players.size() > 0) {
			if(players.size() > 1) {
				sender.sendMessage(Color.RED + OpenClassic.getGame().getTranslator().translate("command.multiple-players"));
				return;
			}
					
			Player player = players.get(0);
			player.moveTo(((Player) sender).getPosition());
			sender.sendMessage(Color.GREEN + String.format(OpenClassic.getGame().getTranslator().translate("teleport.teleported-to-you"), player.getDisplayName() + Color.GREEN));
		} else {
			sender.sendMessage(Color.RED + OpenClassic.getGame().getTranslator().translate("command.player-not-found"));
		}
	}
	
	@Command(aliases = {"tpto"}, desc = "Teleports you to the given coordinates.", permission = "openclassic.commands.tpto", min = 3, usage = "<x> <y> <z> [level]", senders = {Player.class})
	public void tpto(Sender sender, String command, String args[]) {
		Player player = (Player) sender;
		int x = 0;
		int y = 0;
		int z = 0;
		
		try {
			x = Integer.parseInt(args[0]);
			y = Integer.parseInt(args[1]);
			z = Integer.parseInt(args[2]);
		} catch(NumberFormatException e) {
			sender.sendMessage(Color.RED + OpenClassic.getGame().getTranslator().translate("teleport.invalid-coords"));
			return;
		}
		
		Level level = player.getPosition().getLevel();
		
		if(args.length > 3) {
			level = OpenClassic.getServer().getLevel(args[3]);
		}
		
		if(level == null) {
			sender.sendMessage(Color.RED + OpenClassic.getGame().getTranslator().translate("level.level-not-found"));
			return;
		}
		
		player.moveTo(level, x, y, z);
	}
	
	@Command(aliases = {"goto", "g"}, desc = "Sends you to a level's spawn.", permission = "openclassic.commands.goto", min = 1, usage = "<level>", senders = {Player.class})
	public void gotoLevel(Sender sender, String command, String args[]) {
		Player player = (Player) sender;
		
		Level level = OpenClassic.getServer().getLevel(args[0]);
		if(level == null) {
			level = OpenClassic.getServer().loadLevel(args[0], false);
			if(level == null) {
				sender.sendMessage(Color.RED + OpenClassic.getGame().getTranslator().translate("level.level-not-found"));
				return;
			}
		}

		player.moveTo(level.getSpawn());
		OpenClassic.getServer().broadcastMessage(String.format(OpenClassic.getGame().getTranslator().translate("level.goto"), player.getDisplayName() + Color.AQUA, Color.GREEN + level.getName() + Color.AQUA));
	}
	
	@Command(aliases = {"levels"}, desc = "Lists all loaded levels.", permission = "openclassic.commands.levels")
	public void levels(Sender sender, String command, String args[]) {
		sender.sendMessage(Color.AQUA + OpenClassic.getGame().getTranslator().translate("level.loaded"));
		for(Level level : OpenClassic.getServer().getLevels()) {
			sender.sendMessage(Color.BLUE + level.getName());
		}
	}
	
	@Command(aliases = {"createlevel"}, desc = "Creates a level.", permission = "openclassic.commands.createlevel", min = 5, usage = "<name> <width> <height> <depth> <type>")
	public void createlevel(Sender sender, String command, String args[]) {
		Level level = OpenClassic.getServer().loadLevel(args[0], false);
		if(level != null) {
			sender.sendMessage(Color.RED + OpenClassic.getGame().getTranslator().translate("level.exists"));
			return;
		}
		
		if(!OpenClassic.getGame().isGenerator(args[4])) {
			sender.sendMessage(Color.RED + OpenClassic.getGame().getTranslator().translate("level.invalid-type"));
			return;
		}
		
		short width = 0;
		short height = 0;
		short depth = 0;
		
		try {
			width = Short.parseShort(args[1]);
			height = Short.parseShort(args[2]);
			depth = Short.parseShort(args[3]);
		} catch(NumberFormatException e) {
			sender.sendMessage(Color.RED + OpenClassic.getGame().getTranslator().translate("level.invalid-dim"));
			return;
		}
		
		Level lvl = OpenClassic.getGame().createLevel(new LevelInfo(args[0], null, width, height, depth), OpenClassic.getGame().getGenerator(args[4]));
		((ServerLevel) lvl).setAuthor(sender.getName());
		sender.sendMessage(Color.GREEN + OpenClassic.getGame().getTranslator().translate("level.create-success"));
	}
	
	@Command(aliases = {"loadlevel"}, desc = "Loads a level", permission = "openclassic.commands.loadlevel", min = 1, usage = "<name>")
	public void loadlevel(Sender sender, String command, String args[]) {
		if(OpenClassic.getServer().loadLevel(args[0], false) == null) {
			sender.sendMessage(Color.RED + OpenClassic.getGame().getTranslator().translate("level.level-not-found"));
		}
	}
	
	@Command(aliases = {"unloadlevel"}, desc = "Unloads a level.", permission = "openclassic.commands.unloadlevel", min = 1, usage = "<name>")
	public void unloadlevel(Sender sender, String command, String args[]) {
		if(OpenClassic.getServer().getLevel(args[0]) == null) {
			sender.sendMessage(Color.RED + OpenClassic.getGame().getTranslator().translate("level.not-loaded"));
			return;
		}
		
		OpenClassic.getServer().unloadLevel(args[0]);
	}
	
	@Command(aliases = {"solid", "bedrock"}, desc = "Toggles bedrock placement mode.", permission = "openclassic.commands.solid", senders = {Player.class})
	public void solid(Sender sender, String command, String args[]) {
		Player player = (Player) sender;
		if(player.getPlaceMode() != VanillaBlock.BEDROCK.getId()) {
			player.setPlaceMode(VanillaBlock.BEDROCK.getId());
			player.sendMessage(Color.GREEN + OpenClassic.getGame().getTranslator().translate("solid.enable"));
		} else {
			player.setPlaceMode(0);
			player.sendMessage(Color.GREEN + OpenClassic.getGame().getTranslator().translate("solid.disable"));
		}
	}
	
	@Command(aliases = {"water"}, desc = "Toggles water placement mode.", permission = "openclassic.commands.water", senders = {Player.class})
	public void water(Sender sender, String command, String args[]) {
		Player player = (Player) sender;
		if(player.getPlaceMode() != VanillaBlock.WATER.getId()) {
			player.setPlaceMode(VanillaBlock.WATER.getId());
			player.sendMessage(Color.GREEN + OpenClassic.getGame().getTranslator().translate("water.enable"));
		} else {
			player.setPlaceMode(0);
			player.sendMessage(Color.GREEN + OpenClassic.getGame().getTranslator().translate("water.disable"));
		}
	}
	
	@Command(aliases = {"stillwater"}, desc = "Toggles still water placement mode.", permission = "openclassic.commands.stillwater", senders = {Player.class})
	public void still_water(Sender sender, String command, String args[]) {
		Player player = (Player) sender;
		if(player.getPlaceMode() != VanillaBlock.STATIONARY_WATER.getId()) {
			player.setPlaceMode(VanillaBlock.STATIONARY_WATER.getId());
			player.sendMessage(Color.GREEN + OpenClassic.getGame().getTranslator().translate("stillwater.enable"));
		} else {
			player.setPlaceMode(0);
			player.sendMessage(Color.GREEN + OpenClassic.getGame().getTranslator().translate("stillwater.disable"));
		}
	}
	
	@Command(aliases = {"lava"}, desc = "Toggles lava placement mode.", permission = "openclassic.commands.lava", senders = {Player.class})
	public void lava(Sender sender, String command, String args[]) {
		Player player = (Player) sender;
		if(player.getPlaceMode() != VanillaBlock.LAVA.getId()) {
			player.setPlaceMode(VanillaBlock.LAVA.getId());
			player.sendMessage(Color.GREEN + OpenClassic.getGame().getTranslator().translate("lava.enable"));
		} else {
			player.setPlaceMode(0);
			player.sendMessage(Color.GREEN + OpenClassic.getGame().getTranslator().translate("lava.disable"));
		}
	}
	
	@Command(aliases = {"stilllava"}, desc = "Toggles still lava placement mode.", permission = "openclassic.commands.stilllava", senders = {Player.class})
	public void still_lava(Sender sender, String command, String args[]) {
		Player player = (Player) sender;
		if(player.getPlaceMode() != VanillaBlock.STATIONARY_LAVA.getId()) {
			player.setPlaceMode(VanillaBlock.STATIONARY_LAVA.getId());
			player.sendMessage(Color.GREEN + OpenClassic.getGame().getTranslator().translate("stilllava.enable"));
		} else {
			player.setPlaceMode(0);
			player.sendMessage(Color.GREEN + OpenClassic.getGame().getTranslator().translate("stilllava.disable"));
		}
	}
	
	@Command(aliases = {"say"}, desc = "Sends a server message.", usage = "<message>", permission = "openclassic.commands.say")
	public void say(Sender sender, String command, String args[]) {
		StringBuilder build = new StringBuilder();
		for(String arg : args) { 
			build.append(arg + " ");
		}
		
		OpenClassic.getServer().broadcastMessage(Color.PINK + "[Server] " + build.toString().trim());
	}
	
}
