package ch.spacebase.openclassic.client.gui;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.HeartbeatManager;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ButtonList;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.util.GeneralUtils;
import ch.spacebase.openclassic.client.util.HTTPUtil;
import ch.spacebase.openclassic.client.util.Server;
import ch.spacebase.openclassic.server.ClassicServer;
import ch.spacebase.openclassic.server.ui.SettingsFrame;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.SessionData;

public class ServerListScreen extends GuiScreen {

	private GuiScreen parent;
	private String title = "Select a server.";

	private boolean select = false;

	public ServerListScreen(GuiScreen parent) {
		this.parent = parent;
	}

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(new ButtonList(0, this.getWidth(), this.getHeight(), this));
		this.getWidget(0, ButtonList.class).setContents(SessionData.serverInfo);

		this.attachWidget(new Button(1, this.getWidth() / 2 - 206, this.getHeight() / 6 + 144, 100, 20, this, "Favorites"));
		this.attachWidget(new Button(2, this.getWidth() / 2 - 102, this.getHeight() / 6 + 144, 100, 20, this, "Add Favorite"));
		this.attachWidget(new Button(3, this.getWidth() / 2 + 2, this.getHeight() / 6 + 144, 100, 20, this, "Enter a URL..."));
		this.attachWidget(new Button(4, this.getWidth() / 2 + 106, this.getHeight() / 6 + 144, 100, 20, this, "Back to Menu"));
		this.attachWidget(new Button(5, this.getWidth() / 2 - 206, this.getHeight() / 6 + 168, 100, 20, this, OpenClassic.getServer() != null && OpenClassic.getServer().isRunning() ? "Stop Server" : "Start Server"));
		this.attachWidget(new Button(6, this.getWidth() / 2 - 102, this.getHeight() / 6 + 168, 100, 20, this, "Server Settings"));
		this.attachWidget(new Button(7, this.getWidth() / 2 + 2, this.getHeight() / 6 + 168, 100, 20, this, "Server Console"));
		this.attachWidget(new Button(8, this.getWidth() / 2 + 106, this.getHeight() / 6 + 168, 100, 20, this, OpenClassic.getServer() != null && OpenClassic.getServer().isRunning() && HeartbeatManager.getURL().equals("") ? "Awaiting URL..." : "Connect"));
		if(OpenClassic.getServer() == null || !OpenClassic.getServer().isRunning()) {
			this.getWidget(6, Button.class).setActive(false);
			this.getWidget(7, Button.class).setActive(false);
			this.getWidget(8, Button.class).setActive(false);
		}
		
		if(HeartbeatManager.getURL().equals("")) {
			this.getWidget(8, Button.class).setActive(false);
		}
	}

	public final void onButtonClick(Button button) {
		if (button.getId() == 1) {
			OpenClassic.getClient().setCurrentScreen(new FavoriteServersScreen(this));
		}

		if (button.getId() == 2) {
			if (this.select) {
				this.title = "Select a server.";
				this.select = false;
			} else {
				this.title = Color.GREEN + "Select a server to favorite.";
				this.select = true;
			}
		}

		if (button.getId() == 3) {
			OpenClassic.getClient().setCurrentScreen(new ServerURLScreen(this));
		}
		
		if (button.getId() == 4) {
			OpenClassic.getClient().setCurrentScreen(this.parent);
		}
		
		if (button.getId() == 5) {
			if(OpenClassic.getServer() != null) {
				OpenClassic.getServer().shutdown();
				this.getWidget(5, Button.class).setText("Start Server");
				this.getWidget(6, Button.class).setActive(false);
				this.getWidget(7, Button.class).setActive(false);
				this.getWidget(8, Button.class).setActive(false);
			} else {
				Thread thr = new Thread("Server") {
					public void run() {
						new ClassicServer(new File(OpenClassic.getClient().getDirectory(), "server")).start(new String[] { "embedded" });
					}
				};
				
				thr.setDaemon(true);
				thr.start();
				
				this.getWidget(5, Button.class).setText("Stop Server");
				this.getWidget(6, Button.class).setActive(true);
				this.getWidget(7, Button.class).setActive(true);
				this.getWidget(8, Button.class).setActive(false);
				this.getWidget(8, Button.class).setText("Awaiting URL...");
			}
		}
		
		if (button.getId() == 6) {
			SettingsFrame frame = new SettingsFrame();
			
			frame.serverName.setText(OpenClassic.getServer().getConfig().getString("info.name"));
			frame.motd.setText(OpenClassic.getServer().getConfig().getString("info.motd"));
			frame.port.setText(String.valueOf(OpenClassic.getServer().getConfig().getInteger("options.port")));
			frame.chckbxShowOnServer.setSelected(OpenClassic.getServer().getConfig().getBoolean("options.public"));
			frame.maxPlayers.setText(String.valueOf(OpenClassic.getServer().getConfig().getInteger("options.max-players")));
			frame.chckbxVerifyUsers.setSelected(OpenClassic.getServer().getConfig().getBoolean("options.online-mode"));
			frame.chckbxUseWhitelist.setSelected(OpenClassic.getServer().getConfig().getBoolean("options.whitelist"));
			frame.chckbxAllowFlying.setSelected(OpenClassic.getServer().getConfig().getBoolean("options.allow-flight"));
			frame.defaultLevel.setText(OpenClassic.getServer().getConfig().getString("options.default-level"));
			frame.chckbxEnabled.setSelected(OpenClassic.getServer().getConfig().getBoolean("physics.enabled"));
			frame.chckbxFalling.setSelected(OpenClassic.getServer().getConfig().getBoolean("physics.falling"));
			frame.chckbxFlower.setSelected(OpenClassic.getServer().getConfig().getBoolean("physics.flower"));
			frame.chckbxMushroom.setSelected(OpenClassic.getServer().getConfig().getBoolean("physics.mushroom"));
			frame.chckbxTree.setSelected(OpenClassic.getServer().getConfig().getBoolean("physics.trees"));
			frame.chckbxSponge.setSelected(OpenClassic.getServer().getConfig().getBoolean("physics.sponge"));
			frame.chckbxLiquid.setSelected(OpenClassic.getServer().getConfig().getBoolean("physics.liquid"));
			frame.chckbxGrass.setSelected(OpenClassic.getServer().getConfig().getBoolean("physics.grass"));
			frame.changePhysics();
			
			GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			frame.setLocation((gd.getDisplayMode().getWidth() - frame.getWidth()) / 2, (gd.getDisplayMode().getHeight() - frame.getHeight()) / 2);
			frame.setVisible(true);
		}
		
		if (button.getId() == 7) {
			ConsoleScreen.get().setParent(this);
			OpenClassic.getClient().setCurrentScreen(ConsoleScreen.get());
		}
		
		if(button.getId() == 8) {
			Minecraft mc = GeneralUtils.getMinecraft();
			String page = HTTPUtil.fetchUrl(HeartbeatManager.getURL(), "", "http://www.minecraft.net/classic/list/");
			mc.data = new SessionData(HTTPUtil.getParameterOffPage(page, "username"));
			mc.data.key = HTTPUtil.getParameterOffPage(page, "mppass");
			mc.data.haspaid = Boolean.valueOf(HTTPUtil.getParameterOffPage(page, "haspaid"));
			mc.server = HTTPUtil.getParameterOffPage(page, "server");
			mc.port = Integer.parseInt(HTTPUtil.getParameterOffPage(page, "port"));

			mc.initGame();
			mc.setCurrentScreen(null);
		}
	}
	
	public void update() {
		if(!HeartbeatManager.getURL().equals("") && !this.getWidget(8, Button.class).isActive()) {
			this.getWidget(8, Button.class).setActive(true);
			this.getWidget(8, Button.class).setText("Connect");
		}
	}

	@Override
	public void onButtonListClick(ButtonList list, Button button) {
		Server server = SessionData.servers.get(list.getCurrentPage() * 5 + button.getId());
		
		if (this.select) {
			this.title = "Select a server.";
			this.select = false;
			
			SessionData.favorites.put(server.name, server.getUrl());
		} else {
			this.joinServer(server);
		}
	}

	private void joinServer(Server server) {
		if(server != null) {
			Minecraft mc = GeneralUtils.getMinecraft();
			
			OpenClassic.getClient().getProgressBar().setTitle("Connecting...");
			OpenClassic.getClient().getProgressBar().setText("Getting server info...");
			OpenClassic.getClient().getProgressBar().setProgress(0);

			String page = HTTPUtil.fetchUrl(server.getUrl(), "", "http://www.minecraft.net/classic/list/");
			mc.data = new SessionData(HTTPUtil.getParameterOffPage(page, "username"));
			mc.data.key = HTTPUtil.getParameterOffPage(page, "mppass");
			mc.data.haspaid = Boolean.valueOf(HTTPUtil.getParameterOffPage(page, "haspaid"));
			mc.server = HTTPUtil.getParameterOffPage(page, "server");
			mc.port = Integer.parseInt(HTTPUtil.getParameterOffPage(page, "port"));

			mc.initGame();
			mc.setCurrentScreen(null);
		}
	}

	public void render() {
		RenderHelper.getHelper().drawDirtBG();
		RenderHelper.getHelper().renderText(this.title, this.getWidth() / 2, 15);

		super.render();
	}
}
