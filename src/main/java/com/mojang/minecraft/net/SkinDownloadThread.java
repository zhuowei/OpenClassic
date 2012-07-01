package com.mojang.minecraft.net;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.client.util.HTTPUtil;

import com.mojang.minecraft.net.NetworkPlayer;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.imageio.ImageIO;

final class SkinDownloadThread extends Thread {

	private NetworkPlayer player;

	public SkinDownloadThread(NetworkPlayer player) {
		this.player = player;
	}

	public final void run() {
		try {
			if(!Boolean.valueOf(HTTPUtil.fetchUrl("http://www.minecraft.net/haspaid.jsp", "user=" + URLEncoder.encode(this.player.name, "UTF-8"))))
				return;
		} catch(UnsupportedEncodingException e) {
		}
		
		HttpURLConnection conn = null;

		try {
			conn = (HttpURLConnection) (new URL("http://www.minecraft.net/skin/" + Color.stripColor(this.player.name) + ".png")).openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(false);
			conn.connect();
			if (conn.getResponseCode() != 404 && conn.getResponseCode() != 403) {
				this.player.newTexture = ImageIO.read(conn.getInputStream());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.disconnect();
		}
	}
}
