package ch.spacebase.openclassic.server.network.codec;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import ch.spacebase.openclassic.api.network.msg.LevelFinalizeMessage;

public class LevelFinalizeCodec extends MessageCodec<LevelFinalizeMessage> {

	public LevelFinalizeCodec() {
		super(LevelFinalizeMessage.class, (byte) 0x04);
	}

	@Override
	public ChannelBuffer encode(LevelFinalizeMessage message) throws IOException {
		ChannelBuffer buffer = ChannelBuffers.buffer(6);
		
		buffer.writeShort(message.getWidth());
		buffer.writeShort(message.getHeight());
		buffer.writeShort(message.getDepth());
		
		return buffer;
	}

	@Override
	public LevelFinalizeMessage decode(ChannelBuffer buffer) throws IOException {
		short width = buffer.readShort();
		short height = buffer.readShort();
		short depth = buffer.readShort();
		
		return new LevelFinalizeMessage(width, height, depth);
	}
	
}
