package ch.spacebase.openclassic.client.util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.mojang.minecraft.Minecraft;

public class LWJGLNatives {

	public static void load(File dir) {
	    String os = System.getProperty("os.name").toLowerCase();
		if(os.contains("linux")) {
			load(dir.getPath(), "libjinput-linux.so", "86");
			load(dir.getPath(), "libjinput-linux64.so", "64");
			load(dir.getPath(), "liblwjgl.so", "86");
			load(dir.getPath(), "liblwjgl64.so", "64");
			load(dir.getPath(), "libopenal.so", "86");
			load(dir.getPath(), "libopenal64.so", "64");
		} else if(os.contains("solaris")) {
			load(dir.getPath(), "liblwjgl.so", "86");
			load(dir.getPath(), "liblwjgl64.so", "64");
			load(dir.getPath(), "libopenal.so", "86");
			load(dir.getPath(), "libopenal64.so", "64");
		} else if(os.contains("win")) {
			load(dir.getPath(), "OpenAL64.dll", "64");
			load(dir.getPath(), "OpenAL32.dll", "86");
			load(dir.getPath(), "lwjgl64.dll", "64");
			load(dir.getPath(), "lwjgl.dll", "86");
			load(dir.getPath(), "jinput-raw_64.dll", "64");
			load(dir.getPath(), "jinput-raw.dll", "86");
			load(dir.getPath(), "jinput-dx8_64.dll", "64");
			load(dir.getPath(), "jinput-dx8.dll", "86");
		} else if(os.contains("macos")) {
			load(dir.getPath(), "openal.dylib", "both");
			load(dir.getPath(), "liblwjgl.jnilib", "both");
			load(dir.getPath(), "libjinput-osx.jnilib", "both");
		} else {
			throw new RuntimeException("Your system does not support LWJGL.");
		}
		
		System.setProperty("java.library.path", System.getProperty("java.library.path") + ":" + dir.getPath());
		System.setProperty("org.lwjgl.librarypath", dir.getPath());
	}

	private static void load(String dir, String lib, String arch) {
		File file = new File(dir + "/" + lib);
		if(file.exists()) {
			if(System.getProperty("os.arch").contains(arch) || arch.equals("both")) {
				System.load(file.getPath());
			}
			
			return;
		}
		
		try {
			InputStream in = Minecraft.class.getResourceAsStream("/" + lib);
			System.out.println("Writing " + lib + " to " + file.getPath());
			copy(in, file);
			in.close();
			if(System.getProperty("os.arch").contains(arch) || arch.equals("both")) System.load(file.getPath());
		} catch (Exception e) {
			System.out.println("Failed to unpack native " + lib + "!");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private static void copy(InputStream in, File to) throws IOException {
		if(!to.exists()) {
			if(!to.getParentFile().exists()) {
				try {
					to.getParentFile().mkdirs();
				} catch(SecurityException e) {
					e.printStackTrace();
				}
			}
			
			try {
				to.createNewFile();
			} catch(SecurityException e) {
				e.printStackTrace();
			}
		}
		
		DataOutputStream out = new DataOutputStream(new FileOutputStream(to));
		
		int next;
		while((next = in.read()) != -1) {
			out.write(next);
		}
		
		out.close();
	}

}
