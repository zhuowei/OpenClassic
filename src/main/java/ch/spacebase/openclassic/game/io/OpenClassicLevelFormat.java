package ch.spacebase.openclassic.game.io;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.level.Level;
import ch.spacebase.openclassic.api.level.LevelInfo;
import ch.spacebase.openclassic.api.level.generator.FlatLandGenerator;
import ch.spacebase.openclassic.api.util.io.IOUtils;

import ch.spacebase.opennbt.TagBuilder;
import ch.spacebase.opennbt.stream.NBTInputStream;
import ch.spacebase.opennbt.stream.NBTOutputStream;
import ch.spacebase.opennbt.tag.ByteArrayTag;
import ch.spacebase.opennbt.tag.ByteTag;
import ch.spacebase.opennbt.tag.CompoundTag;
import ch.spacebase.opennbt.tag.DoubleTag;
import ch.spacebase.opennbt.tag.LongTag;
import ch.spacebase.opennbt.tag.ShortTag;
import ch.spacebase.opennbt.tag.StringTag;

public class OpenClassicLevelFormat {
	
	public static Level load(String name, boolean create) throws IOException {
		if(!(new File(OpenClassic.getGame().getDirectory(), "levels/" + name + ".map").exists())) {
			if(new File(OpenClassic.getGame().getDirectory(), "levels/" + name + ".mclevel").exists()) {
				OpenClassic.getLogger().info("Minecraft Indev map detected! Reading...");
				Level level = IndevLevelFormat.read("levels/" + name + ".mclevel");
				save(level);
				return level;
			}
			
			if(new File(OpenClassic.getGame().getDirectory(), "levels/" + name + ".mine").exists()) {
				OpenClassic.getLogger().info("Minecraft Classic map detected! Reading...");
				Level level = MinecraftLevelFormat.read("levels/" + name + ".mine");
				save(level);
				return level;
			}
			
			if(new File(OpenClassic.getGame().getDirectory(), "levels/" + name + ".dat").exists()) {
				OpenClassic.getLogger().info("Minecraft Classic map detected! Reading...");
				Level level = MinecraftLevelFormat.read("levels/" + name + ".dat");
				save(level);
				return level;
			}
			
			if(new File(OpenClassic.getGame().getDirectory(), "levels/" + name + ".lvl").exists()) {
				OpenClassic.getLogger().info("MCSharp map detected! Reading...");
				Level level = MCSharpLevelFormat.load("levels/" + name + ".lvl");
				save(level);
				return level;
			}
			
			if(new File(OpenClassic.getGame().getDirectory(), "levels/" + name + ".oclvl").exists()) {
				OpenClassic.getLogger().info("Old OpenClassic map detected! Reading...");
				Level level = readOld("levels/" + name + ".oclvl");
				save(level);
				return level;
			}
		}

		com.mojang.minecraft.level.Level level = new com.mojang.minecraft.level.Level();
		
		File levelFile = new File(OpenClassic.getGame().getDirectory(), "levels/" + name + ".map");
		if(!levelFile.exists()) {
			if(create) {
				OpenClassic.getLogger().info("Level \"" + name + "\" was not found. Creating with default settings...");
				return OpenClassic.getGame().createLevel(new LevelInfo(name, new Position(null, 0, 65, 0), (short) 256, (short) 64, (short) 256), new FlatLandGenerator());
			} else {
				return null;
			}
		}
		
		FileInputStream in = new FileInputStream(levelFile);
		NBTInputStream nbt = new NBTInputStream(in);
		CompoundTag root = (CompoundTag) nbt.readTag();
		CompoundTag info = (CompoundTag) root.get("Info");
		CompoundTag spawn = (CompoundTag) root.get("Spawn");
		CompoundTag map = (CompoundTag) root.get("Map");
		
		level.name = ((StringTag) info.get("Name")).getValue();
		level.creator = ((StringTag) info.get("Author")).getValue();
		level.createTime = ((LongTag) info.get("CreationTime")).getValue();
		
		level.xSpawn = ((DoubleTag) spawn.get("x")).getValue().shortValue();
		level.ySpawn = ((DoubleTag) spawn.get("y")).getValue().shortValue();
		level.zSpawn = ((DoubleTag) spawn.get("z")).getValue().shortValue();
		level.rotSpawn = ((ByteTag) spawn.get("yaw")).getValue();
		
		short width = ((ShortTag) map.get("Width")).getValue();
		short height = ((ShortTag) map.get("Height")).getValue();
		short depth = ((ShortTag) map.get("Depth")).getValue();
		byte blocks[] = ((ByteArrayTag) map.get("Blocks")).getValue();
		level.setData(width, height, depth, blocks);

		nbt.close();
		return level.openclassic;
	}
	
	public static Level readOld(String file) throws IOException {
		com.mojang.minecraft.level.Level level = new com.mojang.minecraft.level.Level();
		
		FileInputStream in = new FileInputStream(new File(OpenClassic.getGame().getDirectory(), file));
		GZIPInputStream gzipIn = new GZIPInputStream(in);
		DataInputStream data = new DataInputStream(gzipIn);
		
		level.name = IOUtils.readString(data);
		level.creator = IOUtils.readString(data);
		level.createTime = data.readLong();
		
		level.xSpawn = (short) data.readDouble();
		level.ySpawn = (short) data.readDouble();
		level.zSpawn = (short) data.readDouble();
		level.rotSpawn = data.readByte();
		data.readByte();
		
		short width = data.readShort();
		short height = data.readShort();
		short depth = data.readShort();
		
		byte[] blocks = new byte[width * depth * height];
		data.read(blocks);

		level.setData(width, depth, height, blocks);
		
		data.close();
		return level.openclassic;
	}
	
	public static void save(Level level) throws IOException {
		FileOutputStream out = new FileOutputStream(new File(OpenClassic.getGame().getDirectory(), "levels/" + level.getName() + ".map"));
		NBTOutputStream nbt = new NBTOutputStream(out);
		
		TagBuilder root = new TagBuilder("Level");
		
		TagBuilder info = new TagBuilder("Info");
		info.append("Name", level.getName());
		info.append("Author", level.getAuthor());
		info.append("CreationTime", level.getCreationTime());
		root.append(info);
		
		TagBuilder spawn = new TagBuilder("Spawn");
		spawn.append("x", level.getSpawn().getX());
		spawn.append("y", level.getSpawn().getY());
		spawn.append("z", level.getSpawn().getZ());
		spawn.append("yaw", level.getSpawn().getYaw());
		spawn.append("pitch", level.getSpawn().getPitch());
		root.append(spawn);
		
		TagBuilder map = new TagBuilder("Map");
		map.append("Width", level.getWidth());
		map.append("Height", level.getHeight());
		map.append("Depth", level.getDepth());
		map.append("Blocks", level.getBlocks());
		root.append(map);
		
		nbt.writeTag(root.toCompoundTag());
		nbt.close();
	}
	
	private OpenClassicLevelFormat() {
	}
	
}
