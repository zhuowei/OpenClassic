package ch.spacebase.openclassic.server.network.handler;

import ch.spacebase.openclassic.api.network.msg.Message;
import ch.spacebase.openclassic.server.player.ServerPlayer;
import ch.spacebase.openclassic.server.player.ServerSession;



public abstract class MessageHandler<T extends Message> {

	public abstract void handle(ServerSession session, ServerPlayer player, T message);
	
}
