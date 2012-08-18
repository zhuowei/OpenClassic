package ch.spacebase.openclassic.server.network.codec.custom;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import ch.spacebase.openclassic.api.network.msg.custom.audio.MusicStopMessage;
import ch.spacebase.openclassic.server.network.codec.MessageCodec;
import ch.spacebase.openclassic.server.util.ChannelBufferUtils;

public class MusicStopCodec extends MessageCodec<MusicStopMessage> {

	public MusicStopCodec() {
		super(MusicStopMessage.class, (byte) 0x18);
	}

	@Override
	public ChannelBuffer encode(MusicStopMessage message) throws IOException {
		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
		ChannelBufferUtils.writeString(buffer, message.getIdentifier());
		return buffer;
	}

	@Override
	public MusicStopMessage decode(ChannelBuffer buffer) throws IOException {
		String identifier = ChannelBufferUtils.readString(buffer);
		return new MusicStopMessage(identifier);
	}
	
}
