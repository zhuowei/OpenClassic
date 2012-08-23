package ch.spacebase.openclassic.server.io;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.level.Level;
import ch.spacebase.openclassic.server.level.ServerLevel;

public class MCSharpLevelFormat {
	
	public static Level load(String file) throws IOException {
		ServerLevel level = new ServerLevel();

		FileInputStream in = new FileInputStream(new File(OpenClassic.getGame().getDirectory(), file));
		GZIPInputStream decompressor = new GZIPInputStream(in);

		DataInputStream data = new DataInputStream(decompressor);

		int magic = convert(data.readShort());

		if (magic != 1874) {
			OpenClassic.getLogger().severe("Only version 1 MCSharp maps are supported.");
			OpenClassic.getLogger().severe("Trying MCForge 6..");
			return MCForgeLevelFormat.load(file);
		}

		short width = convert(data.readShort());
		short height = convert(data.readShort());
		short depth = convert(data.readShort());

		level.setSpawn(new Position(level, data.readShort(), data.readShort(), data.readShort(), (byte) data.readUnsignedByte(), (byte) data.readUnsignedByte()));

		byte[] blocks = new byte[width * depth * height];

		for (int z = 0; z < depth; z++) {
			for (int y = 0; y < height; y++) {
				byte[] row = new byte[height];
				data.readFully(row);

				for (int x = 0; x < width; x++) {
					blocks[ServerLevel.coordsToBlockIndex(x, y, z, width, height, depth)] = translateBlock(row[x]);
				}
			}
		}

		level.setWorldData(width, height, depth, blocks);
		level.setName(file.replace(".lvl", ""));
		
		data.close();
		return level;
	}
	
	private static short convert(int convert) {
		return (short) (((convert >> 8) & 0xff) + ((convert << 8) & 0xff00));
	}

	public static byte translateBlock(byte id) {
		if (id <= 49)
			return id;
		
		if (id == 111)
			return VanillaBlock.LOG.getId();
		
		return VanillaBlock.AIR.getId();
	}

	private MCSharpLevelFormat() {
	}

}
