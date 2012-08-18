package ch.spacebase.openclassic.server.network.codec;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import ch.spacebase.openclassic.api.network.msg.PlayerPositionRotationMessage;


public class PlayerPositionRotationCodec extends MessageCodec<PlayerPositionRotationMessage> {

	public PlayerPositionRotationCodec() {
		super(PlayerPositionRotationMessage.class, (byte) 0x09);
	}

	@Override
	public ChannelBuffer encode(PlayerPositionRotationMessage message) throws IOException {
		ChannelBuffer buffer = ChannelBuffers.buffer(9);
		
		buffer.writeByte(message.getPlayerId());
		buffer.writeShort((short) (message.getXChange() * 32));
		buffer.writeShort((short) (message.getYChange() * 32));
		buffer.writeShort((short) (message.getZChange() * 32));
		buffer.writeByte(message.getYaw());
		buffer.writeByte(message.getPitch());
		
		return buffer;
	}

	@Override
	public PlayerPositionRotationMessage decode(ChannelBuffer buffer) throws IOException {
		byte playerId = buffer.readByte();
		double xChange = buffer.readShort() / 32;
		double yChange = buffer.readShort() / 32;
		double zChange = buffer.readShort() / 32;
		byte yaw = buffer.readByte();
		byte pitch = buffer.readByte();
		
		return new PlayerPositionRotationMessage(playerId, xChange, yChange, zChange, yaw, pitch);
	}

}
