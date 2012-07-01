package com.mojang.minecraft;

import com.mojang.minecraft.SessionData;
import com.mojang.minecraft.Minecraft;
import java.applet.Applet;
import java.awt.BorderLayout;

public class MinecraftApplet extends Applet {
	
	private static final long serialVersionUID = 1L;
	private MinecraftCanvas canvas;
	private Minecraft minecraft;
	
	public void init() {
		this.canvas = new MinecraftCanvas();
		this.minecraft = new Minecraft(this.canvas, this.getWidth(), this.getHeight());
		this.canvas.setMinecraft(this.minecraft);
		
		if (this.getParameter("username") != null && this.getParameter("sessionid") != null) {
			this.minecraft.data = new SessionData(this.getParameter("username"), this.getParameter("sessionid"));
			if (this.getParameter("mppass") != null && !this.getParameter("mppass").equals("")) {
				this.minecraft.data.key = this.getParameter("mppass");
			}

			this.minecraft.data.haspaid = "true".equals(this.getParameter("haspaid"));
		}

		if (this.getParameter("server") != null && this.getParameter("port") != null) {
			String server = this.getParameter("server");
			int port = 25565;
			if(this.getParameter("port") != null && !this.getParameter("port").equals("")) {
				port = Integer.parseInt(this.getParameter("port"));
			}
			
			this.minecraft.server = server;
			this.minecraft.port = port;
		}

		this.setLayout(new BorderLayout());
		this.add(this.canvas, "Center");
		this.canvas.setFocusable(true);
		this.validate();
	}

	public void start() {
		this.minecraft.stopping = false;
	}

	public void stop() {
		this.minecraft.stopping = true;
	}

	public void destroy() {
		this.canvas.stopThread();
	}
	
	public Minecraft getMinecraft() {
		return this.minecraft;
	}
	
}
