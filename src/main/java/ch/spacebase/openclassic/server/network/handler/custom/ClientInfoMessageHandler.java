package ch.spacebase.openclassic.server.network.handler.custom;

import ch.spacebase.openclassic.api.network.msg.custom.GameInfoMessage;
import ch.spacebase.openclassic.server.network.handler.MessageHandler;
import ch.spacebase.openclassic.server.player.ServerPlayer;
import ch.spacebase.openclassic.server.player.ServerSession;

public class ClientInfoMessageHandler extends MessageHandler<GameInfoMessage> {

	@Override
	public void handle(ServerSession session, final ServerPlayer player, GameInfoMessage message) {
		if(session == null || player == null) return;
		player.getClientInfo().setVersion(message.getVersion());
		player.getClientInfo().setLanguage(message.getLanguage());
	}

}
