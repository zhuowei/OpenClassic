package ch.spacebase.openclassic.server.network.codec;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import ch.spacebase.openclassic.api.network.msg.PlayerDisconnectMessage;
import ch.spacebase.openclassic.server.util.ChannelBufferUtils;


public class PlayerDisconnectCodec extends MessageCodec<PlayerDisconnectMessage> {

	public PlayerDisconnectCodec() {
		super(PlayerDisconnectMessage.class, (byte) 0x0e);
	}

	@Override
	public ChannelBuffer encode(PlayerDisconnectMessage message) throws IOException {
		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
		
		ChannelBufferUtils.writeString(buffer, message.getMessage());
		
		return buffer;
	}

	@Override
	public PlayerDisconnectMessage decode(ChannelBuffer buffer) throws IOException {
		String message = ChannelBufferUtils.readString(buffer);
		
		return new PlayerDisconnectMessage(message);
	}

}
