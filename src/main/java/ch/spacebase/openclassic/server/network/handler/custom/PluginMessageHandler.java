package ch.spacebase.openclassic.server.network.handler.custom;

import ch.spacebase.openclassic.api.network.msg.custom.PluginMessage;
import ch.spacebase.openclassic.api.plugin.RemotePluginInfo;
import ch.spacebase.openclassic.server.network.handler.MessageHandler;
import ch.spacebase.openclassic.server.player.ServerPlayer;
import ch.spacebase.openclassic.server.player.ServerSession;

public class PluginMessageHandler extends MessageHandler<PluginMessage> {

	@Override
	public void handle(ServerSession session, ServerPlayer player, PluginMessage message) {
		if(session == null || player == null) return;
		player.getClientInfo().addPlugin(new RemotePluginInfo(message.getName(), message.getVersion()));
	}

}
