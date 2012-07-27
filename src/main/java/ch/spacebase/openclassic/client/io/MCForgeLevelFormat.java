package ch.spacebase.openclassic.client.io;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.level.Level;

public class MCForgeLevelFormat {
	public static Level load(String file) throws Exception {
		com.mojang.minecraft.level.Level level = new com.mojang.minecraft.level.Level();

		File f = new File(file);
		FileInputStream in = new FileInputStream(f);
		DataInputStream data = new DataInputStream(in);
		long magic = data.readLong();
		if (magic != 7882256401675281664L) {
			OpenClassic.getLogger().severe("Only MCForge 6 levels are supported!");
			System.out.println(magic);
			return null;
		}
		byte version = data.readByte();
		if (version != 1) {
			OpenClassic.getLogger().severe("Unknown version!");
			return null;
		}
		String ldata = readString(data);
		int width = Integer.parseInt(ldata.split("\\@")[0]);
		int height = Integer.parseInt(ldata.split("\\@")[1]);
		int depth = Integer.parseInt(ldata.split("\\@")[2]);
		ldata = readString(data);
		level.xSpawn = Integer.parseInt(ldata.split("\\!")[0]);
		level.ySpawn = Integer.parseInt(ldata.split("\\!")[1]);
		level.zSpawn = Integer.parseInt(ldata.split("\\!")[2]);
		ldata = readString(data);
		//====METADATA CRAP==//
		int metadata = readInt(data);
		for (int i = 0; i < metadata; i++) {
			String key = readString(data);
			String value = readString(data);
		}
		//====METADATA CRAP==//
		int blocksize = readInt(data);
		int bytesize = readInt(data);
		byte[] compressed = new byte[bytesize];
		data.readFully(compressed);
		byte[] decompressed = decompress(compressed);
		byte[] blocks = new byte[width * depth * height];
		for (int i = 0; i < decompressed.length; i++)
			blocks[i] = translateBlock(decompressed[i]);
		level.setData(width, depth, height, blocks);
		data.close();
		try {
			f.delete();
		} catch(SecurityException e) {
			e.printStackTrace();
		}
		System.out.println("Done!");
		return level.openclassic;
	}

	public static byte translateBlock(byte id) {
		if (id <= 49)
			return id;

		if (id == 111)
			return VanillaBlock.LOG.getId();

		return VanillaBlock.AIR.getId();
	}
	
	private static String readString(DataInputStream data) {
		try {
			int sizea = readEncodedInt(data);
			byte[] array = new byte[sizea];
			data.readFully(array);
			String ldata = new String(array, "US-ASCII");
			return ldata;
		} catch (Exception e) {
			return "";
		}
	}
	
	private static int readInt(DataInputStream data) {
		try {
			byte[] temp = new byte[4];
			data.readFully(temp);
			return (int)temp[0] | (int)(temp[1] << 8) | (int)(temp[2] << 16) | (int)(temp[3] << 24);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}
	
	private static int readEncodedInt(DataInputStream data) throws Exception {
		int num = 0;
		int num2 = 0;
		while (num2 != 35) {
			byte b = data.readByte();
			num |= (int)(b & 127) << num2;
			num2 += 7;
			if ((b & 128) == 0)
				return num;
		}
		throw new Exception("Format Exception");
	}

	private static byte[] decompress(byte[] gzip) {
		try {
			final int size = 4096;
			ByteArrayInputStream ba = new ByteArrayInputStream(gzip);
			GZIPInputStream gz = new GZIPInputStream(ba);
			byte[] data = new byte[size];
			gz.read(data);
			gz.close();
			ba.close();
			return data;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new byte[0];
		}
	}

	private MCForgeLevelFormat() {
	}
}
