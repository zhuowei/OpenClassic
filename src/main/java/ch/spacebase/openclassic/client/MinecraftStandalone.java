package ch.spacebase.openclassic.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.URLEncoder;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import ch.spacebase.openclassic.client.cookie.Cookie;
import ch.spacebase.openclassic.client.cookie.CookieList;
import ch.spacebase.openclassic.client.util.HTTPUtil;
import ch.spacebase.openclassic.client.util.Server;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.MinecraftCanvas;
import com.mojang.minecraft.SessionData;
import com.mojang.minecraft.render.TextureManager;

/**
 * @author Steveice10 <Steveice10@gmail.com>
 */
public class MinecraftStandalone {

	public static boolean debug;
	public static JFrame frame;
	
	private static Minecraft minecraft;
	
	public static void main(String[] args) {
		if (Arrays.asList(args).contains("debug"))
			debug = true;

		String user = "";
		String pass = "";
		for (String arg : args) {
			if (arg.contains("username="))
				user = arg.split("=")[1];
			if (arg.contains("password="))
				pass = arg.split("=")[1];

			if (!user.equals("") && !pass.equals("")) {
				init(user, pass);
				return;
			}
		}

		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if (info.getName().equals("Nimbus")) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Login frame = new Login();
					GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
					frame.setLocation((gd.getDisplayMode().getWidth() - frame.getWidth()) / 2, (gd.getDisplayMode().getHeight() - frame.getHeight()) / 2);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static void init(String user, String pass) {
		frame = new JFrame("Loading...");

		try {
			frame.setIconImage(ImageIO.read(TextureManager.class.getResourceAsStream("/icon.png")));
		} catch (IOException e) {
		}

		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		frame.setMinimumSize(new Dimension(854, 480));
		frame.setLocation((gd.getDisplayMode().getWidth() - frame.getWidth()) / 2, (gd.getDisplayMode().getHeight() - frame.getHeight()) / 2);

		frame.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
				e.getWindow().dispose();
			}

			@Override
			public void windowClosed(WindowEvent e) {
				minecraft.running = false;
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowActivated(WindowEvent e) {
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}
		});

		MinecraftCanvas canvas = new MinecraftCanvas();
		minecraft = new Minecraft(canvas, frame.getWidth(), frame.getHeight());
		canvas.setMinecraft(minecraft);
		canvas.setSize(frame.getWidth(), frame.getHeight());
		
		if (user != null && pass != null && !user.equals("") && !pass.equals("")) {
			if (!auth(user, pass)) {
				JOptionPane.showMessageDialog(null, "Login Failed! You will not be able to play multiplayer.");
			}
		}

		frame.setLayout(new BorderLayout());
		frame.add(canvas, "Center");
		canvas.setFocusable(true);
		frame.pack();
		frame.setVisible(true);
	}

	public static boolean auth(String username, String password) {
		CookieList cookies = new CookieList();
		CookieHandler.setDefault(cookies);

		System.out.println("Authing...");
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
			minecraft.data = new SessionData(result.substring(result.indexOf("Logged in as ") + 13, result.indexOf(" | ")), "-1");
			
			try {
				minecraft.data.haspaid = HTTPUtil.fetchUrl("http://www.minecraft.net/haspaid.jsp", "user=" + URLEncoder.encode(minecraft.data.username, "UTF-8")).equals("true");
			} catch (UnsupportedEncodingException e) {
			}

			System.out.println("Success! You are " + minecraft.data.username + " and you " + (minecraft.data.haspaid ? "have not" : "have") + " paid!");
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
