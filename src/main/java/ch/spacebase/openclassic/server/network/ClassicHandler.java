package ch.spacebase.openclassic.server.network;

import java.util.logging.Level;

import javax.swing.DefaultListModel;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;


import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.event.EventFactory;
import ch.spacebase.openclassic.api.event.player.PlayerQuitEvent;
import ch.spacebase.openclassic.api.network.msg.Message;
import ch.spacebase.openclassic.server.ClassicServer;
import ch.spacebase.openclassic.server.player.ServerSession;
import ch.spacebase.openclassic.server.ui.GuiConsoleManager;

public class ClassicHandler extends SimpleChannelUpstreamHandler {

	@Override
	public void channelConnected(ChannelHandlerContext context, ChannelStateEvent event) {
		Channel channel = event.getChannel();
		((ClassicServer) OpenClassic.getGame()).getChannelGroup().add(channel);

		ServerSession session = new ServerSession(channel);
		((ClassicServer) OpenClassic.getGame()).getSessionRegistry().add(session);
		context.setAttachment(session);

		OpenClassic.getLogger().info("Channel connected: " + channel + ".");
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void channelDisconnected(ChannelHandlerContext context, ChannelStateEvent event) {
		Channel channel = event.getChannel();
		((ClassicServer) OpenClassic.getGame()).getChannelGroup().remove(channel);

		ServerSession session = (ServerSession) context.getAttachment();
		((ClassicServer) OpenClassic.getGame()).getSessionRegistry().remove(session);

		if(session.getPlayer() != null) {
			OpenClassic.getServer().broadcastMessage(EventFactory.callEvent(new PlayerQuitEvent(session.getPlayer(), String.format(OpenClassic.getGame().getTranslator().translate("player.logout"), session.getPlayer().getDisplayName() + Color.AQUA))).getMessage());
			if(((ClassicServer) OpenClassic.getServer()).getConsoleManager() instanceof GuiConsoleManager) {
				DefaultListModel model = ((GuiConsoleManager) ((ClassicServer) OpenClassic.getServer()).getConsoleManager()).getFrame().players;
				if(model.indexOf(session.getPlayer().getName()) != -1) {
					model.remove(model.indexOf(session.getPlayer().getName()));
				}
			}

			session.getPlayer().getPosition().getLevel().removePlayer(session.getPlayer().getName());
			if(!session.disconnectMsgSent) session.getPlayer().getData().save(OpenClassic.getServer().getDirectory().getPath() + "/players/" + session.getPlayer().getName() + ".nbt");
		} else {
			if(!session.disconnectMsgSent) OpenClassic.getLogger().info(channel.getRemoteAddress() + " disconnected.");
		}

		OpenClassic.getLogger().info("Channel disconnected: " + channel + ".");
	}

	@Override
	public void messageReceived(ChannelHandlerContext context, MessageEvent event) {
		ServerSession session = (ServerSession) context.getAttachment();
		session.messageReceived((Message) event.getMessage());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext context, ExceptionEvent event) {
		Channel channel = event.getChannel();

		if (channel.isOpen()) {
			if(!(event.getCause().getMessage() != null && (event.getCause().getMessage().equals("Connection reset by peer") || event.getCause().getMessage().equals("Connection timed out")))) OpenClassic.getLogger().log(Level.WARNING, "Exception caught, closing channel: " + channel + "...", event.getCause());
			channel.close();
		}
	}

}
