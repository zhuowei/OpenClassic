package ch.spacebase.openclassic.server.network.codec.custom;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import ch.spacebase.openclassic.api.network.msg.custom.LevelColorMessage;
import ch.spacebase.openclassic.server.network.codec.MessageCodec;
import ch.spacebase.openclassic.server.util.ChannelBufferUtils;

public class LevelColorCodec extends MessageCodec<LevelColorMessage> {

	public LevelColorCodec() {
		super(LevelColorMessage.class, (byte) 0x15);
	}

	@Override
	public ChannelBuffer encode(LevelColorMessage message) throws IOException {
		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
		ChannelBufferUtils.writeString(buffer, message.getType());
		buffer.writeInt(message.getValue());
		return buffer;
	}

	@Override
	public LevelColorMessage decode(ChannelBuffer buffer) throws IOException {
		String type = ChannelBufferUtils.readString(buffer);
		int value = buffer.readInt();
		return new LevelColorMessage(type, value);
	}
	
}
