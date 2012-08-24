package com.mojang.minecraft.level;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.data.NBTData;
import ch.spacebase.openclassic.api.event.EventFactory;
import ch.spacebase.openclassic.api.event.level.LevelLoadEvent;
import ch.spacebase.openclassic.api.event.level.LevelSaveEvent;
import ch.spacebase.openclassic.client.io.OpenClassicLevelFormat;
import ch.spacebase.openclassic.client.level.ClientLevel;

import com.mojang.minecraft.ProgressBarDisplay;
import com.mojang.minecraft.level.Level;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class LevelIO {

	private ProgressBarDisplay progress;

	public LevelIO(ProgressBarDisplay progress) {
		this.progress = progress;
	}

	public final boolean save(Level level) {	
		if(EventFactory.callEvent(new LevelSaveEvent(level.openclassic)).isCancelled()) {
			return true;
		}
		
		try {
			OpenClassicLevelFormat.save(level.openclassic);
			if(level.openclassic.getData() != null) level.openclassic.getData().save(OpenClassic.getGame().getDirectory().getPath() + "/levels/" + level.name + ".nbt");
			return true;
		} catch (IOException e) {
			if (this.progress != null) {
				this.progress.setText(String.format(OpenClassic.getGame().getTranslator().translate("level.save-fail"), level.name));
			}
			
			e.printStackTrace();
			
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e1) {
			}
			
			return false;
		}
	}

	public final Level load(String name) {		
		if (this.progress != null) {
			this.progress.setTitle(OpenClassic.getGame().getTranslator().translate("level.loading"));
			this.progress.setText(OpenClassic.getGame().getTranslator().translate("level.reading"));
		}
		
		try {
			Level level = ((ClientLevel) OpenClassicLevelFormat.load(name, false)).getHandle();
			level.openclassic.data = new NBTData(level.name);
			level.openclassic.data.load(OpenClassic.getGame().getDirectory().getPath() + "/levels/" + level.name + ".nbt");
			EventFactory.callEvent(new LevelLoadEvent(level.openclassic));
			return level;
		} catch (IOException e) {
			if (this.progress != null) {
				this.progress.setText(String.format(OpenClassic.getGame().getTranslator().translate("level.load-fail"), name));
			}
			
			e.printStackTrace();
			
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e1) {
			}
			
			return null;
		}
	}
	
	public static void saveOld(Level level) {
		if(EventFactory.callEvent(new LevelSaveEvent(level.openclassic)).isCancelled()) {
			return;
		}
		
		try {
			DataOutputStream data = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(new File(OpenClassic.getGame().getDirectory(), "levels/" + level.name + ".mine"))));
			data.writeInt(656127880);
			data.writeByte(2);
			ObjectOutputStream obj = new ObjectOutputStream(data);
			obj.writeObject(level);
			obj.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static byte[] processData(InputStream in) {
		try {
			DataInputStream dataIn = new DataInputStream(new GZIPInputStream(in));
			byte[] data = new byte[dataIn.readInt()];
			dataIn.readFully(data);
			dataIn.close();
			return data;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
