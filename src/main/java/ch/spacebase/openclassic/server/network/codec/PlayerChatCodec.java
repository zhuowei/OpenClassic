package ch.spacebase.openclassic.server.network.codec;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import ch.spacebase.openclassic.api.network.msg.PlayerChatMessage;
import ch.spacebase.openclassic.server.util.ChannelBufferUtils;


public class PlayerChatCodec extends MessageCodec<PlayerChatMessage> {

	public PlayerChatCodec() {
		super(PlayerChatMessage.class, (byte) 0x0d);
	}

	@Override
	public ChannelBuffer encode(PlayerChatMessage message) throws IOException {
		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
		
		buffer.writeByte(message.getPlayerId());
		ChannelBufferUtils.writeString(buffer, message.getMessage());
		
		return buffer;
	}

	@Override
	public PlayerChatMessage decode(ChannelBuffer buffer) throws IOException {
		byte playerId = buffer.readByte();
		String message = ChannelBufferUtils.readString(buffer);
		
		return new PlayerChatMessage(playerId, message);
	}

}
