package ch.spacebase.openclassic.server.network.codec;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import ch.spacebase.openclassic.api.network.msg.PlayerRotationMessage;


public class PlayerRotationCodec extends MessageCodec<PlayerRotationMessage> {

	public PlayerRotationCodec() {
		super(PlayerRotationMessage.class, (byte) 0x0b);
	}

	@Override
	public ChannelBuffer encode(PlayerRotationMessage message) throws IOException {
		ChannelBuffer buffer = ChannelBuffers.buffer(3);
		
		buffer.writeByte(message.getPlayerId());
		buffer.writeByte(message.getYaw());
		buffer.writeByte(message.getPitch());
		
		return buffer;
	}

	@Override
	public PlayerRotationMessage decode(ChannelBuffer buffer) throws IOException {
		byte playerId = buffer.readByte();
		byte yaw = buffer.readByte();
		byte pitch = buffer.readByte();
		
		return new PlayerRotationMessage(playerId, yaw, pitch);
	}

}
