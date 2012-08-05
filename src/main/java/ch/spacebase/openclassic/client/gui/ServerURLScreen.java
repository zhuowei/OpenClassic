package ch.spacebase.openclassic.client.gui;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.TextBox;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.util.GeneralUtils;
import ch.spacebase.openclassic.client.util.HTTPUtil;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.SessionData;
import com.mojang.minecraft.gui.ErrorScreen;

import org.lwjgl.input.Keyboard;

/**
 * @author Steveice10 <Steveice10@gmail.com>
 */
public class ServerURLScreen extends GuiScreen {

	private GuiScreen parent;

	public ServerURLScreen(GuiScreen parent) {
		this.parent = parent;
	}

	public void onOpen() {
		Keyboard.enableRepeatEvents(true);
		
		this.clearWidgets();
		this.attachWidget(new Button(0, this.getWidth() / 2 - 100, this.getHeight() / 4 + 120, this, "Connect"));
		this.attachWidget(new Button(1, this.getWidth() / 2 - 100, this.getHeight() / 4 + 144, this, "Cancel"));
		this.attachWidget(new TextBox(2, this.getWidth() / 2 - 100, this.getHeight() / 2 - 10, this));
		this.getWidget(2, TextBox.class).setFocus(true);
		
		this.getWidget(0, Button.class).setActive(false);
	}

	public void onClose() {
		Keyboard.enableRepeatEvents(false);
	}

	public void onButtonClick(Button button) {
		if(button.getId() == 0) {
			Minecraft mc = GeneralUtils.getMinecraft();
			
			mc.progressBar.setTitle("Connecting...");
			mc.progressBar.setText("Getting server info...");
			mc.progressBar.setProgress(0);
			String play = HTTPUtil.fetchUrl(this.getWidget(2, TextBox.class).getText(), "", "http://www.minecraft.net/classic/list");
			String mppass = HTTPUtil.getParameterOffPage(play, "mppass");
			
			if (mppass.length() > 0) {
				String user = HTTPUtil.getParameterOffPage(play, "username");
				System.out.println("Got user details: user=" + user);
					
				mc.data = new SessionData(user);
				mc.data.key = mppass;
				
				try {
					mc.data.haspaid = Boolean.valueOf(HTTPUtil.fetchUrl("http://www.minecraft.net/haspaid.jsp", "user=" + URLEncoder.encode(user, "UTF-8")));
				} catch(UnsupportedEncodingException e) {
				}
				
				mc.server = HTTPUtil.getParameterOffPage(play, "server");
				mc.port = Integer.parseInt(HTTPUtil.getParameterOffPage(play, "port"));
			} else {
				OpenClassic.getClient().setCurrentScreen(new ErrorScreen("Failed to connect!", "Make sure the URL is correct and from the minecraft.net server list!"));
				return;
			}
			
			mc.initGame();
			OpenClassic.getClient().setCurrentScreen(null);
		}
		
		if(button.getId() == 1) {
			OpenClassic.getClient().setCurrentScreen(this.parent);
		}
	}

	public void onKeyPress(char c, int key) {
		super.onKeyPress(c, key);
		this.getWidget(0, Button.class).setActive(this.getWidget(2, TextBox.class).getText().length() > 0);
	}

	public void render() {
		RenderHelper.getHelper().drawDirtBG();
		
		RenderHelper.getHelper().renderText("Enter the server's URL.", this.getWidth() / 2, 40);
		super.render();
	}
}
