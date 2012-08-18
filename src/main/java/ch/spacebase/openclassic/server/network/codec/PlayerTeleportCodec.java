package ch.spacebase.openclassic.server.network.codec;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import ch.spacebase.openclassic.api.network.msg.PlayerTeleportMessage;


public class PlayerTeleportCodec extends MessageCodec<PlayerTeleportMessage> {

	public PlayerTeleportCodec() {
		super(PlayerTeleportMessage.class, (byte) 0x08);
	}

	@Override
	public ChannelBuffer encode(PlayerTeleportMessage message) throws IOException {
		ChannelBuffer buffer = ChannelBuffers.buffer(9);
		
		buffer.writeByte(message.getPlayerId());
		buffer.writeShort((short) (message.getX() * 32));
		buffer.writeShort((short) (message.getY() * 32));
		buffer.writeShort((short) (message.getZ() * 32));
		buffer.writeByte(message.getYaw());
		buffer.writeByte(message.getPitch());
		
		return buffer;
	}

	@Override
	public PlayerTeleportMessage decode(ChannelBuffer buffer) throws IOException {
		byte playerId = buffer.readByte();
		double x = buffer.readShort() / 32;
		double y = buffer.readShort() / 32;
		double z = buffer.readShort() / 32;
		byte yaw = buffer.readByte();
		byte pitch = buffer.readByte();
		
		return new PlayerTeleportMessage(playerId, x, y, z, yaw, pitch);
	}

}
