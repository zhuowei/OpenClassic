package ch.spacebase.openclassic.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.MinecraftCanvas;
import com.mojang.minecraft.render.TextureManager;

/**
 * @author Steveice10 <Steveice10@gmail.com>
 */
public class MinecraftStandalone {

	public static JFrame frame;
	private static Minecraft minecraft;
	
	public static void start(String[] args) {
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

		frame.setLayout(new BorderLayout());
		frame.add(canvas, "Center");
		canvas.setFocusable(true);
		frame.pack();
		frame.setVisible(true);
	}

}
