package ch.spacebase.openclassic.server.network.codec.custom;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import ch.spacebase.openclassic.api.network.msg.custom.audio.AudioRegisterMessage;
import ch.spacebase.openclassic.server.network.codec.MessageCodec;
import ch.spacebase.openclassic.server.util.ChannelBufferUtils;

public class AudioRegisterCodec extends MessageCodec<AudioRegisterMessage> {

	public AudioRegisterCodec() {
		super(AudioRegisterMessage.class, (byte) 0x16);
	}

	@Override
	public ChannelBuffer encode(AudioRegisterMessage message) throws IOException {
		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
		ChannelBufferUtils.writeString(buffer, message.getIdentifier());
		ChannelBufferUtils.writeString(buffer, message.getUrl());
		buffer.writeByte(message.isIncluded() ? 1 : 0);
		buffer.writeByte(message.isMusic() ? 1 : 0);
		return buffer;
	}

	@Override
	public AudioRegisterMessage decode(ChannelBuffer buffer) throws IOException {
		String identifier = ChannelBufferUtils.readString(buffer);
		String url = ChannelBufferUtils.readString(buffer);
		boolean included = buffer.readByte() == 1;
		boolean music = buffer.readByte() == 1;
		return new AudioRegisterMessage(identifier, url, included, music);
	}
	
}
