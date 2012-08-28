package ch.spacebase.openclassic.client.gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.URLEncoder;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.PasswordTextBox;
import ch.spacebase.openclassic.api.gui.widget.StateButton;
import ch.spacebase.openclassic.api.gui.widget.TextBox;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.cookie.Cookie;
import ch.spacebase.openclassic.client.cookie.CookieList;
import ch.spacebase.openclassic.client.util.GeneralUtils;
import ch.spacebase.openclassic.client.util.HTTPUtil;
import ch.spacebase.openclassic.client.util.Server;

import org.lwjgl.input.Keyboard;

import com.mojang.minecraft.SessionData;

/**
 * @author Steveice10 <Steveice10@gmail.com>
 */
public class LoginScreen extends GuiScreen {

	private String title = OpenClassic.getGame().getTranslator().translate("gui.login.enter");
	
	public void onOpen() {
		Keyboard.enableRepeatEvents(true);
		
		this.clearWidgets();
		this.attachWidget(new Button(0, this.getWidth() / 2 - 100, this.getHeight() / 4 + 120, 98, 20, this, OpenClassic.getGame().getTranslator().translate("gui.login.login")));
		this.attachWidget(new Button(4, this.getWidth() / 2 + 2, this.getHeight() / 4 + 120, 98, 20, this, OpenClassic.getGame().getTranslator().translate("gui.login.play-offline")));
		this.attachWidget(new StateButton(1, this.getWidth() / 2 - 100, this.getHeight() / 4 + 144, this, OpenClassic.getGame().getTranslator().translate("gui.login.remember")));
		this.attachWidget(new TextBox(2, this.getWidth() / 2 - 100, this.getHeight() / 2 - 10, this, 64));
		this.attachWidget(new PasswordTextBox(3, this.getWidth() / 2 - 100, this.getHeight() / 2 + 16, this, 64));
		
		this.getWidget(1, StateButton.class).setState(this.getLoginFile(false).exists() ? OpenClassic.getGame().getTranslator().translate("gui.yes") : OpenClassic.getGame().getTranslator().translate("gui.no"));
		this.getWidget(2, TextBox.class).setFocus(true);
		
		if(this.getLoginFile(false).exists()) {
			BufferedReader reader = null;
			
			try {
				reader = new BufferedReader(new FileReader(this.getLoginFile(true)));
				String line = "";
				while((line = reader.readLine()) != null) {
					if(this.getWidget(2, TextBox.class).getText().equals("")) {
						this.getWidget(2, TextBox.class).setText(line);
					} else if(this.getWidget(3, TextBox.class).getText().equals("")) {
						this.getWidget(3, TextBox.class).setText(line);
						break;
					}
				}
			} catch(IOException e) {
				System.out.println(OpenClassic.getGame().getTranslator().translate("gui.login.fail-check"));
				e.printStackTrace();
			} finally {
				if(reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			this.getWidget(0, Button.class).setActive(false);
		}
	}

	public void onClose() {
		Keyboard.enableRepeatEvents(false);
	}

	public void onButtonClick(Button button) {
		if(button.getId() == 0) {
			String user = this.getWidget(2, TextBox.class).getText();
			String pass = this.getWidget(3, TextBox.class).getText();
			
			if(this.getWidget(1, StateButton.class).getState().equals(OpenClassic.getGame().getTranslator().translate("gui.yes"))) {
				BufferedWriter writer = null;
				
				try {
					writer = new BufferedWriter(new FileWriter(this.getLoginFile(true)));
					writer.write(user);
					writer.newLine();
					writer.write(pass);
				} catch (IOException e1) {
					System.out.println(OpenClassic.getGame().getTranslator().translate("gui.login.fail-create"));
					e1.printStackTrace();
				} finally {
					if(writer != null) {
						try {
							writer.close();
						} catch(IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			} else if(this.getLoginFile(false).exists()) {
				this.getLoginFile(false).delete();
			}
			
			OpenClassic.getClient().getProgressBar().setTitle(OpenClassic.getGame().getTranslator().translate("gui.login.logging-in"));
			OpenClassic.getClient().getProgressBar().setProgress(0);
			if (!auth(user, pass)) {
				this.title = Color.RED + OpenClassic.getGame().getTranslator().translate("gui.login.failed");
				return;
			}
			
			GeneralUtils.getMinecraft().setCurrentScreen(new MainMenuScreen());
		}
		
		if(button.getId() == 1) {
			this.getWidget(1, StateButton.class).setState(this.getWidget(1, StateButton.class).getState() == OpenClassic.getGame().getTranslator().translate("gui.yes") ? OpenClassic.getGame().getTranslator().translate("gui.no") : OpenClassic.getGame().getTranslator().translate("gui.yes"));
		}
		
		if(button.getId() == 4) {
			GeneralUtils.getMinecraft().setCurrentScreen(new MainMenuScreen());
		}
	}

	public void onKeyPress(char c, int key) {
		super.onKeyPress(c, key);
		this.getWidget(0, Button.class).setActive(this.getWidget(2, TextBox.class).getText().length() > 0 && this.getWidget(3, TextBox.class).getText().length() > 0);
	}

	public void render() {
		RenderHelper.getHelper().drawDirtBG();
		
		RenderHelper.getHelper().renderText(this.title, this.getWidth() / 2, 40);
		RenderHelper.getHelper().renderText(OpenClassic.getGame().getTranslator().translate("gui.login.user"), (this.getWidth() / 2 - 104) - RenderHelper.getHelper().getStringWidth("Username"), this.getHeight() / 2 - 6);
		RenderHelper.getHelper().renderText(OpenClassic.getGame().getTranslator().translate("gui.login.pass"), (this.getWidth() / 2 - 104) - RenderHelper.getHelper().getStringWidth("Password"), this.getHeight() / 2 + 20);
		super.render();
	}
	
	private File getLoginFile(boolean create) {
		File file = new File(OpenClassic.getClient().getDirectory(), ".login");
		if(!file.exists() && create) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				System.out.println(OpenClassic.getGame().getTranslator().translate("gui.login.fail-create"));
				e.printStackTrace();
			}
		}
		
		return file;
	}
	
	public static boolean auth(String username, String password) {
		CookieList cookies = new CookieList();
		CookieHandler.setDefault(cookies);
		String result = "";

		HTTPUtil.fetchUrl("https://www.minecraft.net/login", "", "https://www.minecraft.net");

		try {
			result = HTTPUtil.fetchUrl("https://www.minecraft.net/login", "username=" + URLEncoder.encode(username, "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8"), "http://www.minecraft.net");
		} catch (UnsupportedEncodingException e) {
			System.out.println("UTF-8 not supported!");
			return false;
		}

		Cookie cookie = cookies.getCookie("https://www.minecraft.net", "PLAY_SESSION");
		if (cookie != null)
			result = HTTPUtil.fetchUrl("http://www.minecraft.net", "", "https://www.minecraft.net/login");

		if (result.contains("Logged in as")) {
			GeneralUtils.getMinecraft().data = new SessionData(result.substring(result.indexOf("Logged in as ") + 13, result.indexOf(" | ")));
			
			try {
				GeneralUtils.getMinecraft().data.haspaid = HTTPUtil.fetchUrl("http://www.minecraft.net/haspaid.jsp", "user=" + URLEncoder.encode(GeneralUtils.getMinecraft().data.username, "UTF-8")).equals("true");
			} catch (UnsupportedEncodingException e) {
			}

			parseServers(HTTPUtil.rawFetchUrl("http://www.minecraft.net/classic/list", "", "http://www.minecraft.net"));
			return true;
		}

		return false;
	}

	private static void parseServers(String data) {
		int index = data.indexOf("<a href=\"");
		while (index != -1) {
			index = data.indexOf("classic/play/", index);
			if (index == -1) {
				break;
			}
			
			String id = data.substring(index + 13, data.indexOf("\"", index));
			index = data.indexOf(">", index) + 1;
			String name = data.substring(index, data.indexOf("</a>", index)).replaceAll("&amp;", "&").replaceAll("&hellip;", "...");
			index = data.indexOf("<td>", index) + 4;
			String users = data.substring(index, data.indexOf("</td>", index));
			index = data.indexOf("<td>", index) + 4;
			String max = data.substring(index, data.indexOf("</td>", index));

			Server s = new Server(name, Integer.valueOf(users).intValue(), Integer.valueOf(max).intValue(), id);
			SessionData.servers.add(s);
			SessionData.serverInfo.add(s.name);
		}
	}

	public static String toHex(byte[] data) {
		StringBuffer buffer = new StringBuffer(data.length * 2);

		for (int index = 0; index < data.length; index++) {
			int hex = data[index] & 0xFF;
			if (hex < 16) {
				buffer.append("0");
			}

			buffer.append(Long.toString(hex, 16));
		}

		return buffer.toString();
	}
	
}
