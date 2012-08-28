package ch.spacebase.openclassic.server.network.handler;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.swing.DefaultListModel;


import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.custom.CustomBlock;
import ch.spacebase.openclassic.api.block.model.Quad;
import ch.spacebase.openclassic.api.event.EventFactory;
import ch.spacebase.openclassic.api.event.player.PlayerConnectEvent;
import ch.spacebase.openclassic.api.event.player.PlayerJoinEvent;
import ch.spacebase.openclassic.api.event.player.PlayerLoginEvent;
import ch.spacebase.openclassic.api.level.Level;
import ch.spacebase.openclassic.api.network.msg.IdentificationMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerTeleportMessage;
import ch.spacebase.openclassic.api.network.msg.custom.GameInfoMessage;
import ch.spacebase.openclassic.api.network.msg.custom.PluginMessage;
import ch.spacebase.openclassic.api.network.msg.custom.block.BlockModelMessage;
import ch.spacebase.openclassic.api.network.msg.custom.block.CustomBlockMessage;
import ch.spacebase.openclassic.api.network.msg.custom.block.QuadMessage;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.player.Session;
import ch.spacebase.openclassic.api.player.Session.State;
import ch.spacebase.openclassic.api.plugin.Plugin;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.server.ClassicServer;
import ch.spacebase.openclassic.server.player.ServerPlayer;
import ch.spacebase.openclassic.server.player.ServerSession;
import ch.spacebase.openclassic.server.ui.GuiConsoleManager;

public class IdentificationMessageHandler extends MessageHandler<IdentificationMessage> {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void handle(ServerSession session, ServerPlayer pl, IdentificationMessage message) {
		if (session == null) 
			return;
		
		if(session.getState() == State.GAME) return;
		
		String ip = session.getAddress().toString().replace("/", "").split(":")[0];
		PlayerConnectEvent event = EventFactory.callEvent(new PlayerConnectEvent(message.getUsernameOrServerName(), session.getAddress()));
		if(event.getResult() != PlayerConnectEvent.Result.ALLOWED) {
			if(event.getKickMessage() == null || event.getKickMessage().equals("")) {
				this.kickFromResult(message.getUsernameOrServerName(), ip, session, event.getResult());
			} else {
				session.disconnect(event.getKickMessage());
			}
		}
		
		char[] nameChars = message.getUsernameOrServerName().toCharArray();
		
		for (char nameChar : nameChars) {
			if (nameChar < ' ' || nameChar > '\177') {
				session.disconnect(OpenClassic.getGame().getTranslator().translate("disconnect.invalid-user"));
				return;
			}
		}
		
		if(message.getUsernameOrServerName().length() < 2 || message.getUsernameOrServerName().length() > 16) {
			session.disconnect(OpenClassic.getGame().getTranslator().translate("disconnect.user-length"));
			return;
		}

		for (Player p : OpenClassic.getServer().getPlayers()) {
			if (p.getName().equalsIgnoreCase(message.getUsernameOrServerName())) {
				p.getSession().disconnect(OpenClassic.getGame().getTranslator().translate("disconnect.login-location"));
				break;
			}
		}
		
		if(message.getProtocolVersion() != Constants.PROTOCOL_VERSION) {
			session.disconnect(OpenClassic.getGame().getTranslator().translate("disconnect.version-mismatch"));
			return;
		}
		
		session.setState(State.IDENTIFYING);
		
		if(OpenClassic.getServer().isOnlineMode()) {
			try {
				String hash = ((ClassicServer) OpenClassic.getGame()).getURLSalt() + message.getUsernameOrServerName();
				
				if (!message.getVerificationKeyOrMotd().equals(this.md5(hash))) {
					session.disconnect(OpenClassic.getGame().getTranslator().translate("disconnect.verify-failed"));
					return;
				}
			} catch (NoSuchAlgorithmException e) {
				session.disconnect(OpenClassic.getGame().getTranslator().translate("disconnect.verify-error"));
				OpenClassic.getLogger().severe("Authentication error: MD5 algorithm not supported?");
				return;
			}
		}
		
		session.setState(State.PREPARING);
		
		PlayerLoginEvent.Result result = PlayerLoginEvent.Result.ALLOWED;
		String msg = "";
		if(!OpenClassic.getServer().isWhitelisted(message.getUsernameOrServerName()) && OpenClassic.getServer().doesUseWhitelist()) {
			result = PlayerLoginEvent.Result.KICK_WHITELIST;
			msg = OpenClassic.getGame().getTranslator().translate("disconnect.not-whitelisted");
		}
		
		if(OpenClassic.getServer().isIpBanned(ip)) {
			result = PlayerLoginEvent.Result.KICK_BANNED;
			msg = OpenClassic.getServer().getIpBanReason(ip);
		}
		
		if(OpenClassic.getServer().isBanned(message.getUsernameOrServerName())) {
			result = PlayerLoginEvent.Result.KICK_BANNED;
			msg = OpenClassic.getServer().getBanReason(message.getUsernameOrServerName());
		}
		
		boolean ignore = OpenClassic.getServer().getPermissionManager().getPlayerGroup(message.getUsernameOrServerName()).hasPermission("openclassic.ignore-max-players");
		if(OpenClassic.getServer().getPlayers().size() >= OpenClassic.getServer().getMaxPlayers() && !ignore) {
			result = PlayerLoginEvent.Result.KICK_FULL;
			msg = OpenClassic.getGame().getTranslator().translate("disconnect.server-full");
		}

		final ServerPlayer player = new ServerPlayer(message.getUsernameOrServerName(), OpenClassic.getServer().getDefaultLevel().getSpawn().clone(), session);
		PlayerLoginEvent e = EventFactory.callEvent(new PlayerLoginEvent(player, session.getAddress(), result, msg));
		if(e.getResult() != PlayerLoginEvent.Result.ALLOWED) {
			if(e.getKickMessage() == null || e.getKickMessage().equals("")) {
				this.kickFromLoginResult(message.getUsernameOrServerName(), ip, session, e.getResult());
			} else {
				session.disconnect(e.getKickMessage());
			}
		}
		
		if(((ClassicServer) OpenClassic.getServer()).getConsoleManager() instanceof GuiConsoleManager) {
			DefaultListModel model = ((GuiConsoleManager) ((ClassicServer) OpenClassic.getServer()).getConsoleManager()).getFrame().players;
			model.add(model.size(), player.getName());
			if(model.capacity() == model.size()) model.setSize(model.getSize() + 1);
		}
		
		Level level = player.getPosition().getLevel();
		if(level == null) {
			level = OpenClassic.getServer().getDefaultLevel();
			
			// Just in case
			player.setPosition(level.getSpawn().clone());
			player.getPosition().setLevel(level);
		}
		
		level.addPlayer(player);

		session.send(new IdentificationMessage(Constants.PROTOCOL_VERSION, OpenClassic.getServer().getServerName(), OpenClassic.getServer().getMotd(), player.getGroup().hasPermission("openclassic.commands.solid") ? Constants.OP : Constants.NOT_OP));
		if(message.getOpOrCustomClient() != 0) {
			if(message.getOpOrCustomClient() == Constants.OPENCLASSIC_PROTOCOL_VERSION) {
				player.getClientInfo().setCustom(true);
				OpenClassic.getLogger().info(Color.GREEN + player.getName() + " is using the OpenClassic Client! Sending custom blocks...");
				StringBuilder build = new StringBuilder();
				for(Plugin plugin : OpenClassic.getServer().getPluginManager().getPlugins()) {
					build.append(plugin.getDescription().getName()).append(",");
				}
				
				session.send(new GameInfoMessage(Constants.SERVER_VERSION, ""));
				for(Plugin plugin : OpenClassic.getServer().getPluginManager().getPlugins()) {
					session.send(new PluginMessage(plugin.getDescription().getName(), plugin.getDescription().getVersion()));
				}

				for(BlockType block : Blocks.getBlocks()) {
					if(block instanceof CustomBlock) {
						player.getSession().send(new CustomBlockMessage((CustomBlock) block));
						player.getSession().send(new BlockModelMessage(block.getId(), block.getModel()));
						for(Quad quad : block.getModel().getQuads()) {
							player.getSession().send(new QuadMessage(block.getId(), quad));
						}
					}
				}
			} else {
				player.sendMessage(Color.RED + "Your OpenClassic Client was detected, however its protocol version does not match the server's.");
				player.sendMessage(Color.RED + "You can still play, but you will not get enhanced client features.");
			}
		}
		
		player.sendLevel(player.getPosition().getLevel());
		session.send(new PlayerTeleportMessage((byte) -1, player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ(), player.getPosition().getYaw(), player.getPosition().getPitch()));
		
		session.setState(State.GAME);
		OpenClassic.getServer().broadcastMessage(EventFactory.callEvent(new PlayerJoinEvent(player, String.format(OpenClassic.getGame().getTranslator().translate("player.login"), player.getDisplayName() + Color.AQUA))).getMessage());
	}

	private void kickFromLoginResult(String user, String ip, Session session, PlayerLoginEvent.Result result) {
		switch(result) {
		case KICK_FULL: session.disconnect(OpenClassic.getGame().getTranslator().translate("disconnect.server-full"));
		case KICK_BANNED: session.disconnect(OpenClassic.getServer().getIpBanReason(ip) != null ? OpenClassic.getServer().getIpBanReason(ip) : OpenClassic.getServer().getIpBanReason(user));
		case KICK_WHITELIST: session.disconnect(OpenClassic.getGame().getTranslator().translate("disconnect.not-whitelisted"));
		default: session.disconnect(OpenClassic.getGame().getTranslator().translate("disconnect.disallowed"));
		}
	}
	
	private void kickFromResult(String user, String ip, Session session, PlayerConnectEvent.Result result) {
		switch(result) {
		case KICK_FULL: session.disconnect(OpenClassic.getGame().getTranslator().translate("disconnect.server-full"));
		case KICK_BANNED: session.disconnect(OpenClassic.getServer().getIpBanReason(ip) != null ? OpenClassic.getServer().getIpBanReason(ip) : OpenClassic.getServer().getIpBanReason(user));
		case KICK_WHITELIST: session.disconnect(OpenClassic.getGame().getTranslator().translate("disconnect.not-whitelisted"));
		default: session.disconnect(OpenClassic.getGame().getTranslator().translate("disconnect.disallowed"));
		}
	}

	public String md5(String string) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("MD5");
		digest.update(string.getBytes());
		
		return new BigInteger(1, digest.digest()).toString(16);
	}

}
