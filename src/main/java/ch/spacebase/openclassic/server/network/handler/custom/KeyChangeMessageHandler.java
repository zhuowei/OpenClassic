package ch.spacebase.openclassic.server.network.handler.custom;

import ch.spacebase.openclassic.api.event.EventFactory;
import ch.spacebase.openclassic.api.event.player.PlayerKeyChangeEvent;
import ch.spacebase.openclassic.api.network.msg.custom.KeyChangeMessage;
import ch.spacebase.openclassic.server.network.handler.MessageHandler;
import ch.spacebase.openclassic.server.player.ServerPlayer;
import ch.spacebase.openclassic.server.player.ServerSession;

public class KeyChangeMessageHandler extends MessageHandler<KeyChangeMessage> {

	@Override
	public void handle(ServerSession session, final ServerPlayer player, KeyChangeMessage message) {
		if(session == null || player == null) return;
		EventFactory.callEvent(new PlayerKeyChangeEvent(player, message.getKey(), message.isPressed()));
		
		// TODO: Add handling if player has gui open
	}

}
