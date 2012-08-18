package ch.spacebase.openclassic.server.network.handler.custom;

import ch.spacebase.openclassic.api.event.EventFactory;
import ch.spacebase.openclassic.api.event.player.CustomMessageEvent;
import ch.spacebase.openclassic.api.network.msg.custom.CustomMessage;
import ch.spacebase.openclassic.server.network.handler.MessageHandler;
import ch.spacebase.openclassic.server.player.ServerPlayer;
import ch.spacebase.openclassic.server.player.ServerSession;

public class CustomMessageHandler extends MessageHandler<CustomMessage> {

	@Override
	public void handle(ServerSession session, ServerPlayer player, CustomMessage message) {
		if(session == null || player == null) return;
		EventFactory.callEvent(new CustomMessageEvent(player, message));
	}

}
