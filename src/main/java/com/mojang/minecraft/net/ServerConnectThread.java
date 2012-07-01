package com.mojang.minecraft.net;

import java.net.InetSocketAddress;

import ch.spacebase.openclassic.api.event.EventFactory;
import ch.spacebase.openclassic.api.event.player.PlayerConnectEvent;
import ch.spacebase.openclassic.api.event.player.PlayerConnectEvent.Result;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.gui.ErrorScreen;
import com.mojang.minecraft.net.NetworkManager;
import com.mojang.minecraft.net.PacketType;
import com.mojang.net.NetworkHandler;

public final class ServerConnectThread extends Thread {

	private String server;
	private int port;
	private String username;
	private String key;
	private Minecraft mc;
	private NetworkManager netManager;

	public ServerConnectThread(NetworkManager netManager, String server, int port, String username, String key, Minecraft mc) {
		this.netManager = netManager;
		this.server = server;
		this.port = port;
		this.username = username;
		this.key = key;
		this.mc = mc;
	}

	public final void run() {
		try {
			this.netManager.netHandler = new NetworkHandler(this.server, this.port, this.netManager.mc);
			this.netManager.netHandler.netManager = this.netManager;
			PlayerConnectEvent event = EventFactory.callEvent(new PlayerConnectEvent(this.mc.data.username, InetSocketAddress.createUnresolved(this.server, this.port)));
			if(event.getResult() != Result.ALLOWED) {
				this.mc.online = false;
				this.mc.netManager = null;
				this.mc.setCurrentScreen(new ErrorScreen("Connect disallowed by plugin!", event.getKickMessage()));
				this.netManager.successful = false;
				return;
			}
			
			this.netManager.netHandler.send(PacketType.IDENTIFICATION, new Object[] { (byte) 7, this.username, this.key, (byte) 0 });
			this.netManager.successful = true;
		} catch (Exception e) {
			this.mc.online = false;
			this.mc.netManager = null;
			this.mc.setCurrentScreen(new ErrorScreen("Failed to connect", "You failed to connect to the server. It\'s probably down!"));
			e.printStackTrace();
			this.netManager.successful = false;
		}
	}
}
