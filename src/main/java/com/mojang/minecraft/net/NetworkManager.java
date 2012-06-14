package com.mojang.minecraft.net;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.gui.ErrorScreen;
import com.mojang.minecraft.net.NetworkPlayer;
import com.mojang.minecraft.net.PacketType;
import com.mojang.minecraft.net.ServerConnectThread;
import com.mojang.net.NetworkHandler;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class NetworkManager {

	public ByteArrayOutputStream levelData;
	public NetworkHandler netHandler;
	public Minecraft mc;
	public boolean successful = false;
	public boolean levelLoaded = false;
	public boolean identified = false;
	public HashMap<Byte, NetworkPlayer> players = new HashMap<Byte, NetworkPlayer>();

	public NetworkManager(Minecraft mc, String server, int port, String username, String key) {
		this.mc = mc;
		this.mc.online = true;
		
		(new ServerConnectThread(this, server, port, username, key, mc)).start();
	}

	public final void sendBlockChange(int x, int y, int z, int mode, int block) {
		this.netHandler.send(PacketType.CLIENT_SET_BLOCK, new Object[] { (short) x, (short) y, (short) z, (byte) mode, (byte) block });
	}

	public final void error(Exception e) {
		this.netHandler.close();
		this.mc.setCurrentScreen(new ErrorScreen("Disconnected!", e.getMessage()));
		e.printStackTrace();
	}

	public final boolean isConnected() {
		return this.netHandler != null && this.netHandler.connected;
	}

	public final List<String> getPlayers() {
		ArrayList<String> players = new ArrayList<String>();
		players.add(this.mc.data.username);

		for(NetworkPlayer player : this.players.values()) {
			players.add(player.name);
		}

		return players;
	}
}
