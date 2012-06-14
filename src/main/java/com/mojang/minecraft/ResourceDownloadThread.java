package com.mojang.minecraft;

import com.mojang.minecraft.Minecraft;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public final class ResourceDownloadThread extends Thread {

	private File resource;
	private Minecraft mc;
	public boolean running = true;
	
	private boolean finished = false;
	private int progress = 0;

	public ResourceDownloadThread(File location, Minecraft mc, ProgressBarDisplay progress) {
		this.mc = mc;
		this.setName("Resource download thread");
		this.setDaemon(true);
		
		this.resource = new File(location, "resources/");
		if (!this.resource.exists() && !this.resource.mkdirs()) {
			throw new RuntimeException("The resource directory could not be created: " + this.resource);
		}
	}

	public final void run() {
		BufferedReader reader = null;

		try {
			ArrayList<String> list = new ArrayList<String>();
			URL base = new URL("http://dl.dropbox.com/u/40737374/minecraft_resources/");
			URL url = new URL(base, "resources/");
			
			reader = new BufferedReader(new InputStreamReader(url.openStream()));
			String line = null;

			while ((line = reader.readLine()) != null)
				list.add(line);

			reader.close();

			for (String curr : list) {
				try {
					String split[] = curr.split(",");
					int size = Integer.parseInt(split[1]);
					File file = new File(this.resource, split[0]);

					if (!file.exists() || file.length() != size) {
						file.getParentFile().mkdirs();
						this.download(new URL(base, split[0].replaceAll(" ", "%20")), file, size);
					} else {
						int index = split[0].indexOf("/");
						if (split[0].substring(0, index).equalsIgnoreCase("sound")) {
							this.mc.audio.registerSound(split[0].substring(index + 1, split[0].length() - 4).replaceAll("[1-9]", "").replaceAll("/", "."), file.toURI().toURL());
						} else if (split[0].substring(0, index).equalsIgnoreCase("music")) {
							if(split[0].contains("sweden"))  {
								this.mc.audio.registerMusic("menu", file.toURI().toURL());
							}
							
							this.mc.audio.registerMusic("bg", file.toURI().toURL());
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (!this.running) return;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		this.finished = true;
	}
	
	private void download(URL url, File file, int size) {
		System.out.println("Downloading " + file.getName());
		this.mc.progressBar.setText(file.getName(), false);
		
		byte[] data = new byte[4096];
		DataInputStream in = null;
		DataOutputStream out = null;
		
		try {
			in = new DataInputStream(url.openStream());
			out = new DataOutputStream(new FileOutputStream(file));

			int length = 0;
			int done = 0;
			while (this.running) {
				length = in.read(data);
				if (length < 0) return;

				out.write(data, 0, length);
				done += length;
				this.progress = (int) (((double) done / (double) size) * 100);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null)
					in.close();
				if (out != null)
					out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		this.mc.progressBar.setText("", false);
		this.progress = 0;
		
		System.out.println("Downloaded " + file.getName());
	}
	
	public int getProgress() {
		return this.progress;
	}
	
	public boolean isFinished() {
		return this.finished;
	}
	
}