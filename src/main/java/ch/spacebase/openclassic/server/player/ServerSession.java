package ch.spacebase.openclassic.server.player;

import java.net.SocketAddress;
import java.util.ArrayDeque;
import java.util.Queue;


import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.custom.CustomBlock;
import ch.spacebase.openclassic.api.event.EventFactory;
import ch.spacebase.openclassic.api.event.player.PlayerKickEvent;
import ch.spacebase.openclassic.api.event.player.PlayerQuitEvent;
import ch.spacebase.openclassic.api.network.msg.BlockChangeMessage;
import ch.spacebase.openclassic.api.network.msg.Message;
import ch.spacebase.openclassic.api.network.msg.PlayerDespawnMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerDisconnectMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerSpawnMessage;
import ch.spacebase.openclassic.api.network.msg.PlayerTeleportMessage;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.player.Session;
import ch.spacebase.openclassic.server.network.HandlerLookupService;
import ch.spacebase.openclassic.server.network.handler.MessageHandler;


public class ServerSession implements Session {

	@SuppressWarnings("unused")
	private static final int TIMEOUT_TICKS = 300;

	private final Channel channel;
	private final Queue<Message> messageQueue = new ArrayDeque<Message>();
	@SuppressWarnings("unused")
	private int timeoutCounter = 0;
	private State state = State.IDENTIFYING;
	private ServerPlayer player;
	private boolean pendingRemoval = false;
	public boolean disconnectMsgSent = false;

	public ServerSession(Channel channel) {
		this.channel = channel;
	}

	public State getState() {
		return this.state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public Player getPlayer() {
		return this.player;
	}

	public void setPlayer(ServerPlayer player) {
		if (this.player != null)
			throw new IllegalStateException();

		this.player = player;
	}

	@SuppressWarnings("unchecked")
	public boolean tick() {
		if (this.pendingRemoval)
			return false;
		//this.timeoutCounter++;

		Message message;
		
		while ((message = this.messageQueue.poll()) != null) {
			MessageHandler<Message> handler = (MessageHandler<Message>) HandlerLookupService.find(message.getClass());
			
			if (handler != null) {
				handler.handle(this, player, message);
			}
			
			//this.timeoutCounter = 0;
		}

		/* if (this.timeoutCounter >= TIMEOUT_TICKS)
			disconnect("Connection timed out"); */
		
		//this.send(new PingMessage());

		return true;
	}

	public void send(Message message) {
		if(message instanceof BlockChangeMessage && !this.player.hasCustomClient()) {
			BlockType block = Blocks.fromId(((BlockChangeMessage) message).getBlock());
			if(block instanceof CustomBlock) {
				message = new BlockChangeMessage(((BlockChangeMessage) message).getX(), ((BlockChangeMessage) message).getY(), ((BlockChangeMessage) message).getZ(), ((CustomBlock) block).getFallback().getId());
			}
		}
		
		if(!this.canSendPlayerMessage(message, this.player)) return;
		if(message.getClass().getPackage().getName().contains("custom") && !this.player.hasCustomClient()) return;
		this.channel.write(message);
	}

	private boolean canSendPlayerMessage(Message message, ServerPlayer player) {
		if(message instanceof PlayerDespawnMessage && ((PlayerDespawnMessage) message).getPlayerId() != -1 && !player.canSee(OpenClassic.getServer().getPlayer(((PlayerDespawnMessage) message).getPlayerId()))) {
			return false;
		} else if(message instanceof PlayerTeleportMessage && ((PlayerTeleportMessage) message).getPlayerId() != -1 && !player.canSee(OpenClassic.getServer().getPlayer(((PlayerTeleportMessage) message).getPlayerId()))) {
			return false;
		} else if(message instanceof PlayerSpawnMessage && ((PlayerSpawnMessage) message).getPlayerId() != -1 && !player.canSee(OpenClassic.getServer().getPlayer(((PlayerSpawnMessage) message).getPlayerId()))) {
			return false;
		}
		
		return true;
	}

	public void disconnect(String reason) {
		if(this.player != null && this.state == State.GAME) {
			PlayerKickEvent event = EventFactory.callEvent(new PlayerKickEvent(this.player, reason, this.player.getDisplayName() + Color.AQUA + " has been kicked. (" + reason + Color.AQUA + ")"));
			if(event.isCancelled()) {
				return;
			}
			
			EventFactory.callEvent(new PlayerQuitEvent(this.player, this.player.getDisplayName() + Color.AQUA + " has been kicked. (" + reason + Color.AQUA + ")"));
			OpenClassic.getServer().broadcastMessage(event.getMessage());
		} else {
			OpenClassic.getLogger().info(this.channel.getRemoteAddress() + " disconnected by server: \"" + reason + "\"");
		}
		
		this.channel.write(new PlayerDisconnectMessage(reason)).addListener(ChannelFutureListener.CLOSE);
		this.disconnectMsgSent = true;
	}
	
	public SocketAddress getAddress() {
		return this.channel.getRemoteAddress();
	}

	@Override
	public String toString() {
		return ServerSession.class.getName() + " [address=" + this.channel.getRemoteAddress() + "]";
	}

	public <T extends Message> void messageReceived(T message) {
		this.messageQueue.add(message);
	}

	public void flagForRemoval() {
		this.pendingRemoval = true;
	}

	public void dispose() {
		if (this.player != null) {
			this.player.destroy();
			this.player = null;
		}
	}
	
}
