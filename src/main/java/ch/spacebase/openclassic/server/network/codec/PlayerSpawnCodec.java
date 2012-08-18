package ch.spacebase.openclassic.server.network.codec;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import ch.spacebase.openclassic.api.network.msg.PlayerSpawnMessage;
import ch.spacebase.openclassic.server.util.ChannelBufferUtils;



public class PlayerSpawnCodec extends MessageCodec<PlayerSpawnMessage> {

	public PlayerSpawnCodec() {
		super(PlayerSpawnMessage.class, (byte) 0x07);
	}

	@Override
	public ChannelBuffer encode(PlayerSpawnMessage message) throws IOException {
		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
		
		buffer.writeByte(message.getPlayerId());
		ChannelBufferUtils.writeString(buffer, message.getName());
		buffer.writeShort((short) (message.getX() * 32));
		buffer.writeShort((short) (message.getY() * 32));
		buffer.writeShort((short) (message.getZ() * 32));
		buffer.writeByte(message.getYaw());
		buffer.writeByte(message.getPitch());
		
		return buffer;
	}

	@Override
	public PlayerSpawnMessage decode(ChannelBuffer buffer) throws IOException {
		byte playerId = buffer.readByte();
		String name = ChannelBufferUtils.readString(buffer);
		double x = buffer.readShort() / 32;
		double y = buffer.readShort() / 32;
		double z = buffer.readShort() / 32;
		byte yaw = buffer.readByte();
		byte pitch = buffer.readByte();
		
		return new PlayerSpawnMessage(playerId, name, x, y, z, yaw, pitch);
	}

}
