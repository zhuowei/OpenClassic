package ch.spacebase.openclassic.server.io;

import java.io.IOException;
import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.level.Level;
import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.server.level.ServerLevel;

public class MinecraftLevelFormat {

	// TODO: BlockMap errors.
	public static Level read(String file) throws IOException {
		ServerLevel level = new ServerLevel();

		com.mojang.minecraft.level.Level lvlData = ((ClientLevel) ch.spacebase.openclassic.client.io.MinecraftLevelFormat.read(file)).getHandle();
		
		level.setSpawn(new Position(level, lvlData.xSpawn, lvlData.ySpawn, lvlData.zSpawn, (byte) lvlData.rotSpawn, (byte) 0));

		level.setName(lvlData.name);
		level.setAuthor(lvlData.creator);
		level.setCreationTime(lvlData.createTime);
			
		short width  = (short) lvlData.width;
		short height = (short) lvlData.height;
		short depth  = (short) lvlData.depth;
		byte[] blocks = lvlData.blocks;

		level.setWorldData(width, height, depth, blocks);
		return level;
	}
	
	private MinecraftLevelFormat() {
	}

}
