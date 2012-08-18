package ch.spacebase.openclassic.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.spacebase.openclassic.client.MinecraftStandalone;
import ch.spacebase.openclassic.server.ClassicServer;

public class Main {

	public static void main(String args[]) {
		final List<String> argList = new ArrayList<String>(Arrays.asList(args));
		if(argList.contains("server")) {
			argList.remove("server");
			new Thread("Server") {
				public void run() {
					new ClassicServer().start(argList.toArray(new String[argList.size()]));
				}
			}.start();
		} else {
			MinecraftStandalone.start(args);
		}
	}
	
}
