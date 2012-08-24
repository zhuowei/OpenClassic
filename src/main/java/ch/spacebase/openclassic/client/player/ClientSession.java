package ch.spacebase.openclassic.client.player;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import com.mojang.minecraft.net.PacketType;

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
		if(message.getClass().getPackage().getName().contains("custom") && !GeneralUtils.getMinecraft().openclassicServer) return;
		
		PacketType type = PacketType.packets[message.getOpcode()];
		if(type != null) {
			GeneralUtils.getMinecraft().netManager.netHandler.send(type, message.getParams()); 
			return;
		}
	}

	@Override
	public void disconnect(String reason) {
		if(GeneralUtils.getMinecraft().netManager == null || !GeneralUtils.getMinecraft().netManager.isConnected()) return;
		GeneralUtils.getMinecraft().netManager.netHandler.close();
	}

	@Override
	public SocketAddress getAddress() {
		if(GeneralUtils.getMinecraft().netManager == null || !GeneralUtils.getMinecraft().netManager.isConnected()) return null;
		return InetSocketAddress.createUnresolved(GeneralUtils.getMinecraft().server, GeneralUtils.getMinecraft().port);
	}

}
