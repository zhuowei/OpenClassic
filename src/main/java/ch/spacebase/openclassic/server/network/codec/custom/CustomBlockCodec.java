package ch.spacebase.openclassic.server.network.codec.custom;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.StepSound;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.block.custom.CustomBlock;
import ch.spacebase.openclassic.api.network.msg.custom.block.CustomBlockMessage;
import ch.spacebase.openclassic.server.network.codec.MessageCodec;
import ch.spacebase.openclassic.server.util.ChannelBufferUtils;

public class CustomBlockCodec extends MessageCodec<CustomBlockMessage> {

	public CustomBlockCodec() {
		super(CustomBlockMessage.class, (byte) 0x11);
	}

	@Override
	public ChannelBuffer encode(CustomBlockMessage message) throws IOException {
		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
		buffer.writeByte(message.getBlock().getId());
		buffer.writeByte(message.getBlock().isOpaque() ? 1 : 0);
		buffer.writeByte(message.getBlock().isSelectable() ? 1 : 0);
		ChannelBufferUtils.writeString(buffer, message.getBlock().getStepSound().name());
		buffer.writeByte(message.getBlock().isLiquid() ? 1 : 0);
		buffer.writeInt(message.getBlock().getTickDelay());
		buffer.writeByte(message.getBlock().getFallback().getId());
		buffer.writeByte(message.getBlock().isSolid() ? 1 : 0);
		
		return buffer;
	}

	@Override
	public CustomBlockMessage decode(ChannelBuffer buffer) throws IOException {
		byte id = buffer.readByte();
		boolean opaque = buffer.readByte() == 1;
		boolean selectable = buffer.readByte() == 1;
		StepSound sound = StepSound.valueOf(ChannelBufferUtils.readString(buffer));
		boolean liquid = buffer.readByte() == 1;
		int delay = buffer.readInt();
		VanillaBlock fallback = (VanillaBlock) Blocks.fromId(buffer.readByte());
		boolean solid = buffer.readByte() == 1;
		
		CustomBlock block = new CustomBlock(id, sound, null, opaque, liquid, selectable);
		block.setTickDelay(delay);
		block.setFallback(fallback);
		block.setSolid(solid);
		
		return new CustomBlockMessage(block);
	}
	
}
