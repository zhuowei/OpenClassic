package com.mojang.minecraft;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.player.Player;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.imageio.ImageIO;

public final class SkinDownloadThread extends Thread {

	private Minecraft mc;

	public SkinDownloadThread(Minecraft mc) {
		this.mc = mc;
	}

	public final void run() {
		if (this.mc.data != null) {
			HttpURLConnection conn = null;

			try {
				String name = this.mc.data.username;
				if (name.contains("@")) {
					name = this.mc.data.username.substring(0, this.mc.data.username.indexOf('@'));
				}

				conn = (HttpURLConnection) new URL("http://www.minecraft.net/skin/" + name + ".png").openConnection();
				conn.setDoInput(true);
				conn.setDoOutput(false);
				conn.connect();

				if (conn.getResponseCode() == 200) {
					Player.newTexture = ImageIO.read(conn.getInputStream());
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				conn.disconnect();
			}
		}
	}
}