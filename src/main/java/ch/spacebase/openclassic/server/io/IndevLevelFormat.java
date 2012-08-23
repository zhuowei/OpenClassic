package ch.spacebase.openclassic.server.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.level.Level;
import ch.spacebase.openclassic.server.level.ServerLevel;


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
		ServerLevel level = new ServerLevel();
		
		NBTInputStream in = new NBTInputStream(new FileInputStream(new File(OpenClassic.getGame().getDirectory(), file)));
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
		
		double x = spawn.get(0).getValue() / 32;
		double y = spawn.get(1).getValue() / 32;
		double z = spawn.get(2).getValue() / 32;
		
		String name = ((StringTag) about.get("Name")).getValue();
		String author = ((StringTag) about.get("Author")).getValue();
		long created = ((LongTag) about.get("CreatedOn")).getValue();
		
		level.setName(name);
		level.setAuthor(author);
		level.setCreationTime(created);
		level.setWorldData(width, height, depth, blocks);
		level.setSpawn(new Position(level, x, y, z));
		
		in.close();	
		return level;
	}
	
	public static byte convert(byte input) {
		byte out = input;
		
		if(input == 50 || input == 51) out = 1;
		if(input == 52) out = 8;
		
		return out;
	}
	
}
