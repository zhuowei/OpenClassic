package ch.spacebase.openclassic.server.network.handler.custom;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.custom.CustomBlock;
import ch.spacebase.openclassic.api.block.model.Quad;
import ch.spacebase.openclassic.api.network.msg.custom.GameInfoMessage;
import ch.spacebase.openclassic.api.network.msg.custom.PluginMessage;
import ch.spacebase.openclassic.api.network.msg.custom.block.BlockModelMessage;
import ch.spacebase.openclassic.api.network.msg.custom.block.CustomBlockMessage;
import ch.spacebase.openclassic.api.network.msg.custom.block.QuadMessage;
import ch.spacebase.openclassic.api.plugin.Plugin;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.server.network.handler.MessageHandler;
import ch.spacebase.openclassic.server.player.ServerPlayer;
import ch.spacebase.openclassic.server.player.ServerSession;

public class ClientInfoMessageHandler extends MessageHandler<GameInfoMessage> {

	@Override
	public void handle(ServerSession session, final ServerPlayer player, GameInfoMessage message) {
		if(session == null || player == null) return;
		
		player.getClientInfo().setCustom(true);
		player.getClientInfo().setVersion(message.getVersion());
		player.getClientInfo().setLanguage(message.getLanguage());
		StringBuilder build = new StringBuilder();
		for(Plugin plugin : OpenClassic.getServer().getPluginManager().getPlugins()) {
			build.append(plugin.getDescription().getName()).append(",");
		}
		
		session.send(new GameInfoMessage(Constants.SERVER_VERSION, ""));
		for(Plugin plugin : OpenClassic.getServer().getPluginManager().getPlugins()) {
			session.send(new PluginMessage(plugin.getDescription().getName(), plugin.getDescription().getVersion()));
		}
		
		OpenClassic.getLogger().info(player.getName() + " is using OpenClassic client v" + message.getVersion() + "! Sending custom blocks...");
		
		for(BlockType block : Blocks.getBlocks()) {
			if(block instanceof CustomBlock) {
				player.getSession().send(new CustomBlockMessage((CustomBlock) block));
				player.getSession().send(new BlockModelMessage(block.getId(), block.getModel()));
				for(Quad quad : block.getModel().getQuads()) {
					player.getSession().send(new QuadMessage(block.getId(), quad));
				}
			}
		}
	}

}
