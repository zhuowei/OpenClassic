package ch.spacebase.openclassic.server.network.codec.custom;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import ch.spacebase.openclassic.api.network.msg.custom.PluginMessage;
import ch.spacebase.openclassic.server.network.codec.MessageCodec;
import ch.spacebase.openclassic.server.util.ChannelBufferUtils;

public class PluginCodec extends MessageCodec<PluginMessage> {

	public PluginCodec() {
		super(PluginMessage.class, (byte) 0x19);
	}

	@Override
	public ChannelBuffer encode(PluginMessage message) throws IOException {
		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
		ChannelBufferUtils.writeString(buffer, message.getName());
		ChannelBufferUtils.writeString(buffer, message.getVersion());
		
		return buffer;
	}

	@Override
	public PluginMessage decode(ChannelBuffer buffer) throws IOException {
		String name = ChannelBufferUtils.readString(buffer);
		String version = ChannelBufferUtils.readString(buffer);
		return new PluginMessage(name, version);
	}
	
}
