package com.mojang.minecraft;

import ch.spacebase.openclassic.api.block.Block;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.StepSound;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.block.custom.CustomBlock;
import ch.spacebase.openclassic.api.block.model.BoundingBox;
import ch.spacebase.openclassic.api.block.model.CubeModel;
import ch.spacebase.openclassic.api.block.model.CuboidModel;
import ch.spacebase.openclassic.api.block.model.TransparentModel;
import ch.spacebase.openclassic.api.block.model.Model;
import ch.spacebase.openclassic.api.block.model.Quad;
import ch.spacebase.openclassic.api.block.model.Texture;
import ch.spacebase.openclassic.api.block.model.Vertex;
import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.event.EventFactory;
import ch.spacebase.openclassic.api.event.block.BlockPlaceEvent;
import ch.spacebase.openclassic.api.event.player.PlayerJoinEvent;
import ch.spacebase.openclassic.api.event.player.PlayerKeyChangeEvent;
import ch.spacebase.openclassic.api.event.player.PlayerKickEvent;
import ch.spacebase.openclassic.api.event.player.PlayerLoginEvent;
import ch.spacebase.openclassic.api.event.player.PlayerLoginEvent.Result;
import ch.spacebase.openclassic.api.event.player.PlayerQuitEvent;
import ch.spacebase.openclassic.api.event.player.PlayerRespawnEvent;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.level.LevelInfo;
import ch.spacebase.openclassic.api.level.generator.Generator;
import ch.spacebase.openclassic.api.plugin.Plugin;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.client.ClassicClient;
import ch.spacebase.openclassic.client.gui.MainMenuScreen;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.client.render.ClientRenderHelper;
import ch.spacebase.openclassic.client.sound.ClientAudioManager;
import ch.spacebase.openclassic.client.util.BlockUtils;
import ch.spacebase.openclassic.client.util.LWJGLNatives;

import com.mojang.minecraft.gamemode.CreativeGameMode;
import com.mojang.minecraft.gamemode.GameMode;
import com.mojang.minecraft.gamemode.SurvivalGameMode;
import com.mojang.minecraft.gui.ChatInputScreen;
import com.mojang.minecraft.gui.GameOverScreen;
import com.mojang.minecraft.gui.HUDScreen;
import com.mojang.minecraft.gui.ErrorScreen;
import com.mojang.minecraft.gui.MenuScreen;
import com.mojang.minecraft.item.Arrow;
import com.mojang.minecraft.item.Item;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.level.LevelIO;
import com.mojang.minecraft.mob.Mob;
import com.mojang.minecraft.model.ModelManager;
import com.mojang.minecraft.model.ModelRenderer;
import com.mojang.minecraft.net.NetworkManager;
import com.mojang.minecraft.net.NetworkPlayer;
import com.mojang.minecraft.net.PacketType;
import com.mojang.minecraft.particle.ParticleManager;
import com.mojang.minecraft.particle.WaterDropParticle;
import com.mojang.minecraft.phys.AABB;
import com.mojang.minecraft.player.InputHandler;
import com.mojang.minecraft.player.Player;
import com.mojang.minecraft.render.ClippingHelper;
import com.mojang.minecraft.render.FontRenderer;
import com.mojang.minecraft.render.EntityRenderer;
import com.mojang.minecraft.render.ShapeRenderer;
import com.mojang.minecraft.render.TextureManager;
import com.mojang.minecraft.render.LevelRenderer;
import com.mojang.minecraft.render.animation.AnimatedTexture;
import com.mojang.util.MathHelper;
import java.awt.AWTException;
import java.awt.Canvas;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Controllers;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.GLU;

public final class Minecraft implements Runnable {

	public static final String VERSION = "0.31";
	private static final Random rand = new Random();

	public GameMode mode;
	private boolean fullscreen = false;
	public int width;
	public int height;
	private Timer timer = new Timer(20);
	public Level level;
	public LevelRenderer levelRenderer;
	public Player player;
	public ParticleManager particleManager;
	public SessionData data = null;
	public String host;
	public Canvas canvas;
	public boolean noLevel = false;
	public volatile boolean stopping = false;
	private Cursor cursor;
	public TextureManager textureManager;
	public FontRenderer fontRenderer;
	public GuiScreen currentScreen = null;
	public ProgressBarDisplay progressBar = new ProgressBarDisplay(this);
	public EntityRenderer renderer = new EntityRenderer(this);
	public LevelIO levelIo;
	public ClientAudioManager audio;
	public ResourceDownloadThread resourceThread;
	private int ticks;
	private int blockHitTime;
	public Robot robot;
	public HUDScreen hud;
	public boolean online;
	public NetworkManager netManager;
	public MovingObjectPosition selected;
	public GameSettings settings;
	private MinecraftApplet applet;
	public String server;
	public int port;
	public volatile boolean running;
	public String debugInfo;
	public boolean hasMouse;
	private int lastClick;
	public boolean raining;
	public File dir;
	public boolean ingame;
	private boolean started;
	public String levelName = "";
	public int levelSize = 0;

	public boolean openclassicServer = false;
	public boolean hacks = true;
	private List<CustomBlock> clientCache = new ArrayList<CustomBlock>();
	private boolean ctf;
	public int mipmapMode = 0;

	static {
		// Apparently the enum needs a kickstart...
		@SuppressWarnings("unused")
		BlockType type = VanillaBlock.AIR;
	}

	public Minecraft(Canvas canvas, MinecraftApplet applet, int width, int height, boolean fullscreen) {
		this.levelIo = new LevelIO(this.progressBar);
		this.ticks = 0;
		this.blockHitTime = 0;
		this.online = false;
		this.selected = null;
		this.server = null;
		this.port = 0;
		this.running = false;
		this.debugInfo = "";
		this.hasMouse = false;
		this.lastClick = 0;
		this.raining = false;

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

		this.applet = applet;
		this.canvas = canvas;
		this.width = width;
		this.height = height;
		this.fullscreen = fullscreen;
		if (canvas != null) {
			try {
				this.robot = new Robot();
				return;
			} catch (AWTException e) {
				e.printStackTrace();
			}
		}
	}

	public final void setCurrentScreen(GuiScreen screen) {
		if (this.currentScreen != null) {
			this.currentScreen.onClose();
		}

		if (screen == null && this.player != null && this.mode instanceof SurvivalGameMode && this.player.health <= 0) {
			screen = new GameOverScreen();
		}

		this.currentScreen = screen;
		if (screen != null) {
			if (this.hasMouse) {
				this.player.releaseAllKeys();
				this.hasMouse = false;
				if (this.noLevel) {
					try {
						Mouse.setNativeCursor(null);
					} catch (LWJGLException e) {
						e.printStackTrace();
					}
				} else {
					Mouse.setGrabbed(false);
				}
			}

			int width = this.width * 240 / this.height;
			int height = this.height * 240 / this.height;
			screen.open(width, height);
			this.online = false;
		} else {
			this.grabMouse();
		}
	}

	private static void checkGLError(String task) {
		int error = GL11.glGetError();
		if (error != 0) {
			String message = GLU.gluErrorString(error);
			System.out.println("########## GL ERROR ##########");
			System.out.println("@ " + task);
			System.out.println(error + ": " + message);
			System.exit(0);
		}
	}

	public final void shutdown() {
		if(this.ingame) this.stopGame(false);

		try {
			if (this.resourceThread != null) {
				this.resourceThread.running = false;
			}
		} catch (Exception e) {
		}

		this.audio.cleanup();

		Mouse.destroy();
		Keyboard.destroy();
		Display.destroy();

		System.exit(0);
	}

	public void stopGame(boolean menu) {
		this.audio.stopMusic();

		if(menu) this.setCurrentScreen(new MainMenuScreen());

		if(!this.noLevel && this.level != null) {
			try {
				this.levelIo.save(this.level);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if(this.data != null) this.data.key = "";

		this.level = null;
		this.particleManager = null;
		this.hud = null;
		if(this.player != null && this.player.openclassic.getData() != null && this.netManager == null) this.player.openclassic.getData().save(OpenClassic.getClient().getDirectory().getPath() + "/player.nbt");

		if(this.netManager != null) {
			if(this.player != null) EventFactory.callEvent(new PlayerQuitEvent(OpenClassic.getClient().getPlayer(), "Quit"));
			if(this.netManager.isConnected()) {
				this.netManager.netHandler.close();
			}

			this.netManager = null;
		}

		this.openclassicServer = false;
		this.server = null;
		this.port = 0;
		this.data.key = "";
		this.online = false;
		this.ingame = false;
		this.hacks = true;
		this.player = null;
		this.settings.speed = false;
	}

	public void initGame() {
		this.initGame(OpenClassic.getGame().getGenerator("normal"));
	}

	public void initGame(Generator gen) {
		this.audio.stopMusic();
		this.audio.lastBGM = System.currentTimeMillis();

		if (this.server != null && this.data != null) {
			Level level = new Level();
			level.setData(8, 8, 8, new byte[512]);
			this.setLevel(level);
		} else {
			try {
				if (!this.noLevel) {
					Level var11 = this.levelIo.load("level");
					if (var11 != null) {
						this.setLevel(var11);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (this.level == null) {
				this.progressBar.setTitle("Generating...");
				this.progressBar.setText("");
				this.progressBar.setProgress(0);
				OpenClassic.getClient().createLevel(new LevelInfo(!this.levelName.equals("") ? this.levelName : "A Nice World", null, (short) (128 << this.levelSize), (short) 128, (short) (128 << this.levelSize)), gen);
				this.levelName = "";
			}
		}

		this.particleManager = new ParticleManager(this.level, this.textureManager);
		if (this.noLevel) {
			try {
				IntBuffer buffer = BufferUtils.createIntBuffer(256);
				buffer.clear().limit(256);
				this.cursor = new Cursor(16, 16, 0, 0, 1, buffer, null);
			} catch (LWJGLException e) {
				e.printStackTrace();
			}
		}

		checkGLError("Post startup");
		this.hud = new HUDScreen(this, this.width, this.height);
		(new SkinDownloadThread(this)).start();
		if (this.server != null && this.data != null) {
			this.netManager = new NetworkManager(this, this.server, this.port, this.data.username, this.data.key);
			this.hacks = false;
		}

		this.mode = this.settings.survival && this.netManager == null ? new SurvivalGameMode(this) : new CreativeGameMode(this);

		if(this.level != null) {
			this.mode.apply(this.level);
		}

		if(this.player != null) {
			this.mode.apply(this.player);
		}

		this.ingame = true;
	}

	@SuppressWarnings({ "null", "unused" })
	public final void run() {
		this.running = true;
		OpenClassic.setGame(new ClassicClient(this));

		try {
			String home = System.getProperty("user.home", ".");
			OS os = OS.lookup(System.getProperty("os.name").toLowerCase());
			switch (os) {
			case linux:
			case solaris:
				this.dir = new File(home, ".minecraft_classic/");
				break;
			case windows:
				if (System.getenv("APPDATA") != null) {
					this.dir = new File(System.getenv("APPDATA"), ".minecraft_classic/");
				} else {
					this.dir = new File(home, ".minecraft_classic/");
				}
				break;
			case macos:
				this.dir = new File(home, "Library/Application Support/minecraft_classic");
				break;
			default:
				this.dir = new File(home, "minecraft_classic/");
			}

			if (!this.dir.exists() && !this.dir.mkdirs()) {
				throw new RuntimeException("The working directory could not be created: " + dir);
			}

			File lib = new File(this.dir, "lib");
			if(!lib.exists()) {
				lib.mkdirs();
			}

			LWJGLNatives.load(os, lib);

			File file = new File(this.dir, "levels");
			if(!file.exists()) {
				file.mkdirs();
			}

			File f = new File(this.dir, "screenshots");
			if(!f.exists()) {
				f.mkdirs();
			}
			
			File fl = new File(this.dir, "texturepacks");
			if(!fl.exists()) {
				fl.mkdirs();
			}

			if (this.canvas != null) {
				Display.setParent(this.canvas);
			} else if (this.fullscreen) {
				Display.setFullscreen(true);
				this.width = Display.getDisplayMode().getWidth();
				this.height = Display.getDisplayMode().getHeight();
			} else {
				Display.setDisplayMode(new DisplayMode(this.width, this.height));
			}

			Display.setTitle("Minecraft " + VERSION);

			try {
				Display.create();
			} catch (LWJGLException e) {
				e.printStackTrace();

				try {
					Thread.sleep(1000L);
				} catch (InterruptedException e1) {
				}

				Display.create();
			}

			Keyboard.create();
			Mouse.create();

			try {
				Controllers.create();
			} catch (Exception var55) {
				var55.printStackTrace();
			}

			checkGLError("Pre startup");
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glShadeModel(GL11.GL_SMOOTH);
			GL11.glClearDepth(GL11.GL_CLIENT_PIXEL_STORE_BIT);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glDepthFunc(GL11.GL_LEQUAL);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0);
			GL11.glCullFace(GL11.GL_BACK);
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			GL11.glMatrixMode(GL11.GL_MODELVIEW);

			if (GLContext.getCapabilities().OpenGL30) {
				System.out.println("Using OpenGL 3.0 for mipmap generation.");
				this.mipmapMode = 1;
			} else if (GLContext.getCapabilities().GL_EXT_framebuffer_object) {
				System.out.println("Using GL_EXT_framebuffer_object extension for mipmap generation.");
				this.mipmapMode = 2;
			} else if (GLContext.getCapabilities().OpenGL14) {
				System.out.println("Using GL_GENERATE_MIPMAP for mipmap generation. This might slow down with large textures.");
				this.mipmapMode = 3;
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_GENERATE_MIPMAP, GL11.GL_TRUE);
			} else {
				System.out.println("Mipmaps unsupported.");
			}

			checkGLError("Startup");
			SessionData.loadFavorites(this.dir);

			this.audio = new ClientAudioManager(this);
			this.settings = new GameSettings(this, this.dir);

			this.mode = this.settings.survival ? new SurvivalGameMode(this) : new CreativeGameMode(this);
			this.textureManager = new TextureManager(this.settings);
			this.textureManager.addAnimatedTexture((new com.mojang.minecraft.render.animation.LavaTexture()));
			this.textureManager.addAnimatedTexture((new com.mojang.minecraft.render.animation.WaterTexture()));
			this.fontRenderer = new FontRenderer(this.settings, "/default.png", this.textureManager);
			this.levelRenderer = new LevelRenderer(this, this.textureManager);
			Item.initModels();
			Mob.modelCache = new ModelManager();
			GL11.glViewport(0, 0, this.width, this.height);

			((ClassicClient) OpenClassic.getClient()).init();

			try {
				this.resourceThread = new ResourceDownloadThread(dir, this, this.progressBar);
				this.resourceThread.start();
			} catch(Exception e) {
				e.printStackTrace();
			}

			this.progressBar.setTitle("Downloading Resources...");
			this.progressBar.setProgress(0);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.toString(), "Failed to start Minecraft", 0);
			return;
		}

		long lastUpdate = System.currentTimeMillis();
		int fps = 0;

		try {
			while (this.running) {
				if (this.stopping) {
					Thread.sleep(100L);
				} else {
					if (this.canvas == null && Display.isCloseRequested()) {
						this.running = false;
					}

					if(!this.started) {
						if(this.resourceThread.isFinished()) {
							Thread.sleep(1000);
							if(this.server == null || this.server.equals("") || this.port == 0) {
								this.setCurrentScreen(new MainMenuScreen());
							} else {
								this.initGame();
							}

							this.started = true;
						} else {
							this.progressBar.setProgress(this.resourceThread.getProgress());
							continue;
						}
					}

					try {
						long var18 = System.currentTimeMillis() - this.timer.g;
						long var20 = System.nanoTime() / 1000000L;
						double var24;
						if (var18 > 1000) {
							long var22 = var20 - this.timer.h;
							var24 = (double) var18 / (double) var22;
							this.timer.i += (var24 - this.timer.i) * 0.20000000298023224D;
							this.timer.g = System.currentTimeMillis();
							this.timer.h = var20;
						}

						if (var18 < 0L) {
							this.timer.g = System.currentTimeMillis();
							this.timer.h = var20;
						}

						double var95;
						var24 = ((var95 = var20 / 1000.0D) - this.timer.b) * this.timer.i;
						this.timer.b = var95;
						if (var24 < 0.0D) {
							var24 = 0.0D;
						}

						if (var24 > 1.0D) {
							var24 = 1.0D;
						}

						this.timer.f = (float) (this.timer.f + var24 * this.timer.e * this.timer.a);
						this.timer.c = (int) this.timer.f;
						if (this.timer.c > 100) {
							this.timer.c = 100;
						}

						this.timer.f -= this.timer.c;
						this.timer.time = this.timer.f;

						for (int var64 = 0; var64 < this.timer.c; var64++) {
							this.ticks++;
							this.tick();
						}

						checkGLError("Pre render");
						GL11.glEnable(GL11.GL_TEXTURE_2D);

						if (!this.online) {
							this.mode.applyBlockCracks(this.timer.time);
							float var65 = this.timer.time;
							com.mojang.minecraft.render.EntityRenderer var66 = this.renderer;
							if (this.renderer.displayActive && !Display.isActive() && !Mouse.isButtonDown(0) && !Mouse.isButtonDown(1) && !Mouse.isButtonDown(2)) { // Fixed focus bug for some computers/OS's
								this.displayMenu();
							}

							var66.displayActive = Display.isActive();
							int var68;
							int var70;
							int var86;
							int var81;
							if (this.hasMouse) {
								var81 = 0;
								var86 = 0;
								if (this.noLevel) {
									if (this.canvas != null) {
										Point var90 = this.canvas.getLocationOnScreen();
										var70 = var90.x + this.width / 2;
										var68 = var90.y + this.height / 2;
										Point var75 = MouseInfo.getPointerInfo().getLocation();
										var81 = var75.x - var70;
										var86 = -(var75.y - var68);
										this.robot.mouseMove(var70, var68);
									} else {
										Mouse.setCursorPosition(this.width / 2, this.height / 2);
									}
								} else {
									var81 = Mouse.getDX();
									var86 = Mouse.getDY();
								}

								byte var91 = 1;
								if (this.settings.invertMouse) {
									var91 = -1;
								}

								this.player.turn(var81, (var86 * var91));
							}

							if (!this.online) {
								var81 = this.width * 240 / this.height;
								var86 = this.height * 240 / this.height;
								int var94 = Mouse.getX() * var81 / this.width;
								var70 = var86 - Mouse.getY() * var86 / this.height - 1;
								if (this.level != null) {
									float var80 = var65;
									com.mojang.minecraft.render.EntityRenderer var82 = var66;
									com.mojang.minecraft.render.EntityRenderer var27 = var66;
									float var29 = (this.player = var66.mc.player).xRotO + (this.player.xRot - this.player.xRotO) * var65;
									float var30 = this.player.yRotO + (this.player.yRot - this.player.yRotO) * var65;
									com.mojang.minecraft.model.Vector var31 = var66.a(var65);
									float var32 = MathHelper.cos(-var30 * 0.017453292F - (float) Math.PI);
									float var69 = MathHelper.sin(-var30 * 0.017453292F - (float) Math.PI);
									float var74 = MathHelper.cos(-var29 * 0.017453292F);
									float var33 = MathHelper.sin(-var29 * 0.017453292F);
									float var34 = var69 * var74;
									float var87 = var32 * var74;
									float var36 = this.mode.d();
									com.mojang.minecraft.model.Vector var71 = var31.add(var34 * var36, var33 * var36, var87 * var36);
									this.selected = this.level.clip(var31, var71, true);
									var74 = var36;
									if (this.selected != null) {
										var74 = this.selected.blockPos.distance(var66.a(var65));
									}

									var31 = var66.a(var65);
									if (this.mode instanceof CreativeGameMode) {
										var36 = 32.0F;
									} else {
										var36 = var74;
									}

									var71 = var31.add(var34 * var36, var33 * var36, var87 * var36);
									var66.g = null;
									List<Entity> var37 = this.level.blockMap.getEntities(this.player, this.player.bb.expand(var34 * var36, var33 * var36, var87 * var36));
									float var35 = 0.0F;

									for (var81 = 0; var81 < var37.size(); ++var81) {
										Entity var88 = var37.get(var81);
										if (var88.isPickable()) {
											var74 = 0.1F;
											MovingObjectPosition var78;
											if ((var78 = var88.bb.grow(var74, var74, var74).clip(var31, var71)) != null && ((var74 = var31.distance(var78.blockPos)) < var35 || var35 == 0.0F)) {
												var27.g = var88;
												var35 = var74;
											}
										}
									}

									if (var27.g != null && !(this.mode instanceof CreativeGameMode)) {
										var27.mc.selected = new MovingObjectPosition(var27.g);
									}

									int var77 = 0;

									while (true) {
										if (var77 >= 2) {
											GL11.glColorMask(true, true, true, false);
											break;
										}

										if (this.settings.anaglyph) {
											if (var77 == 0) {
												GL11.glColorMask(false, true, true, false);
											} else {
												GL11.glColorMask(true, false, false, false);
											}
										}

										GL11.glViewport(0, 0, var82.mc.width, var82.mc.height);
										var29 = 1.0F / (4 - var82.mc.settings.viewDistance);
										var29 = 1.0F - (float) Math.pow(var29, 0.25D);
										var30 = (this.level.skyColor >> 16 & 255) / 255.0F;
										float var117 = (this.level.skyColor >> 8 & 255) / 255.0F;
										var32 = (this.level.skyColor & 255) / 255.0F;
										var82.i = (this.level.fogColor >> 16 & 255) / 255.0F;
										var82.j = (this.level.fogColor >> 8 & 255) / 255.0F;
										var82.k = (this.level.fogColor & 255) / 255.0F;
										var82.i += (var30 - var82.i) * var29;
										var82.j += (var117 - var82.j) * var29;
										var82.k += (var32 - var82.k) * var29;
										var82.i *= var82.b;
										var82.j *= var82.b;
										var82.k *= var82.b;
										BlockType var73 = Blocks.fromId(this.level.getTile((int) this.player.x, (int) (this.player.y + 0.12F), (int) this.player.z));
										if (var73 != null && var73.isLiquid()) {
											if (var73 == VanillaBlock.WATER || var73 == VanillaBlock.STATIONARY_WATER) {
												var82.i = 0.02F;
												var82.j = 0.02F;
												var82.k = 0.2F;
											} else if (var73 == VanillaBlock.LAVA || var73 == VanillaBlock.STATIONARY_LAVA) {
												var82.i = 0.6F;
												var82.j = 0.1F;
												var82.k = 0.0F;
											}
										}

										if (var82.mc.settings.anaglyph) {
											var74 = (var82.i * 30.0F + var82.j * 59.0F + var82.k * 11.0F) / 100.0F;
											var33 = (var82.i * 30.0F + var82.j * 70.0F) / 100.0F;
											var34 = (var82.i * 30.0F + var82.k * 70.0F) / 100.0F;
											var82.i = var74;
											var82.j = var33;
											var82.k = var34;
										}

										GL11.glClearColor(var82.i, var82.j, var82.k, 0.0F);
										GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
										var82.b = 1.0F;
										GL11.glEnable(GL11.GL_CULL_FACE);
										var82.d = (512 >> (var82.mc.settings.viewDistance << 1));
										GL11.glMatrixMode(GL11.GL_PROJECTION);
										GL11.glLoadIdentity();
										var29 = 0.07F;
										if (var82.mc.settings.anaglyph) {
											GL11.glTranslatef((-((var77 << 1) - 1)) * var29, 0.0F, 0.0F);
										}

										Player var116 = var82.mc.player;
										var69 = 70.0F;
										if (var116.health <= 0) {
											var74 = var116.deathTime + var80;
											var69 /= (1.0F - 500.0F / (var74 + 500.0F)) * 2.0F + 1.0F;
										}

										GLU.gluPerspective(var69, (float) var82.mc.width / (float) var82.mc.height, 0.05F, var82.d);
										GL11.glMatrixMode(GL11.GL_MODELVIEW);
										GL11.glLoadIdentity();
										if (var82.mc.settings.anaglyph) {
											GL11.glTranslatef(((var77 << 1) - 1) * 0.1F, 0.0F, 0.0F);
										}

										var82.b(var80);
										if (var82.mc.settings.viewBobbing) {
											var82.c(var80);
										}

										var116 = var82.mc.player;
										GL11.glTranslatef(0.0F, 0.0F, -0.1F);
										GL11.glRotatef(var116.xRotO + (var116.xRot - var116.xRotO) * var80, 1.0F, 0.0F, 0.0F);
										GL11.glRotatef(var116.yRotO + (var116.yRot - var116.yRotO) * var80, 0.0F, 1.0F, 0.0F);
										var69 = var116.xo + (var116.x - var116.xo) * var80;
										var74 = var116.yo + (var116.y - var116.yo) * var80;
										var33 = var116.zo + (var116.z - var116.zo) * var80;
										GL11.glTranslatef(-var69, -var74, -var33);
										ClippingHelper var76 = com.mojang.minecraft.render.ClippingHelper.prepare();
										ClippingHelper var100 = var76;

										int var98;
										for (var98 = 0; var98 < this.levelRenderer.chunkCache.length; ++var98) {
											this.levelRenderer.chunkCache[var98].setChunkRendered(var100);
										}

										try {
											Collections.sort(var82.mc.levelRenderer.chunks, new com.mojang.minecraft.render.ChunkDirtyAndDistanceComparator(this.player));
										} catch(Exception e) {
										}

										var98 = this.levelRenderer.chunks.size() - 1;
										int var105 = this.levelRenderer.chunks.size();
										if (var105 > 3) {
											var105 = 3;
										}

										int var104;
										for (var104 = 0; var104 < var105; ++var104) {
											com.mojang.minecraft.render.Chunk var118 = this.levelRenderer.chunks.remove(var98 - var104);
											var118.update();
											var118.loaded = false;
										}

										var82.renderFog();
										GL11.glEnable(GL11.GL_FOG);
										this.levelRenderer.sortChunks(this.player, 0);
										int var83;
										int var110;
										com.mojang.minecraft.render.ShapeRenderer var115;
										int var114;
										int var125;
										int var122;
										int var120;
										if (this.level.isSolid(this.player.x, this.player.y, this.player.z, 0.1F)) {
											var120 = (int) this.player.x;
											var83 = (int) this.player.y;
											var110 = (int) this.player.z;

											for (var122 = var120 - 1; var122 <= var120 + 1; ++var122) {
												for (var125 = var83 - 1; var125 <= var83 + 1; ++var125) {
													for (int var38 = var110 - 1; var38 <= var110 + 1; ++var38) {
														var105 = var38;
														var98 = var125;
														int var99 = this.levelRenderer.level.getTile(var122, var125, var38);
														if (var99 != 0 && Blocks.fromId(var99).isSolid()) {
															GL11.glColor4f(0.2F, 0.2F, 0.2F, 1.0F);
															GL11.glDepthFunc(GL11.GL_LESS);
															var115 = ShapeRenderer.instance;
															ShapeRenderer.instance.begin();

															Blocks.fromId(var99).getModel().renderAll(var122, var98, var105, 0.2F);

															var115.end();
															GL11.glCullFace(GL11.GL_FRONT);
															var115.begin();

															Blocks.fromId(var99).getModel().renderAll(var122, var98, var105, 0.2F);

															var115.end();
															GL11.glCullFace(GL11.GL_BACK);
															GL11.glDepthFunc(GL11.GL_LEQUAL);
														}
													}
												}
											}
										}

										var82.a(true);
										com.mojang.minecraft.model.Vector var103 = var82.a(var80);
										this.levelRenderer.level.blockMap.render(var103, var76, this.levelRenderer.textureManager, var80);
										var82.a(false);
										var82.renderFog();
										float var107 = var80;
										ParticleManager var96 = this.particleManager;
										var29 = -MathHelper.cos(this.player.yRot * 3.1415927F / 180.0F);
										var117 = -(var30 = -MathHelper.sin(this.player.yRot * 3.1415927F / 180.0F)) * MathHelper.sin(this.player.xRot * 3.1415927F / 180.0F);
										var32 = var29 * MathHelper.sin(this.player.xRot * 3.1415927F / 180.0F);
										var69 = MathHelper.cos(this.player.xRot * 3.1415927F / 180.0F);

										for (var83 = 0; var83 < 2; ++var83) {
											if (var96.particles[var83].size() != 0) {
												var110 = 0;
												if (var83 == 0) {
													var110 = var96.textureManager.bindTexture("/particles.png");
												}

												if (var83 == 1) {
													var110 = var96.textureManager.bindTexture("/terrain.png");
												}

												GL11.glBindTexture(GL11.GL_TEXTURE_2D, var110);
												ShapeRenderer.instance.begin();

												for (var120 = 0; var120 < var96.particles[var83].size(); ++var120) {
													var96.particles[var83].get(var120).render(ShapeRenderer.instance, var107, var29, var69, var30, var117, var32);
												}

												ShapeRenderer.instance.end();
											}
										}

										GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textureManager.bindTexture("/rock.png"));
										GL11.glEnable(GL11.GL_TEXTURE_2D);
										GL11.glCallList(this.levelRenderer.listId);
										var82.renderFog();
										GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textureManager.bindTexture("/clouds.png"));
										GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
										var107 = (this.levelRenderer.level.cloudColor >> 16 & 255) / 255.0F;
										var29 = (this.levelRenderer.level.cloudColor >> 8 & 255) / 255.0F;
										var30 = (this.levelRenderer.level.cloudColor & 255) / 255.0F;
										if (this.settings.anaglyph) {
											var117 = (var107 * 30.0F + var29 * 59.0F + var30 * 11.0F) / 100.0F;
											var32 = (var107 * 30.0F + var29 * 70.0F) / 100.0F;
											var69 = (var107 * 30.0F + var30 * 70.0F) / 100.0F;
											var107 = var117;
											var29 = var32;
											var30 = var69;
										}

										var115 = com.mojang.minecraft.render.ShapeRenderer.instance;
										var74 = 0.0F;
										var33 = 4.8828125E-4F;
										var74 = (this.levelRenderer.level.depth + 2);
										var34 = (this.levelRenderer.ticks + var80) * var33 * 0.03F;
										var35 = 0.0F;
										var115.begin();
										var115.color(var107, var29, var30);

										for (var86 = -2048; var86 < this.levelRenderer.level.width + 2048; var86 += 512) {
											for (var125 = -2048; var125 < this.levelRenderer.level.height + 2048; var125 += 512) {
												var115.vertexUV(var86, var74, (var125 + 512), var86 * var33 + var34, (var125 + 512) * var33);
												var115.vertexUV((var86 + 512), var74, (var125 + 512), (var86 + 512) * var33 + var34, (var125 + 512) * var33);
												var115.vertexUV((var86 + 512), var74, var125, (var86 + 512) * var33 + var34, var125 * var33);
												var115.vertexUV(var86, var74, var125, var86 * var33 + var34, var125 * var33);
												var115.vertexUV(var86, var74, var125, var86 * var33 + var34, var125 * var33);
												var115.vertexUV((var86 + 512), var74, var125, (var86 + 512) * var33 + var34, var125 * var33);
												var115.vertexUV((var86 + 512), var74, (var125 + 512), (var86 + 512) * var33 + var34, (var125 + 512) * var33);
												var115.vertexUV(var86, var74, (var125 + 512), var86 * var33 + var34, (var125 + 512) * var33);
											}
										}

										var115.end();
										GL11.glDisable(GL11.GL_TEXTURE_2D);
										var115.begin();
										var34 = (this.levelRenderer.level.skyColor >> 16 & 255) / 255.0F;
										var35 = (this.levelRenderer.level.skyColor >> 8 & 255) / 255.0F;
										var87 = (this.levelRenderer.level.skyColor & 255) / 255.0F;
										if (this.settings.anaglyph) {
											var36 = (var34 * 30.0F + var35 * 59.0F + var87 * 11.0F) / 100.0F;
											var69 = (var34 * 30.0F + var35 * 70.0F) / 100.0F;
											var74 = (var34 * 30.0F + var87 * 70.0F) / 100.0F;
											var34 = var36;
											var35 = var69;
											var87 = var74;
										}

										var115.color(var34, var35, var87);
										var74 = (this.levelRenderer.level.depth + 10);

										for (var125 = -2048; var125 < this.levelRenderer.level.width + 2048; var125 += 512) {
											for (var68 = -2048; var68 < this.levelRenderer.level.height + 2048; var68 += 512) {
												var115.vertex(var125, var74, var68);
												var115.vertex((var125 + 512), var74, var68);
												var115.vertex((var125 + 512), var74, (var68 + 512));
												var115.vertex(var125, var74, (var68 + 512));
											}
										}

										var115.end();
										GL11.glEnable(GL11.GL_TEXTURE_2D);
										var82.renderFog();
										int var108;
										if (var82.mc.selected != null) {
											GL11.glDisable(GL11.GL_ALPHA_TEST);
											MovingObjectPosition var10001 = var82.mc.selected;
											var105 = this.player.inventory.getSelected();
											MovingObjectPosition var102 = var10001;
											com.mojang.minecraft.render.ShapeRenderer var113 = com.mojang.minecraft.render.ShapeRenderer.instance;
											GL11.glEnable(GL11.GL_BLEND);
											GL11.glEnable(GL11.GL_ALPHA_TEST);
											GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
											GL11.glColor4f(1.0F, 1.0F, 1.0F, (MathHelper.sin(System.currentTimeMillis() / 100.0F) * 0.2F + 0.4F) * 0.5F);
											if (this.levelRenderer.cracks > 0) {
												GL11.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_SRC_COLOR);
												var108 = this.levelRenderer.textureManager.bindTexture("/terrain.png");
												GL11.glBindTexture(GL11.GL_TEXTURE_2D, var108);
												GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
												GL11.glPushMatrix();
												BlockType var10000 = (var114 = this.levelRenderer.level.getTile(var102.x, var102.y, var102.z)) > 0 ? Blocks.fromId(var114) : null;
												var73 = var10000;
												var74 = (var73.getModel().getSelectionBox().getX1() + var73.getModel().getSelectionBox().getX2()) / 2.0F;
												var33 = (var73.getModel().getSelectionBox().getY1() + var73.getModel().getSelectionBox().getY2()) / 2.0F;
												var34 = (var73.getModel().getSelectionBox().getZ1() + var73.getModel().getSelectionBox().getZ2()) / 2.0F;
												GL11.glTranslatef(var102.x + var74, var102.y + var33, var102.z + var34);
												var35 = 1.01F;
												GL11.glScalef(1.01F, var35, var35);
												GL11.glTranslatef(-(var102.x + var74), -(var102.y + var33), -(var102.z + var34));
												var113.begin();
												var113.noColor();
												GL11.glDepthMask(false);
												if (var73 == null) {
													var73 = VanillaBlock.STONE;
												}

												for (var86 = 0; var86 < var73.getModel().getQuads().size(); ++var86) {
													ClientRenderHelper.getHelper().drawCracks(var73.getModel().getQuad(var86), var102.x, var102.y, var102.z, 240 + (int) (this.levelRenderer.cracks * 10.0F));
												}

												var113.end();
												GL11.glDepthMask(true);
												GL11.glPopMatrix();
											}

											GL11.glDisable(GL11.GL_BLEND);
											GL11.glDisable(GL11.GL_ALPHA_TEST);
											var10001 = var82.mc.selected;
											this.player.inventory.getSelected();
											var102 = var10001;
											GL11.glEnable(GL11.GL_BLEND);
											GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
											GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
											GL11.glLineWidth(2.0F);
											GL11.glDisable(GL11.GL_TEXTURE_2D);
											GL11.glDepthMask(false);
											var29 = 0.002F;
											var104 = this.levelRenderer.level.getTile(var102.x, var102.y, var102.z);
											if (var104 > 0) {
												AABB aabb = BlockUtils.getSelectionBox(var104, var102.x, var102.y, var102.z).grow(var29, var29, var29);
												GL11.glBegin(GL11.GL_LINE_STRIP);
												GL11.glVertex3f(aabb.x0, aabb.y0, aabb.z0);
												GL11.glVertex3f(aabb.x1, aabb.y0, aabb.z0);
												GL11.glVertex3f(aabb.x1, aabb.y0, aabb.z1);
												GL11.glVertex3f(aabb.x0, aabb.y0, aabb.z1);
												GL11.glVertex3f(aabb.x0, aabb.y0, aabb.z0);
												GL11.glEnd();
												GL11.glBegin(GL11.GL_LINE_STRIP);
												GL11.glVertex3f(aabb.x0, aabb.y1, aabb.z0);
												GL11.glVertex3f(aabb.x1, aabb.y1, aabb.z0);
												GL11.glVertex3f(aabb.x1, aabb.y1, aabb.z1);
												GL11.glVertex3f(aabb.x0, aabb.y1, aabb.z1);
												GL11.glVertex3f(aabb.x0, aabb.y1, aabb.z0);
												GL11.glEnd();
												GL11.glBegin(GL11.GL_LINES);
												GL11.glVertex3f(aabb.x0, aabb.y0, aabb.z0);
												GL11.glVertex3f(aabb.x0, aabb.y1, aabb.z0);
												GL11.glVertex3f(aabb.x1, aabb.y0, aabb.z0);
												GL11.glVertex3f(aabb.x1, aabb.y1, aabb.z0);
												GL11.glVertex3f(aabb.x1, aabb.y0, aabb.z1);
												GL11.glVertex3f(aabb.x1, aabb.y1, aabb.z1);
												GL11.glVertex3f(aabb.x0, aabb.y0, aabb.z1);
												GL11.glVertex3f(aabb.x0, aabb.y1, aabb.z1);
												GL11.glEnd();
											}

											GL11.glDepthMask(true);
											GL11.glEnable(GL11.GL_TEXTURE_2D);
											GL11.glDisable(GL11.GL_BLEND);
											GL11.glEnable(GL11.GL_ALPHA_TEST);
										}

										GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
										var82.renderFog();
										GL11.glEnable(GL11.GL_TEXTURE_2D);
										GL11.glEnable(GL11.GL_BLEND);
										GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.levelRenderer.textureManager.bindTexture("/water.png"));
										GL11.glCallList(this.levelRenderer.listId + 1);
										GL11.glDisable(GL11.GL_BLEND);
										GL11.glEnable(GL11.GL_BLEND);
										GL11.glColorMask(false, false, false, false);
										var120 = this.levelRenderer.sortChunks(this.player, 1);
										GL11.glColorMask(true, true, true, true);
										if (var82.mc.settings.anaglyph) {
											if (var77 == 0) {
												GL11.glColorMask(false, true, true, false);
											} else {
												GL11.glColorMask(true, false, false, false);
											}
										}

										if (var120 > 0) {
											GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textureManager.bindTexture("/terrain.png"));
											GL11.glCallLists(this.levelRenderer.buffer);
										}

										GL11.glDepthMask(true);
										GL11.glDisable(GL11.GL_BLEND);
										GL11.glDisable(GL11.GL_FOG);
										if (var82.mc.raining) {
											float var97 = var80;
											var27 = var82;
											this.player = var82.mc.player;
											Level var109 = var82.mc.level;
											var104 = (int) this.player.x;
											var108 = (int) this.player.y;
											var114 = (int) this.player.z;
											com.mojang.minecraft.render.ShapeRenderer var84 = com.mojang.minecraft.render.ShapeRenderer.instance;
											GL11.glDisable(GL11.GL_CULL_FACE);
											GL11.glNormal3f(0.0F, 1.0F, 0.0F);
											GL11.glEnable(GL11.GL_BLEND);
											GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
											GL11.glBindTexture(GL11.GL_TEXTURE_2D, var82.mc.textureManager.bindTexture("/rain.png"));

											for (var110 = var104 - 5; var110 <= var104 + 5; ++var110) {
												for (var122 = var114 - 5; var122 <= var114 + 5; ++var122) {
													var120 = var109.getHighestTile(var110, var122);
													var86 = var108 - 5;
													var125 = var108 + 5;
													if (var86 < var120) {
														var86 = var120;
													}

													if (var125 < var120) {
														var125 = var120;
													}

													if (var86 != var125) {
														var74 = (((var27.levelTicks + var110 * 3121 + var122 * 418711) % 32) + var97) / 32.0F;
														float var124 = var110 + 0.5F - this.player.x;
														var35 = var122 + 0.5F - this.player.z;
														float var92 = MathHelper.sqrt(var124 * var124 + var35 * var35) / 5;
														GL11.glColor4f(1.0F, 1.0F, 1.0F, (1.0F - var92 * var92) * 0.7F);
														var84.begin();
														var84.vertexUV(var110, var86, var122, 0.0F, var86 * 2.0F / 8.0F + var74 * 2.0F);
														var84.vertexUV((var110 + 1), var86, (var122 + 1), 2.0F, var86 * 2.0F / 8.0F + var74 * 2.0F);
														var84.vertexUV((var110 + 1), var125, (var122 + 1), 2.0F, var125 * 2.0F / 8.0F + var74 * 2.0F);
														var84.vertexUV(var110, var125, var122, 0.0F, var125 * 2.0F / 8.0F + var74 * 2.0F);
														var84.vertexUV(var110, var86, (var122 + 1), 0.0F, var86 * 2.0F / 8.0F + var74 * 2.0F);
														var84.vertexUV((var110 + 1), var86, var122, 2.0F, var86 * 2.0F / 8.0F + var74 * 2.0F);
														var84.vertexUV((var110 + 1), var125, var122, 2.0F, var125 * 2.0F / 8.0F + var74 * 2.0F);
														var84.vertexUV(var110, var125, (var122 + 1), 0.0F, var125 * 2.0F / 8.0F + var74 * 2.0F);
														var84.end();
													}
												}
											}

											GL11.glEnable(GL11.GL_CULL_FACE);
											GL11.glDisable(GL11.GL_BLEND);
										}

										if (var82.g != null) {
											var82.g.renderHover(var82.mc.textureManager, var80);
										}

										GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
										GL11.glLoadIdentity();
										if (var82.mc.settings.anaglyph) {
											GL11.glTranslatef(((var77 << 1) - 1) * 0.1F, 0.0F, 0.0F);
										}

										var82.b(var80);
										if (var82.mc.settings.viewBobbing) {
											var82.c(var80);
										}

										var117 = var82.heldBlock.lastPosition + (var82.heldBlock.heldPosition - var82.heldBlock.lastPosition) * var80;
										var116 = var82.heldBlock.mc.player;
										GL11.glPushMatrix();
										GL11.glRotatef(var116.xRotO + (var116.xRot - var116.xRotO) * var80, 1.0F, 0.0F, 0.0F);
										GL11.glRotatef(var116.yRotO + (var116.yRot - var116.yRotO) * var80, 0.0F, 1.0F, 0.0F);
										var82.heldBlock.mc.renderer.a(true);
										GL11.glPopMatrix();
										GL11.glPushMatrix();
										var69 = 0.8F;
										if (var82.heldBlock.moving) {
											var33 = MathHelper.sin((var74 = (var82.heldBlock.heldOffset + var80) / 7.0F) * 3.1415927F);
											GL11.glTranslatef(-MathHelper.sin(MathHelper.sqrt(var74) * 3.1415927F) * 0.4F, MathHelper.sin(MathHelper.sqrt(var74) * 3.1415927F * 2.0F) * 0.2F, -var33 * 0.2F);
										}

										GL11.glTranslatef(0.7F * var69, -0.65F * var69 - (1.0F - var117) * 0.6F, -0.9F * var69);
										GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
										GL11.glEnable(GL11.GL_NORMALIZE);
										if (var82.heldBlock.moving) {
											var33 = MathHelper.sin((var74 = (var82.heldBlock.heldOffset + var80) / 7.0F) * var74 * 3.1415927F);
											GL11.glRotatef(MathHelper.sin(MathHelper.sqrt(var74) * 3.1415927F) * 80.0F, 0.0F, 1.0F, 0.0F);
											GL11.glRotatef(-var33 * 20.0F, 1.0F, 0.0F, 0.0F);
										}

										GL11.glColor4f(var74 = this.level.getBrightness((int) var116.x, (int) var116.y, (int) var116.z), var74, var74, 1.0F);
										com.mojang.minecraft.render.ShapeRenderer var123 = com.mojang.minecraft.render.ShapeRenderer.instance;
										if (var82.heldBlock.block != null) {
											var34 = 0.4F;
											GL11.glScalef(0.4F, var34, var34);
											GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
											GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textureManager.bindTexture("/terrain.png"));
											var82.heldBlock.block.getModel().renderPreview();
										} else {
											var116.bindTexture(this.textureManager);
											GL11.glScalef(1.0F, -1.0F, -1.0F);
											GL11.glTranslatef(0.0F, 0.2F, 0.0F);
											GL11.glRotatef(-120.0F, 0.0F, 0.0F, 1.0F);
											GL11.glScalef(1.0F, 1.0F, 1.0F);
											var34 = 0.0625F;
											ModelRenderer var127;
											if (!(var127 = this.player.getModel().f).i) {
												var127.b(var34);
											}

											GL11.glCallList(var127.list);
										}

										GL11.glDisable(GL11.GL_NORMALIZE);
										GL11.glPopMatrix();
										var82.heldBlock.mc.renderer.a(false);
										if (!var82.mc.settings.anaglyph) {
											break;
										}

										var77++;
									}

									this.hud.render(var65, this.currentScreen != null, var94, var70);
								} else {
									GL11.glViewport(0, 0, this.width, this.height);
									GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
									GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
									GL11.glMatrixMode(GL11.GL_PROJECTION);
									GL11.glLoadIdentity();
									GL11.glMatrixMode(GL11.GL_MODELVIEW);
									GL11.glLoadIdentity();
									var66.a();
								}

								if (this.currentScreen != null) {
									this.currentScreen.render();
								}

								Thread.yield();
								Display.update();
							}
						}

						if (this.settings.limitFPS) {
							Thread.sleep(5L);
						}

						checkGLError("Post render");
						fps++;
					} catch (Exception e) {
						this.setCurrentScreen(new ErrorScreen("Client error", "The game broke! [" + e + "]"));
						e.printStackTrace();
					}

					while (System.currentTimeMillis() >= lastUpdate + 1000) {
						this.debugInfo = fps + " fps, " + com.mojang.minecraft.render.Chunk.chunkUpdates + " chunk updates";
						com.mojang.minecraft.render.Chunk.chunkUpdates = 0;
						lastUpdate += 1000;
						fps = 0;
					}
				}
			}

			return;
		} catch (StopGameException e) {
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			this.shutdown();
		}
	}

	public final void grabMouse() {
		if (!this.hasMouse) {
			this.hasMouse = true;
			if (this.noLevel) {
				try {
					Mouse.setNativeCursor(this.cursor);
					Mouse.setCursorPosition(this.width / 2, this.height / 2);
				} catch (LWJGLException e) {
					e.printStackTrace();
				}

				if (this.canvas == null) {
					this.canvas.requestFocus();
				}
			} else {
				Mouse.setGrabbed(true);
			}

			this.setCurrentScreen(null);
			this.lastClick = this.ticks + 10000;
		}
	}

	public final void displayMenu() {
		if (this.currentScreen == null && this.ingame && (this.netManager == null || this.netManager.isConnected() && this.netManager.levelLoaded)) {
			this.setCurrentScreen(new MenuScreen());
		}
	}

	private void onMouseClick(int button) {
		if (button != 0 || this.blockHitTime <= 0) {
			if (button == 0) {
				this.renderer.heldBlock.heldOffset = -1;
				this.renderer.heldBlock.moving = true;
			}

			int selected = this.player.inventory.getSelected();
			if (button == 1 && selected > 0 && this.mode.useItem(this.player, selected)) {
				this.renderer.heldBlock.heldPosition = 0;
			} else if (this.selected == null) {
				if (button == 0 && !(this.mode instanceof CreativeGameMode)) {
					this.blockHitTime = 10;
				}

			} else {
				if (this.selected.entityPos) {
					if (button == 0) {
						this.selected.entity.hurt(this.player, 4);
						return;
					}
				} else {
					int x = this.selected.x;
					int y = this.selected.y;
					int z = this.selected.z;
					if (button != 0) {
						if (this.selected.side == 0) {
							--y;
						}

						if (this.selected.side == 1) {
							++y;
						}

						if (this.selected.side == 2) {
							--z;
						}

						if (this.selected.side == 3) {
							++z;
						}

						if (this.selected.side == 4) {
							--x;
						}

						if (this.selected.side == 5) {
							++x;
						}
					}

					if (button == 0) {
						if (this.level != null && (Blocks.fromId(this.level.getTile(x, y, z)) != VanillaBlock.BEDROCK || this.player.userType >= 100)) {
							this.mode.hitBlock(x, y, z);
							return;
						}
					} else {
						int id = this.player.inventory.getSelected();
						if (id <= 0) {
							return;
						}

						if(this.player.openclassic.getPlaceMode() > 0) {
							id = this.player.openclassic.getPlaceMode();
						}

						Block block = this.level.openclassic.getBlockAt(x, y, z);
						AABB collision = BlockUtils.getCollisionBox(id, x, y, z);
						if ((block == null || block.getType() == null || block.getType() == VanillaBlock.AIR || block.getType() == VanillaBlock.WATER || block.getType() == VanillaBlock.STATIONARY_WATER || block.getType() == VanillaBlock.LAVA || block.getType() == VanillaBlock.STATIONARY_LAVA) && (collision == null || (!this.player.bb.intersects(collision) && this.level.isFree(collision)))) {
							if (!this.mode.canPlace(id)) {
								return;
							}

							if (this.isConnected()) {
								this.netManager.sendBlockChange(x, y, z, button, id);
							}

							if(this.netManager == null && EventFactory.callEvent(new BlockPlaceEvent(this.level.openclassic.getBlockAt(x, y, z), OpenClassic.getClient().getPlayer(), this.renderer.heldBlock.block)).isCancelled()) {
								return;
							}

							this.level.netSetTile(x, y, z, id);
							this.renderer.heldBlock.heldPosition = 0;
							if(Blocks.fromId(id).getPhysics() != null) {
								Blocks.fromId(id).getPhysics().onPlace(this.level.openclassic.getBlockAt(x, y, z));
							}

							BlockType type = Blocks.fromId(id);
							if (type.getStepSound() != StepSound.NONE) {
								this.level.playSound(type.getStepSound().getSound(), x, y, z, (type.getStepSound().getVolume() + 1.0F) / 2.0F, type.getStepSound().getPitch() * 0.8F);
							}
						}
					}
				}
			}
		}
	}

	private void tick() {
		this.audio.update(this.player);

		if (this.currentScreen != null) {
			this.lastClick = this.ticks + 10000;
		}

		if (this.currentScreen != null) {
			while (Mouse.next()) {
				if (this.currentScreen != null && Mouse.getEventButtonState()) {
					int x = Mouse.getEventX() * this.currentScreen.getWidth() / this.width;
					int y = this.currentScreen.getHeight() - Mouse.getEventY() * this.currentScreen.getHeight() / this.height - 1;
					this.currentScreen.onMouseClick(x, y, Mouse.getEventButton());
				}
			}

			if(this.currentScreen != null) {
				while (Keyboard.next()) {
					if (Keyboard.getEventKeyState()) {
						if(this.currentScreen != null) {
							this.currentScreen.onKeyPress(Keyboard.getEventCharacter(), Keyboard.getEventKey());
						}
					}
				}

				if(this.currentScreen != null) {
					this.currentScreen.update();
				}
			}
		}

		if(!this.ingame) return;

		if (System.currentTimeMillis() > this.audio.lastBGM && this.audio.playMusic("bg")) {
			this.audio.lastBGM = System.currentTimeMillis() + rand.nextInt(900000) + 300000L;
		}

		this.mode.spawnMobs();
		this.hud.ticks++;

		for (int index = 0; index < this.hud.chatHistory.size(); ++index) {
			this.hud.chatHistory.get(index).time++;
		}

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textureManager.bindTexture("/terrain.png"));

		for (int index = 0; index < this.textureManager.animations.size(); index++) {
			AnimatedTexture animation = this.textureManager.animations.get(index);
			animation.anaglyph = this.textureManager.settings.anaglyph;
			animation.animate();

			ByteBuffer buffer = BufferUtils.createByteBuffer(animation.textureData.length);
			buffer.put(animation.textureData);
			buffer.flip();
			GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, animation.textureId % 16 << 4, animation.textureId / 16 << 4, 16, 16, 6408, 5121, buffer);
		}

		if (this.netManager != null && !(this.currentScreen instanceof ErrorScreen)) {
			if (!this.netManager.isConnected()) {
				this.progressBar.setTitle("Connecting...");
				this.progressBar.setProgress(0);
			} else {
				if (this.netManager.successful) {
					if (this.netManager.netHandler.connected) {
						try {
							this.netManager.netHandler.channel.read(this.netManager.netHandler.in);
							int count = 0;

							while (this.netManager.netHandler.in.position() > 0 && count++ != 100) {
								this.netManager.netHandler.in.flip();
								byte packetId = this.netManager.netHandler.in.get(0);
								PacketType type = PacketType.packets[packetId];

								if (type == null) {
									System.out.println("Bad packet: " + packetId);
									continue;
								}

								if (this.netManager.netHandler.in.remaining() < type.length + 1) {
									this.netManager.netHandler.in.compact();
									break;
								}

								this.netManager.netHandler.in.get();
								Object[] params = new Object[type.params.length];

								for (int param = 0; param < params.length; ++param) {
									params[param] = this.netManager.netHandler.recieveData(type.params[param]);
								}

								if (this.netManager.successful) {
									if (type == PacketType.IDENTIFICATION) {
										if(!this.netManager.identified) {
											PlayerLoginEvent event = EventFactory.callEvent(new PlayerLoginEvent(OpenClassic.getClient().getPlayer(), InetSocketAddress.createUnresolved(this.server, this.port)));
											if(event.getResult() != Result.ALLOWED) {
												this.stopGame(false);
												this.setCurrentScreen(new ErrorScreen("Login disallowed by plugin!", event.getKickMessage()));
											}
										}

										this.progressBar.setTitle(params[1].toString());
										this.progressBar.setText(params[2].toString());
										this.player.userType = (Byte) params[3];

										if(!this.netManager.identified) {
											if(params[1].toString().indexOf("+hax") > -1 || params[2].toString().indexOf("+hax") > -1) {
												this.hacks = true;
											} else {
												this.hacks = false;
											}

											if(this.player.userType == Constants.OP && (params[1].toString().indexOf("+ophax") > -1 || params[2].toString().indexOf("+ophax") > -1)) {
												this.hacks = true;
											}

											if(params[1].toString().indexOf("+ctf") > -1 || params[2].toString().indexOf("+ctf") > -1) {
												this.ctf = true;
											}

											for(BlockType block : Blocks.getBlocks()) {
												if(block instanceof CustomBlock) {
													this.clientCache.add((CustomBlock) block);
													Blocks.unregister(block.getId());
												}
											}

											EventFactory.callEvent(new PlayerJoinEvent(OpenClassic.getClient().getPlayer(), "Joined"));
										}

										this.netManager.identified = true;
									} else if (type == PacketType.LEVEL_INIT) {
										this.setLevel(null);
										this.setCurrentScreen(null);
										this.netManager.levelData = new ByteArrayOutputStream();
										this.netManager.levelLoaded = false;
									} else if (type == PacketType.LEVEL_DATA) {
										short length = (Short) params[0];
										byte[] data = (byte[]) params[1];
										byte percent = (Byte) params[2];
										this.progressBar.setProgress(percent);
										this.netManager.levelData.write(data, 0, length);
									} else if (type == PacketType.LEVEL_FINALIZE) {
										try {
											this.netManager.levelData.close();
										} catch (IOException e) {
											e.printStackTrace();
										}

										byte[] processed = LevelIO.processData(new ByteArrayInputStream(this.netManager.levelData.toByteArray()));
										this.netManager.levelData = null;
										short width = (Short) params[0];
										short depth = (Short) params[1];
										short height = (Short) params[2];
										Level level = new Level();
										level.setNetworkMode(true);
										level.setData(width, depth, height, processed);
										this.setLevel(level);
										this.online = false;
										this.netManager.levelLoaded = true;
									} else if (type == PacketType.CLIENT_SET_BLOCK) {
										// Server is OpenClassic
										this.openclassicServer = true;
										this.netManager.netHandler.send(PacketType.CLIENT_INFO, Constants.CLIENT_VERSION);
									} else if (type == PacketType.SET_BLOCK) {
										if (this.level != null) {
											this.level.netSetTile((Short) params[0], (Short) params[1], (Short) params[2], (Byte) params[3]);
										}
									} else if (type == PacketType.SPAWN_PLAYER) {
										byte playerId = (Byte) params[0];
										String name = (String) params[1];
										short x = (Short) params[2];
										short y = (Short) params[3];
										short z = (Short) params[4];
										byte yaw = (Byte) params[5];
										byte pitch = (Byte) params[6];

										if (playerId >= 0) {
											NetworkPlayer player = new NetworkPlayer(this, playerId, name, x, (short) (y - 22), z, ((yaw + 128) * 360) / 256.0F, (pitch * 360) / 256.0F);
											this.netManager.players.put(playerId, player);
											this.level.addEntity(player);
										} else {
											this.level.setSpawnPos(x / 32, y / 32, z / 32, (yaw * 320 / 256));
											this.player.moveTo(x / 32, y / 32, z / 32, (yaw * 360) / 256, (pitch * 360) / 256);
										}
									} else if (type == PacketType.POSITION_ROTATION) {
										byte playerId = (Byte) params[0];
										short x = (Short) params[1];
										short y = (Short) params[2];
										short z = (Short) params[3];
										byte yaw = ((Byte) params[4]).byteValue();
										byte pitch = ((Byte) params[5]).byteValue();
										if (playerId < 0) {
											this.player.moveTo(x / 32.0F, y / 32.0F, z / 32.0F, (yaw * 360) / 256.0F, (pitch * 360) / 256.0F);
										} else {
											NetworkPlayer var61 = this.netManager.players.get(Byte.valueOf(playerId));
											if (var61 != null) {
												var61.teleport(x, (short) (y - 22), z, ((byte) (yaw + 128) * 360) / 256.0F, (pitch * 360) / 256.0F);
											}
										}
									} else if (type == PacketType.POSITION_ROTATION_UPDATE) {
										byte playerId = ((Byte) params[0]).byteValue();
										byte x = ((Byte) params[1]).byteValue();
										byte y = ((Byte) params[2]).byteValue();
										byte z = ((Byte) params[3]).byteValue();
										byte yaw = ((Byte) params[4]).byteValue();
										byte pitch = ((Byte) params[5]).byteValue();
										if (playerId >= 0) {
											NetworkPlayer moving = this.netManager.players.get(Byte.valueOf(playerId));
											if (moving != null) {
												moving.queue(x, y, z, ((byte) (yaw + 128) * 360) / 256.0F, (pitch * 360) / 256.0F);
											}
										}
									} else if (type == PacketType.ROTATION_UPDATE) {
										byte playerId = (Byte) params[0];
										byte yaw = (Byte) params[1];
										byte pitch = (Byte) params[2];
										if (playerId >= 0) {
											NetworkPlayer moving = this.netManager.players.get(Byte.valueOf(playerId));
											if (moving != null) {
												moving.queue(((byte) (yaw + 128) * 360) / 256.0F, (pitch * 360) / 256.0F);
											}
										}
									} else if (type == PacketType.POSITION_UPDATE) {
										byte x = (Byte) params[1];
										byte y = (Byte) params[2];
										byte z = (Byte) params[3];
										byte playerId = (Byte) params[0];
										NetworkPlayer moving = this.netManager.players.get(playerId);
										if (playerId >= 0 && moving != null) {
											moving.queue(x, y, z);
										}
									} else if (type == PacketType.DESPAWN_PLAYER) {
										NetworkPlayer despawning = this.netManager.players.remove(params[0]);
										if ((Byte) params[0] >= 0 && despawning != null) {
											despawning.clear();
											this.level.removeEntity(despawning);
										}
									} else if (type == PacketType.CHAT_MESSAGE) {
										byte id = (Byte) params[0];
										String message = (String) params[1];
										if (id < 0) {
											this.hud.addChat(Color.YELLOW + message);
										} else {
											this.netManager.players.get(Byte.valueOf(id));
											this.hud.addChat(message);
										}
									} else if (type == PacketType.DISCONNECT) {
										EventFactory.callEvent(new PlayerKickEvent(OpenClassic.getClient().getPlayer(), (String) params[0], " disconnected"));
										this.netManager.netHandler.close();
										this.setCurrentScreen((new ErrorScreen("Disconnected by server!", (String) params[0])));
									} else if (type == PacketType.UPDATE_PLAYER_TYPE) {
										this.player.userType = (Byte) params[0];
										// Custom begins
									} else if (type == PacketType.CUSTOM_BLOCK) {
										byte id = (Byte) params[0];
										boolean opaque = (Byte) params[1] == 1;
										boolean selectable = (Byte) params[2] == 1;
										StepSound sound = StepSound.valueOf((String) params[3]);
										boolean liquid = (Byte) params[4] == 1;
										int delay = (Integer) params[5];
										VanillaBlock fallback = (VanillaBlock) Blocks.fromId((Byte) params[6]);
										boolean solid = (Byte) params[7] == 1;

										CustomBlock block = new CustomBlock(id, sound, null, opaque, liquid, selectable);
										block.setTickDelay(delay);
										block.setFallback(fallback);
										block.setSolid(solid);
										Blocks.register(block);

										System.out.println("Got custom block!");
									} else if (type == PacketType.BLOCK_MODEL) {
										byte block = (Byte) params[0];
										String modelType = (String) params[1];
										Model model = modelType.equals("TransparentModel") ? new TransparentModel("/terrain.png", 16) : (modelType.equals("CuboidModel") ? new CuboidModel("/terrain.png", 16, 0, 0, 0, 1, 1, 1) : (modelType.equals("CubeModel") ? new CubeModel("/terrain.png", 16) : new Model()));
										model.clearQuads();

										float x1 = (Float) params[2];
										float x2 = (Float) params[3];
										float y1 = (Float) params[4];
										float y2 = (Float) params[5];
										float z1 = (Float) params[6];
										float z2 = (Float) params[7];
										model.setCollisionBox(new BoundingBox(x1, y1, z1, x2, y2, z2));

										float sx1 = (Float) params[8];
										float sx2 = (Float) params[9];
										float sy1 = (Float) params[10];
										float sy2 = (Float) params[11];
										float sz1 = (Float) params[12];
										float sz2 = (Float) params[13];
										model.setSelectionBox(new BoundingBox(sx1, sy1, sz1, sx2, sy2, sz2));

										((CustomBlock) Blocks.fromId(block)).setModel(model);
										System.out.println("Got custom model!");
									} else if (type == PacketType.QUAD) {
										byte block = (Byte) params[0];
										int id = (Integer) params[1];

										Vertex vertices[] = new Vertex[4];
										for(int vCount = 0; vCount < 4; vCount++) {
											float x = (Float) params[2 + vCount * 3];
											float y = (Float) params[3 + vCount * 3];
											float z = (Float) params[4 + vCount * 3];

											System.out.println("Vertex: " + x + ", " + y + ", " + z);
											vertices[vCount] = new Vertex(x, y, z);
										}

										String texture = (String) params[14];
										boolean jar = (Byte) params[15] == 1;
										int width = (Integer) params[16];
										int height = (Integer) params[17];
										int swidth = (Integer) params[18];
										int sheight = (Integer) params[19];

										if(!jar) {
											File file = new File(this.dir, "cache/" + this.server + "/" + block + ".png");
											if(!file.exists()) {
												if(!file.getParentFile().exists()) {
													file.getParentFile().mkdirs();
												}

												file.createNewFile();

												System.out.println("Downloading " + file.getName());

												byte[] data = new byte[4096];
												DataInputStream in = null;
												DataOutputStream out = null;

												try {
													in = new DataInputStream((new URL(texture)).openStream());
													out = new DataOutputStream(new FileOutputStream(file));

													int length = 0;
													while (this.running) {
														length = in.read(data);
														if (length < 0) return;

														out.write(data, 0, length);
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

												System.out.println("Downloaded " + file.getName());
											}

											texture = file.getPath();
										}

										Texture t = new Texture(texture, jar, width, height, swidth, sheight);
										Quad quad = new Quad(id, t.getSubTexture((Integer) params[20]), vertices[0], vertices[1], vertices[2], vertices[3]);

										Blocks.fromId(block).getModel().addQuad(quad);
										System.out.println("Got quad!");
									}
								}

								if (!this.netManager.netHandler.connected) {
									break;
								}

								this.netManager.netHandler.in.compact();
							}

							if (this.netManager.netHandler.out.position() > 0) {
								this.netManager.netHandler.out.flip();

								if(this.netManager.netHandler.channel != null) {
									this.netManager.netHandler.channel.write(this.netManager.netHandler.out);
								}

								this.netManager.netHandler.out.compact();
							}
						} catch (Exception e) {
							this.setCurrentScreen(new ErrorScreen("Disconnected!", "You\'ve lost connection to the server!"));
							e.printStackTrace();
							this.online = false;
							this.netManager.netHandler.close();
							this.netManager = null;
						}
					}
				}

				if (this.netManager != null && this.netManager.levelLoaded) {
					int x = (int) (this.player.x * 32.0F);
					int y = (int) (this.player.y * 32.0F);
					int z = (int) (this.player.z * 32.0F);
					int yaw = (int) (this.player.yRot * 256.0F / 360.0F) & 255;
					int pitch = (int) (this.player.xRot * 256.0F / 360.0F) & 255;
					this.netManager.netHandler.send(PacketType.POSITION_ROTATION, new Object[] { (byte) -1, (short) x, (short) y, (short) z, (byte) yaw, (byte) pitch });
				}
			}
		}

		if(this.netManager == null && this.clientCache.size() > 0) {
			for(CustomBlock block : this.clientCache) {
				Blocks.register(block);
			}

			this.clientCache.clear();
		}

		if (this.currentScreen == null && this.player != null && this.player.health <= 0) {
			this.setCurrentScreen(null);
		}

		if (this.currentScreen == null || !this.currentScreen.grabsInput()) {
			while (Mouse.next()) {
				if (Mouse.getEventDWheel() != 0) {
					this.player.inventory.swapPaint(Mouse.getEventDWheel());
				}

				if (this.currentScreen == null) {
					if (!this.hasMouse && Mouse.getEventButtonState()) {
						this.grabMouse();
					} else {
						if(Mouse.getEventButtonState()) {
							if (Mouse.getEventButton() == 0) {
								this.onMouseClick(0);
								this.lastClick = this.ticks;
							}

							if (Mouse.getEventButton() == 1) {
								this.onMouseClick(1);
								this.lastClick = this.ticks;
							}

							if (Mouse.getEventButton() == 2 && this.selected != null) {
								int block = this.level.getTile(this.selected.x, this.selected.y, this.selected.z);
								if (block == VanillaBlock.GRASS.getId()) {
									block = VanillaBlock.DIRT.getId();
								}

								if (block == VanillaBlock.DOUBLE_SLAB.getId()) {
									block = VanillaBlock.SLAB.getId();
								}

								if (block == VanillaBlock.BEDROCK.getId()) {
									block = VanillaBlock.STONE.getId();
								}

								this.player.inventory.grabTexture(block, this.mode instanceof CreativeGameMode);
							}
						}
					}
				}

				if (this.currentScreen != null) {
					if (Mouse.getEventButtonState()) {
						int x = Mouse.getEventX() * this.currentScreen.getWidth() / this.width;
						int y = this.currentScreen.getHeight() - Mouse.getEventY() * this.currentScreen.getHeight() / this.height - 1;
						this.currentScreen.onMouseClick(x, y, Mouse.getEventButton());
					}
				}
			}

			if (this.blockHitTime > 0) {
				this.blockHitTime--;
			}

			while (Keyboard.next()) {
				this.player.setKey(Keyboard.getEventKey(), Keyboard.getEventKeyState());
				if (Keyboard.getEventKeyState()) {
					if (this.currentScreen != null) {
						if (Keyboard.getEventKeyState()) {
							this.currentScreen.onKeyPress(Keyboard.getEventCharacter(), Keyboard.getEventKey());
						}
					}

					if (this.ingame && (this.netManager == null || this.netManager.isConnected() && this.netManager.levelLoaded)) {
						if(Keyboard.getEventKey() == Keyboard.KEY_F2) {
							GL11.glReadBuffer(GL11.GL_FRONT);

							int width = Display.getWidth();
							int height = Display.getHeight();
							ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
							GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

							File file = new File(this.dir, "screenshots/" + (new Date(System.currentTimeMillis()).toString().replaceAll(" ", "-").replaceAll(":", "-")) + ".png");
							BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

							for(int x = 0; x < width; x++) {
								for(int y = 0; y < height; y++)
								{
									int i = (x + (width * y)) * 4;
									int r = buffer.get(i) & 0xFF;
									int g = buffer.get(i + 1) & 0xFF;
									int b = buffer.get(i + 2) & 0xFF;
									image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
								}
							}

							try {
								ImageIO.write(image, "PNG", file);
								if(this.hud != null) this.hud.addChat(Color.GREEN + "Saved screenshot \"" + file.getName() + "\"!");
							} catch (IOException e) {
								e.printStackTrace();
								if(this.hud != null) this.hud.addChat(Color.RED + "Error saving screenshot: " + e.getMessage() + "!");
							}
						}

						if(this.currentScreen == null) {
							if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
								this.displayMenu();
							}

							if (this.mode instanceof CreativeGameMode) {
								if (Keyboard.getEventKey() == this.settings.loadLocKey.key && !this.ctf) {
									PlayerRespawnEvent event = new PlayerRespawnEvent(OpenClassic.getClient().getPlayer(), new Position(OpenClassic.getClient().getLevel(), this.level.xSpawn + 0.5F, this.level.ySpawn, this.level.zSpawn + 0.5F, (byte) this.level.rotSpawn, (byte) 0));
									if(!event.isCancelled()) {
										this.player.resetPos(event.getPosition());
									}
								}

								if (Keyboard.getEventKey() == this.settings.saveLocKey.key && !this.ctf) {
									this.level.setSpawnPos((int) this.player.x, (int) this.player.y, (int) this.player.z, this.player.yRot);
									this.player.resetPos();
								}
							}

							Keyboard.getEventKey();
							if (Keyboard.getEventKey() == Keyboard.KEY_F5) {
								this.raining = !this.raining;
							}

							if (Keyboard.getEventKey() == Keyboard.KEY_TAB && this.mode instanceof SurvivalGameMode && this.player.arrows > 0) {
								this.level.addEntity(new Arrow(this.level, this.player, this.player.x, this.player.y, this.player.z, this.player.yRot, this.player.xRot, 1.2F));
								--this.player.arrows;
							}

							if (Keyboard.getEventKey() == this.settings.buildKey.key) {
								this.mode.openInventory();
							}

							if (Keyboard.getEventKey() == this.settings.chatKey.key) {
								this.player.releaseAllKeys();
								this.setCurrentScreen(new ChatInputScreen());
							}
						}
					}

					for (int selection = 0; selection < 9; ++selection) {
						if (Keyboard.getEventKey() == selection + 2) {
							this.player.inventory.selected = selection;
						}
					}

					if (Keyboard.getEventKey() == this.settings.fogKey.key) {
						this.settings.toggleSetting(4, !Keyboard.isKeyDown(42) && !Keyboard.isKeyDown(54) ? 1 : -1);
					}
					
					EventFactory.callEvent(new PlayerKeyChangeEvent(OpenClassic.getClient().getPlayer(), Keyboard.getEventKey(), Keyboard.isKeyDown(Keyboard.getEventKey())));
					if(this.netManager != null && this.netManager.isConnected() && this.openclassicServer) {
						this.netManager.netHandler.send(PacketType.KEY_CHANGE, Keyboard.getEventKey(), Keyboard.isKeyDown(Keyboard.getEventKey()) ? (byte) 1 : (byte) 0);
					}
				}
			}

			if (this.currentScreen == null) {
				if (Mouse.isButtonDown(0) && (this.ticks - this.lastClick) >= this.timer.a / 4 && this.hasMouse) {
					this.onMouseClick(0);
					this.lastClick = this.ticks;
				}

				if (Mouse.isButtonDown(1) && (this.ticks - this.lastClick) >= this.timer.a / 4 && this.hasMouse) {
					this.onMouseClick(1);
					this.lastClick = this.ticks;
				}
			}

			if (!this.mode.creative && this.blockHitTime <= 0) {
				if (this.currentScreen == null && Mouse.isButtonDown(0) && this.hasMouse && this.selected != null && !this.selected.entityPos) {
					this.mode.hitBlock(this.selected.x, this.selected.y, this.selected.z, this.selected.side);
				} else {
					this.mode.resetHits();
				}
			}
		}

		if (this.level != null) {
			this.renderer.levelTicks++;
			this.renderer.heldBlock.lastPosition = this.renderer.heldBlock.heldPosition;
			if (this.renderer.heldBlock.moving) {
				this.renderer.heldBlock.heldOffset++;
				if (this.renderer.heldBlock.heldOffset == 7) {
					this.renderer.heldBlock.heldOffset = 0;
					this.renderer.heldBlock.moving = false;
				}
			}

			int id = this.player.inventory.getSelected();
			BlockType block = null;
			if (id > 0) {
				block = Blocks.fromId(id);
			}

			block = this.player.openclassic != null && this.player.openclassic.getPlaceMode() != 0 ? Blocks.fromId(this.player.openclassic.getPlaceMode()) : block;

			float position = (block == this.renderer.heldBlock.block ? 1.0F : 0.0F) - this.renderer.heldBlock.heldPosition;
			if (position < -0.4F) {
				position = -0.4F;
			}

			if (position > 0.4F) {
				position = 0.4F;
			}

			this.renderer.heldBlock.heldPosition += position;
			if (this.renderer.heldBlock.heldPosition < 0.1F) {
				this.renderer.heldBlock.block = block;
			}

			if (this.raining) {
				for (int count = 0; count < 50; ++count) {
					int x = (int) this.player.x + this.renderer.rand.nextInt(9) - 4;
					int z = (int) this.player.z + this.renderer.rand.nextInt(9) - 4;
					int y = this.level.getHighestTile(x, z);
					if (y <= (int) this.player.y + 4 && y >= (int) this.player.y - 4) {
						float xOffset = this.renderer.rand.nextFloat();
						float zOffset = this.renderer.rand.nextFloat();
						this.particleManager.spawnParticle(new WaterDropParticle(this.level, x + xOffset, y + 0.1F, z + zOffset));
					}
				}
			}

			this.levelRenderer.ticks++;
			this.level.tickEntities();
			if (!this.isConnected()) {
				this.level.tick();
			}

			this.particleManager.tickParticles();
		}

		for(Plugin plugin : OpenClassic.getClient().getPluginManager().getPlugins()) {
			plugin.tick();
		}
	}

	public final boolean isConnected() {
		return this.netManager != null;
	}

	public final void setLevel(Level level) {
		if (this.applet == null || !this.applet.getDocumentBase().getHost().equalsIgnoreCase("minecraft.net") && !this.applet.getDocumentBase().getHost().equalsIgnoreCase("www.minecraft.net") || !this.applet.getCodeBase().getHost().equalsIgnoreCase("minecraft.net") && !this.applet.getCodeBase().getHost().equalsIgnoreCase("www.minecraft.net")) {
			level = null;
		}

		this.level = level;
		if (level != null) {
			level.initTransient();
			this.mode.apply(level);
			level.font = this.fontRenderer;
			level.rendererContext = this;
			if (!this.isConnected()) {
				this.player = (Player) level.findSubclassOf(Player.class);
				if(this.player != null && this.player.openclassic == null) this.player.openclassic = new ClientPlayer(this.player);
			} else if (this.player != null) {
				this.player.resetPos();
				this.mode.preparePlayer(this.player);
				level.player = this.player;
				level.addEntity(this.player);
			}
		}

		if (this.player == null) {
			this.player = new Player(level);
			this.player.resetPos();
			this.mode.preparePlayer(this.player);
			if (level != null) {
				level.player = this.player;
			}
		}

		if (this.player != null) {
			this.player.input = new InputHandler(this.settings);
			this.mode.apply(this.player);
		}

		if (this.levelRenderer != null) {
			if (this.levelRenderer.level != null) {
				this.levelRenderer.level.removeListener(this.levelRenderer);
			}

			this.levelRenderer.level = level;
			if (level != null) {
				level.addListener(this.levelRenderer);
				this.levelRenderer.refresh();
			}
		}

		if (this.particleManager != null) {
			if (level != null) {
				level.particleEngine = this.particleManager;
			}

			for (int particle = 0; particle < 2; ++particle) {
				this.particleManager.particles[particle].clear();
			}
		}

		System.gc();
	}

	public enum OS {
		linux("linux", "unix"),
		solaris("solaris", "sunos"),
		windows("win"),
		macos("mac"),
		unknown("unknown");

		private String names[];

		private OS(String... names) {
			this.names = names;
		}

		public static OS lookup(String name) {
			for(OS os : values()) {
				for(String curr : os.names) {
					if(name.contains(curr)) return os;
				}
			}

			return unknown;
		}
	}
}
