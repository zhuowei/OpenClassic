package ch.spacebase.openclassic.server.network.codec.custom;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import ch.spacebase.openclassic.api.block.model.BoundingBox;
import ch.spacebase.openclassic.api.block.model.CubeModel;
import ch.spacebase.openclassic.api.block.model.CuboidModel;
import ch.spacebase.openclassic.api.block.model.LiquidModel;
import ch.spacebase.openclassic.api.block.model.Model;
import ch.spacebase.openclassic.api.network.msg.custom.block.BlockModelMessage;
import ch.spacebase.openclassic.server.network.codec.MessageCodec;
import ch.spacebase.openclassic.server.util.ChannelBufferUtils;

public class BlockModelCodec extends MessageCodec<BlockModelMessage> {

	public BlockModelCodec() {
		super(BlockModelMessage.class, (byte) 0x12);
	}

	@Override
	public ChannelBuffer encode(BlockModelMessage message) throws IOException {
		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
		buffer.writeByte(message.getBlock());
		ChannelBufferUtils.writeString(buffer, message.getModel().getClass().getSimpleName());
		
		buffer.writeFloat(message.getModel().getDefaultCollisionBox().getX1());
		buffer.writeFloat(message.getModel().getDefaultCollisionBox().getX2());
		buffer.writeFloat(message.getModel().getDefaultCollisionBox().getY1());
		buffer.writeFloat(message.getModel().getDefaultCollisionBox().getY2());
		buffer.writeFloat(message.getModel().getDefaultCollisionBox().getZ1());
		buffer.writeFloat(message.getModel().getDefaultCollisionBox().getZ2());
		
		buffer.writeFloat(message.getModel().getDefaultCollisionBox().getX1());
		buffer.writeFloat(message.getModel().getDefaultCollisionBox().getX2());
		buffer.writeFloat(message.getModel().getDefaultCollisionBox().getY1());
		buffer.writeFloat(message.getModel().getDefaultCollisionBox().getY2());
		buffer.writeFloat(message.getModel().getDefaultCollisionBox().getZ1());
		buffer.writeFloat(message.getModel().getDefaultCollisionBox().getZ2());
		
		return buffer;
	}

	@Override
	public BlockModelMessage decode(ChannelBuffer buffer) throws IOException {
		byte block = buffer.readByte();
		String type = ChannelBufferUtils.readString(buffer);
		Model model = type.equals("TransparentModel") ? new LiquidModel("/terrain.png", 16) : (type.equals("CuboidModel") ? new CuboidModel("/terrain.png", 16, 0, 0, 0, 1, 1, 1) : (type.equals("CubeModel") ? new CubeModel("/terrain.png", 16) : new Model()));
		model.clearQuads();
		
		float x1 = buffer.readFloat();
		float x2 = buffer.readFloat();
		float y1 = buffer.readFloat();
		float y2 = buffer.readFloat();
		float z1 = buffer.readFloat();
		float z2 = buffer.readFloat();
		model.setCollisionBox(new BoundingBox(x1, y1, z1, x2, y2, z2));
		
		float sx1 = buffer.readFloat();
		float sx2 = buffer.readFloat();
		float sy1 = buffer.readFloat();
		float sy2 = buffer.readFloat();
		float sz1 = buffer.readFloat();
		float sz2 = buffer.readFloat();
		model.setSelectionBox(new BoundingBox(sx1, sy1, sz1, sx2, sy2, sz2));
		
		return new BlockModelMessage(block, model);
	}
	
}
