package com.mojang.minecraft;

import ch.spacebase.openclassic.client.util.Server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SessionData {

	public static final List<Server> servers = new ArrayList<Server>();
	public static final List<String> serverInfo = new ArrayList<String>();
	public static final Map<String, String> favorites = new HashMap<String, String>();
	
	public static File favoriteStore;
	
	public String username;
	public String key;
	public boolean haspaid;

	public SessionData(String username) {
		this.username = username;
	}
	
	public static void loadFavorites(File dir) {
		favoriteStore = new File(dir, "favorites.txt");
		
		if(!favoriteStore.exists()) {
			try {
				favoriteStore.createNewFile();
				return;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader(favoriteStore));
			String line = "";
			
			while((line = reader.readLine()) != null) {
				String favorite[] = line.split(", ");
				favorites.put(favorite[0], favorite[1]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void saveFavorites() {
		if(favoriteStore == null) return;
		
		if(!favoriteStore.exists()) {
			try {
				favoriteStore.createNewFile();
				return;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		BufferedWriter writer = null;
		
		try {
			writer = new BufferedWriter(new FileWriter(favoriteStore));
			for(String favorite : favorites.keySet()) {
				writer.write(favorite + ", " + favorites.get(favorite));
				writer.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}
