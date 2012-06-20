package com.mojang.minecraft.level;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.data.NBTData;
import ch.spacebase.openclassic.api.event.EventFactory;
import ch.spacebase.openclassic.api.event.level.LevelLoadEvent;
import ch.spacebase.openclassic.api.event.level.LevelSaveEvent;
import ch.spacebase.openclassic.client.io.OpenClassicLevelFormat;
import ch.spacebase.openclassic.client.level.ClientLevel;

import com.mojang.minecraft.ProgressBarDisplay;
import com.mojang.minecraft.level.Level;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class LevelIO {

	private ProgressBarDisplay progress;

	public LevelIO(ProgressBarDisplay progress) {
		this.progress = progress;
	}

	public final boolean save(Level level) {
		/* try {
			FileOutputStream out = new FileOutputStream(file);
			save(level, out);
			out.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			if (this.progress != null) {
				this.progress.setText("Failed!");
			}

			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e1) {
			}

			return false;
		} */
		
		if(EventFactory.callEvent(new LevelSaveEvent(level.openclassic)).isCancelled()) {
			return true;
		}
		
		try {
			OpenClassicLevelFormat.save(level.openclassic);
			if(level.openclassic.getData() != null) level.openclassic.getData().save(OpenClassic.getGame().getDirectory().getPath() + "/levels/" + level.name + ".nbt");
			return true;
		} catch (IOException e) {
			if (this.progress != null) {
				this.progress.setText("Failed!");
			}
			
			e.printStackTrace();
			
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e1) {
			}
			
			return false;
		}
	}

	public final Level load(String name) {
		/* try {
			FileInputStream in = new FileInputStream(file);
			Level level = this.load(in);
			in.close();
			return level;
		} catch (Exception e) {
			e.printStackTrace();
			if (this.progress != null) {
				this.progress.setText("Failed!");
			}

			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e1) {
			}

			return null;
		} */
		
		if (this.progress != null) {
			this.progress.setTitle("Loading level");
			this.progress.setText("Reading..");
		}
		
		try {
			Level level = ((ClientLevel) OpenClassicLevelFormat.load(name, false)).getHandle();
			level.openclassic.data = new NBTData(level.name);
			level.openclassic.data.load(OpenClassic.getGame().getDirectory().getPath() + "/levels/" + level.name + ".nbt");
			EventFactory.callEvent(new LevelLoadEvent(level.openclassic));
			return level;
		} catch (IOException e) {
			if (this.progress != null) {
				this.progress.setText("Failed!");
			}
			
			e.printStackTrace();
			
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e1) {
			}
			
			return null;
		}
	}

	/* public final boolean saveOnline(Level level, String host, String username, String sessionid, String levelName, int id) {
		if (sessionid == null) {
			sessionid = "";
		}

		if (this.progress != null && this.progress != null) {
			this.progress.setTitle("Saving level");
		}

		try {
			if (this.progress != null && this.progress != null) {
				this.progress.setText("Compressing..");
			}

			ByteArrayOutputStream levelData = new ByteArrayOutputStream();
			save(level, levelData);
			levelData.close();
			byte[] data = levelData.toByteArray();
			if (this.progress != null && this.progress != null) {
				this.progress.setText("Connecting..");
			}

			HttpURLConnection conn = (HttpURLConnection) (new URL("http://" + host + "/level/save.html")).openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			DataOutputStream out = new DataOutputStream(conn.getOutputStream());
			out.writeUTF(username);
			out.writeUTF(sessionid);
			out.writeUTF(levelName);
			out.writeByte(id);
			out.writeInt(data.length);
			if (this.progress != null) {
				this.progress.setText("Saving..");
			}

			out.write(data);
			out.close();
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			if (!reader.readLine().equalsIgnoreCase("ok")) {
				if (this.progress != null) {
					this.progress.setText("Failed: " + reader.readLine());
				}

				reader.close();
				Thread.sleep(1000L);
				return false;
			} else {
				reader.close();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (this.progress != null) {
				this.progress.setText("Failed!");
			}

			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e1) {
			}

			return false;
		}
	} */

	/* public final Level loadOnline(String host, String username, int id) {
		if (this.progress != null) {
			this.progress.setTitle("Loading level");
		}

		try {
			if (this.progress != null) {
				this.progress.setText("Connecting..");
			}

			HttpURLConnection conn = (HttpURLConnection) (new URL("http://" + host + "/level/load.html?id=" + id + "&user=" + username)).openConnection();
			conn.setDoInput(true);
			
			if (this.progress != null) {
				this.progress.setText("Loading..");
			}

			DataInputStream in = new DataInputStream(conn.getInputStream());
			if (in.readUTF().equalsIgnoreCase("ok")) {
				return this.load(in);
			} else {
				if (this.progress != null) {
					this.progress.setText("Failed: " + in.readUTF());
				}

				in.close();
				Thread.sleep(1000L);
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (this.progress != null) {
				this.progress.setText("Failed!");
			}

			try {
				Thread.sleep(3000L);
			} catch (InterruptedException e1) {
			}

			return null;
		}
	} */

	/* public final Level load(InputStream in) {
		if (this.progress != null) {
			this.progress.setTitle("Loading level");
		}

		if (this.progress != null) {
			this.progress.setText("Reading..");
		}

		try {
			DataInputStream data = new DataInputStream(new GZIPInputStream(in));
			if (data.readInt() != 656127880) {
				return null;
			} else {
				byte var12 = data.readByte();
				if (var12 > 2) {
					return null;
				} else if (var12 <= 1) {
					String name = data.readUTF();
					String creator = data.readUTF();
					long createtime = data.readLong();
					short width = data.readShort();
					short height = data.readShort();
					short depth = data.readShort();
					byte[] blocks = new byte[width * height * depth];
					data.readFully(blocks);
					data.close();
					Level level = new Level();
					level.setData(width, depth, height, blocks);
					level.name = name;
					level.creator = creator;
					level.createTime = createtime;
					if(level.openclassic == null) level.openclassic = new ClientLevel(level);
					return level;
				} else {
					LevelObjectStream obj = new LevelObjectStream(data);
					Level level = (Level) obj.readObject();
					if(level.openclassic == null) level.openclassic = new ClientLevel(level);
					level.initTransient();
					obj.close();
					return level;
				}
			}
		} catch (Exception e) {
			System.out.println("Failed to load level: " + e.toString());
			e.printStackTrace();
			return null;
		}
		
		try {
			return OpenClassicLevelFormat.load(in);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	} */

	public static void saveOld(Level level) {
		if(EventFactory.callEvent(new LevelSaveEvent(level.openclassic)).isCancelled()) {
			return;
		}
		
		try {
			DataOutputStream data = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(new File(OpenClassic.getGame().getDirectory(), "levels/" + level.name + ".mine"))));
			data.writeInt(656127880);
			data.writeByte(2);
			ObjectOutputStream obj = new ObjectOutputStream(data);
			obj.writeObject(level);
			obj.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static byte[] processData(InputStream in) {
		try {
			DataInputStream dataIn = new DataInputStream(new GZIPInputStream(in));
			byte[] data = new byte[dataIn.readInt()];
			dataIn.readFully(data);
			dataIn.close();
			return data;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
