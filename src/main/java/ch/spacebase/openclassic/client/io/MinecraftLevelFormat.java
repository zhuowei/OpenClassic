package ch.spacebase.openclassic.client.io;

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
			OpenClassic.getLogger().severe(OpenClassic.getGame().getTranslator().translate("level.unsupported-format"));
		}
		
		if(version != 2) {
			OpenClassic.getLogger().severe(OpenClassic.getGame().getTranslator().translate("level.unsupported-version"));
		}
		
		LevelObjectStream obj = new LevelObjectStream(data);
		com.mojang.minecraft.level.Level level = null;
		
		try {
			level = (com.mojang.minecraft.level.Level) obj.readObject();
		} catch (ClassNotFoundException e) {
			OpenClassic.getLogger().severe(OpenClassic.getGame().getTranslator().translate("level.load-fail"));
			OpenClassic.getClient().setCurrentScreen(new ErrorScreen(OpenClassic.getGame().getTranslator().translate("level.load-fail"), ""));
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
