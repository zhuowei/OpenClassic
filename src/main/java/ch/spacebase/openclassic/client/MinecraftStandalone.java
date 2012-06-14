package ch.spacebase.openclassic.client;

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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import com.mojang.minecraft.MinecraftApplet;
import com.mojang.minecraft.SessionData;
import com.mojang.minecraft.render.TextureManager;

/**
 * @author Steveice10 <Steveice10@gmail.com>
 */
public class MinecraftStandalone {
	
	private static String username = "";
	private static String haspaid = "";
	
	public static boolean debug;
	
	public static void main(String[] args) {
		if(Arrays.asList(args).contains("debug")) debug = true;
		
		String user = "";
		String pass = "";
		for(String arg : args) {
			if(arg.contains("username=")) user = arg.split("=")[1];
			if(arg.contains("password=")) pass = arg.split("=")[1];
			
			if(!user.equals("") && !pass.equals("")) {
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
		final MinecraftApplet applet = new MinecraftApplet();
		JFrame frame = new JFrame("Minecraft " + Minecraft.VERSION);

		try {
			frame.setIconImage(ImageIO.read(TextureManager.class.getResourceAsStream("/icon.png")));
		} catch (IOException e) {
		}

		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		frame.setMinimumSize(new Dimension(854, 510));
		frame.setMaximumSize(new Dimension(854, 510));
		frame.setResizable(false);
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
				applet.getMinecraft().running = false;
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

		if (user != null && pass != null && !user.equals("") && !pass.equals("")) {
			if(!auth(user, pass)) {
				JOptionPane.showMessageDialog(null, "Login Failed! You will not be able to play multiplayer.");
			}
		}

		applet.addParameter("username", username);
		applet.addParameter("sessionid", "-1");
		applet.addParameter("haspaid", haspaid);
		applet.setSize(854, 480);
		applet.init(true);

		frame.add(applet);
		frame.pack();
		frame.setVisible(true);
	}

	public static boolean auth(String username, String password) {
		CookieList cookies = new CookieList();
		CookieHandler.setDefault(cookies);

		System.out.println("Authing...");
		String hash = "";

		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			digest.update(username.toLowerCase().getBytes());
			digest.update(password.getBytes());

			hash = toHex(digest.digest());
		} catch (NoSuchAlgorithmException e) {
			System.out.println("SHA-1 not supported!");
			return false;
		}

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
			String server = HTTPUtil.fetchUrl("http://direct.worldofminecraft.com/server.txt", "");
			if (server.length() > 0) {
				server = server.trim();
				System.out.println("Getting server data for: " + server);

				try {
					String play = HTTPUtil.fetchUrl("http://www.minecraft.net/classic/play/" + URLEncoder.encode(server, "UTF-8"), "", "http://www.minecraft.net/classic/list");
					String mppass = HTTPUtil.getParameterOffPage(play, "mppass");
	
					if (mppass.length() > 0) {
						String user = HTTPUtil.getParameterOffPage(play, "username");
						System.out.println("Got user details: " + user);
	
						MinecraftStandalone.username = user;
						
						try {
							haspaid = HTTPUtil.fetchUrl("http://www.minecraft.net/haspaid.jsp", "user=" + URLEncoder.encode(user, "UTF-8"));
						} catch(UnsupportedEncodingException e) {
						}
						
						String validate = HTTPUtil.fetchUrl("https://direct.worldofminecraft.com/validate.php", "username=" + URLEncoder.encode(user, "UTF-8") + "&passHash=" + URLEncoder.encode(hash, "UTF-8") + "&server=" + URLEncoder.encode(server, "UTF-8") + "&mppass=" + URLEncoder.encode(mppass, "UTF-8"));
						int index = validate.indexOf('\n');
	
						if (index > 0) {
							String returnCode = validate.substring(0, index);
							if (returnCode.startsWith("Validated")) {
								System.out.println("Successfully validated with WOM direct.");
							    String[] servers = validate.split("\n");

							    for (String data : servers) {
							    	if(data.startsWith("Validated")) continue;
							    	Server s = new Server(username, data);
							    	SessionData.servers.add(s);
							    	if(s.username.contains("@")) {
							    		s.username = s.username.substring(0, s.username.indexOf('@'));
							    	}
							    	
							    	SessionData.serverInfo.add(s.name + " - " + s.description + " : Max Players: " + s.max + ", Online: " + s.users);
							    }
							    
								return true;
							}
						}
	
						System.out.println("Unable to validate with WOM: " + validate);
						return true;
					}
				} catch(UnsupportedEncodingException e) {
					System.out.println("UTF-8 not supported!");
					return false;
				}
			}
			
			return true;
		}

		return false;
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
