package com.mojang.minecraft;

import com.mojang.minecraft.SessionData;
import com.mojang.minecraft.Minecraft;
import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MinecraftApplet extends Applet {
	
	private static final long serialVersionUID = 1L;
	private Canvas canvas;
	private Minecraft minecraft;
	private Thread thread = null;
	
	private Map<String, String> parameters = new HashMap<String, String>();
	private URL base;
	private URL codeBase;

	public void init() {
		this.init(false);
	}
	
	public void init(boolean standalone) {
		if(!standalone) {
			this.initAppletData();
		} else {
			try {
				this.base = new URL("http://www.minecraft.net/");
				this.codeBase = new URL("http://www.minecraft.net/");
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		
		this.canvas = new MinecraftCanvas();
		
		boolean fullscreen = false;
		if (this.getParameter("fullscreen") != null) {
			fullscreen = this.getParameter("fullscreen").equalsIgnoreCase("true");
		}

		this.minecraft = new Minecraft(this.canvas, this, this.getWidth(), this.getHeight(), fullscreen);
		
		this.minecraft.host = this.getDocumentBase().getHost();
		if (this.getDocumentBase().getPort() > 0) {
			this.minecraft.host = this.minecraft.host + ":" + this.getDocumentBase().getPort();
		}

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

		this.minecraft.noLevel = true;
		this.setLayout(new BorderLayout());
		this.add(this.canvas, "Center");
		this.canvas.setFocusable(true);
		this.validate();
	}
	
	public String getParameter(String name) {
		return this.parameters.get(name);
	}
	
	public void addParameter(String name, String value) {
		this.parameters.put(name, value);
	}
	
	public URL getDocumentBase() {
		return this.base;
	}
	
	public URL getCodeBase() {
		return this.codeBase;
	}
	
	public void initAppletData() {
		if(super.getParameter("fullscreen") != null && this.getParameter("fullscreen") == null) {
			this.parameters.put("fullscreen", super.getParameter("fullscreen"));
		}
		
		if(super.getParameter("username") != null && this.getParameter("username") == null) {
			this.parameters.put("username", super.getParameter("username"));
		}
		
		if(super.getParameter("sessionid") != null && this.getParameter("sessionid") == null) {
			this.parameters.put("sessionid", super.getParameter("sessionid"));
		}
		
		if(super.getParameter("mppass") != null && this.getParameter("mppass") == null) {
			this.parameters.put("mppass", super.getParameter("mppass"));
		}
		
		if(super.getParameter("haspaid") != null && this.getParameter("haspaid") == null) {
			this.parameters.put("haspaid", super.getParameter("haspaid"));
		}
		
		if(super.getParameter("server") != null && this.getParameter("server") == null) {
			this.parameters.put("server", super.getParameter("server"));
		}
		
		if(super.getParameter("port") != null && this.getParameter("port") == null) {
			this.parameters.put("port", super.getParameter("port"));
		}
		
		this.base = super.getDocumentBase();
		this.codeBase = super.getCodeBase();
	}

	public void startGameThread() {
		if (this.thread == null) {
			this.thread = new Thread(this.minecraft);
			this.thread.start();
		}
	}

	public void start() {
		this.minecraft.stopping = false;
	}

	public void stop() {
		this.minecraft.stopping = true;
	}

	public void destroy() {
		this.stopGameThread();
	}

	public void stopGameThread() {
		if (this.thread != null) {
			this.minecraft.running = false;

			try {
				this.thread.join(1000L);
			} catch (InterruptedException var3) {
				try {
					this.minecraft.shutdown();
				} catch (Exception var2) {
					var2.printStackTrace();
				}
			}

			this.thread = null;
		}
	}
	
	public Minecraft getMinecraft() {
		return this.minecraft;
	}

	public final class MinecraftCanvas extends Canvas {
		private static final long serialVersionUID = 1L;

		public final synchronized void addNotify() {
			super.addNotify();
			MinecraftApplet.this.startGameThread();
		}

		public final synchronized void removeNotify() {
			MinecraftApplet.this.stopGameThread();
			super.removeNotify();
		}
	}
}
