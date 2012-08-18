package ch.spacebase.openclassic.game.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.level.Level;

import ch.spacebase.opennbt.stream.NBTInputStream;
import ch.spacebase.opennbt.tag.ByteArrayTag;
import ch.spacebase.opennbt.tag.CompoundTag;
import ch.spacebase.opennbt.tag.ListTag;
import ch.spacebase.opennbt.tag.LongTag;
import ch.spacebase.opennbt.tag.ShortTag;
import ch.spacebase.opennbt.tag.StringTag;

public class IndevLevelFormat {
	
	@SuppressWarnings("unchecked")
	public static Level read(String file) throws IOException {
		com.mojang.minecraft.level.Level level = new com.mojang.minecraft.level.Level();
		
		File f = new File(OpenClassic.getGame().getDirectory(), file);
		NBTInputStream in = new NBTInputStream(new FileInputStream(f));
		CompoundTag data = (CompoundTag) in.readTag();
		CompoundTag map = (CompoundTag) data.get("Map");
		ListTag<ShortTag> spawn = (ListTag<ShortTag>) map.get("Spawn");
		CompoundTag about = (CompoundTag) data.get("About");
		
		short width = ((ShortTag) map.get("Width")).getValue();
		short height = ((ShortTag) map.get("Height")).getValue();
		short depth = ((ShortTag) map.get("Length")).getValue();
		byte[] blocks = ((ByteArrayTag) map.get("Blocks")).getValue();
		
		for(int index = 0; index < blocks.length; index++) {
			blocks[index] = convert(blocks[index]);
		}
		
		double x = spawn.get(0).getValue() / 32d;
		double y = spawn.get(1).getValue() / 32d;
		double z = spawn.get(2).getValue() / 32d;
		
		String name = ((StringTag) about.get("Name")).getValue();
		String author = ((StringTag) about.get("Author")).getValue();
		long created = ((LongTag) about.get("CreatedOn")).getValue();
		
		level.name = name;
		level.creator = author;
		level.createTime = created;
		level.setData(width, height, depth, blocks);
		
		level.xSpawn = (short) x;
		level.ySpawn = (short) y;
		level.zSpawn = (short) z;
		
		in.close();
		
		try {
			f.delete();
		} catch(SecurityException e) {
			e.printStackTrace();
		}
		
		return level.openclassic;
	}
	
	public static byte convert(byte input) {
		byte out = input;
		
		if(input == 50 || input == 51) out = 1;
		if(input == 52) out = 8;
		
		return out;
	}
	
}
