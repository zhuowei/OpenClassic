package ch.spacebase.openclassic.game.io;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import com.mojang.minecraft.gui.ErrorScreen;
import com.mojang.minecraft.level.LevelObjectStream;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.level.Level;
import ch.spacebase.openclassic.client.level.ClientLevel;

public class MinecraftLevelFormat {

	public static Level read(String file) throws IOException {
		File f = new File(OpenClassic.getGame().getDirectory(), file);
		FileInputStream fileStream = new FileInputStream(f);
		GZIPInputStream decompressor = new GZIPInputStream(fileStream);
		DataInputStream data = new DataInputStream(decompressor);

		int magic = data.readInt();
		byte version = data.readByte();
		
		if(magic != 0x271bb788) {
			throw new IOException("Unsupported map format!");
		}
		
		if(version != 2) {
			throw new IOException("Unsupported map version!");
		}
		
		LevelObjectStream obj = new LevelObjectStream(data);
		com.mojang.minecraft.level.Level level = null;
		
		try {
			level = (com.mojang.minecraft.level.Level) obj.readObject();
		} catch (ClassNotFoundException e) {
			OpenClassic.getLogger().severe("Failed to load map!");
			OpenClassic.getClient().setCurrentScreen(new ErrorScreen("Failed to load map!", ""));
			e.printStackTrace();
			return null;
		}
		
		if(level.openclassic == null) level.openclassic = new ClientLevel(level);
		
		level.initTransient();
		obj.close();
		
		try {
			f.delete();
		} catch(SecurityException e) {
			e.printStackTrace();
		}
		
		return level.openclassic;
	}
	
	private MinecraftLevelFormat() {
	}

}
