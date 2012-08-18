package ch.spacebase.openclassic.server.network.handler;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.event.EventFactory;
import ch.spacebase.openclassic.api.event.player.PlayerChatEvent;
import ch.spacebase.openclassic.api.network.msg.PlayerChatMessage;
import ch.spacebase.openclassic.api.player.Session.State;
import ch.spacebase.openclassic.server.ClassicServer;
import ch.spacebase.openclassic.server.player.ServerPlayer;
import ch.spacebase.openclassic.server.player.ServerSession;

public class PlayerChatMessageHandler extends MessageHandler<PlayerChatMessage> {

	@SuppressWarnings("unused")
	private static final String illegalChars = "[^0-9a-zA-Z!\"#$%&'()*+,-./:;<=>?@[\\]^_{|}~⌂ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«]";
	
	@Override
	public void handle(ServerSession session, ServerPlayer player, PlayerChatMessage message) {
		if(player == null || session == null) return;
		if(session.getState() != State.GAME) return;
		
		String chat = message.getMessage().trim();
		
		for(char ch : chat.toCharArray()) {
            if (ch < 32 || ch >= 127) {
                session.disconnect("Illegal character in chat!");
                return;
            }
        }
		
		if(chat.startsWith("/")) {
			((ClassicServer) OpenClassic.getGame()).processCommand(player, chat.substring(1, chat.length()));
		} else {
			PlayerChatEvent event = EventFactory.callEvent(new PlayerChatEvent(player, chat));
			if(event.isCancelled()) return;
			
			OpenClassic.getServer().sendToAll(new PlayerChatMessage(player.getPlayerId(), String.format(event.getFormat(), player.getDisplayName(), event.getMessage())));
			OpenClassic.getLogger().info(String.format(event.getFormat(), player.getDisplayName(), event.getMessage()));
		}
	}

}
