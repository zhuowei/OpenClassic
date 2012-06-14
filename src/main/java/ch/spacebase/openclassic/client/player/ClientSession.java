package ch.spacebase.openclassic.client.player;

import java.io.IOException;
import java.net.SocketAddress;

import com.mojang.minecraft.net.PacketType;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.network.msg.Message;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.player.Session;
import ch.spacebase.openclassic.client.util.GeneralUtils;

public class ClientSession implements Session {

	private ClientPlayer player;
	
	public ClientSession(ClientPlayer player) {
		this.player = player;
	}
	
	@Override
	public State getState() {
		return !GeneralUtils.getMinecraft().netManager.identified ? State.IDENTIFYING : (!GeneralUtils.getMinecraft().netManager.levelLoaded ? State.PREPARING : State.GAME);
	}

	@Override
	public Player getPlayer() {
		return this.player;
	}

	@Override
	public void send(Message message) {
		if(GeneralUtils.getMinecraft().netManager == null || !GeneralUtils.getMinecraft().netManager.isConnected()) return;
		
		PacketType type = PacketType.packets[message.getOpcode()];
		if(type != null) {
			GeneralUtils.getMinecraft().netManager.netHandler.send(type, message.getParams()); 
			return;
		}
		
		OpenClassic.getLogger().warning("Unknown Message: " + message.toString());
	}

	@Override
	public void disconnect(String reason) {
		if(GeneralUtils.getMinecraft().netManager == null || !GeneralUtils.getMinecraft().netManager.isConnected()) return;
		GeneralUtils.getMinecraft().netManager.netHandler.close();
	}

	@Override
	public SocketAddress getAddress() {
		if(GeneralUtils.getMinecraft().netManager == null || !GeneralUtils.getMinecraft().netManager.isConnected()) return null;
		
		try {
			return GeneralUtils.getMinecraft().netManager.netHandler.channel.getRemoteAddress();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
