package ch.spacebase.openclassic.client.util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.Minecraft.OS;

public class LWJGLNatives {

	public static void load(OS os, File dir) {
		switch (os) {
		case linux:
			load(dir.getPath(), "libjinput-linux.so");
			load(dir.getPath(), "libjinput-linux64.so");
			load(dir.getPath(), "liblwjgl.so");
			load(dir.getPath(), "liblwjgl64.so");
			load(dir.getPath(), "libopenal.so");
			load(dir.getPath(), "libopenal64.so");
			break;
		case solaris:
			load(dir.getPath(), "liblwjgl.so");
			load(dir.getPath(), "liblwjgl64.so");
			load(dir.getPath(), "libopenal.so");
			load(dir.getPath(), "libopenal64.so");
			break;
		case windows:
			load(dir.getPath(), "OpenAL64.dll");
			load(dir.getPath(), "OpenAL32.dll");
			load(dir.getPath(), "lwjgl64.dll");
			load(dir.getPath(), "lwjgl.dll");
			load(dir.getPath(), "jinput-raw_64.dll");
			load(dir.getPath(), "jinput-raw.dll");
			load(dir.getPath(), "jinput-dx8_64.dll");
			load(dir.getPath(), "jinput-dx8.dll");
			break;
		case macos:
			load(dir.getPath(), "openal.dylib");
			load(dir.getPath(), "liblwjgl.jnilib");
			load(dir.getPath(), "libjinput-osx.jnilib");
			break;
		}
		
		System.setProperty("java.library.path", System.getProperty("java.library.path") + ":" + dir.getPath());
		System.setProperty("org.lwjgl.librarypath", dir.getPath());
	}

	private static void load(String dir, String lib) {
		File file = new File(dir + "/" + lib);
		if(file.exists()) {
			if(!(System.getProperty("os.arch").equals("i386") && lib.contains("64"))) System.load(file.getPath());
			return;
		}
		
		try {
			InputStream in = Minecraft.class.getResourceAsStream("/" + lib);
			System.out.println("Writing " + lib + " to " + file.getPath());
			copy(in, file);
			in.close();
			if(!(System.getProperty("os.arch").equals("i386") && lib.contains("64"))) System.load(file.getPath());
		} catch (Exception e) {
			System.out.println("Failed to unpack native " + lib + "!");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private static void copy(InputStream in, File to) throws IOException {
		if(!to.exists()) {
			if(!to.getParentFile().exists()) {
				to.getParentFile().mkdirs();
			}
			
			to.createNewFile();
		}
		
		DataOutputStream out = new DataOutputStream(new FileOutputStream(to));
		
		byte next;
		while((next = (byte) in.read()) != -1) {
			out.write(next);
		}
		
		out.close();
	}

}
