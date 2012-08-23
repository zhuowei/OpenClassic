package ch.spacebase.openclassic.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.level.generator.LevelGenerator;

import ch.spacebase.openclassic.api.Client;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.ProgressBar;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.StepSound;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.block.custom.CustomBlock;
import ch.spacebase.openclassic.api.block.model.CubeModel;
import ch.spacebase.openclassic.api.block.model.Texture;
import ch.spacebase.openclassic.api.block.physics.FallingBlockPhysics;
import ch.spacebase.openclassic.api.block.physics.FlowerPhysics;
import ch.spacebase.openclassic.api.block.physics.GrassPhysics;
import ch.spacebase.openclassic.api.block.physics.HalfStepPhysics;
import ch.spacebase.openclassic.api.block.physics.LiquidPhysics;
import ch.spacebase.openclassic.api.block.physics.MushroomPhysics;
import ch.spacebase.openclassic.api.block.physics.SaplingPhysics;
import ch.spacebase.openclassic.api.block.physics.SpongePhysics;
import ch.spacebase.openclassic.api.data.NBTData;
import ch.spacebase.openclassic.api.event.EventFactory;
import ch.spacebase.openclassic.api.event.level.LevelCreateEvent;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.MainScreen;
import ch.spacebase.openclassic.api.input.InputHelper;
import ch.spacebase.openclassic.api.level.Level;
import ch.spacebase.openclassic.api.level.LevelInfo;
import ch.spacebase.openclassic.api.level.generator.FlatLandGenerator;
import ch.spacebase.openclassic.api.level.generator.Generator;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.api.plugin.PluginManager.LoadOrder;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.api.sound.AudioManager;
import ch.spacebase.openclassic.api.util.Constants;
import ch.spacebase.openclassic.client.block.physics.TNTPhysics;
import ch.spacebase.openclassic.client.command.ClientCommands;
import ch.spacebase.openclassic.client.input.ClientInputHelper;
import ch.spacebase.openclassic.client.render.ClientRenderHelper;
import ch.spacebase.openclassic.client.util.GeneralUtils;
import ch.spacebase.openclassic.game.ClassicGame;

public class ClassicClient extends ClassicGame implements Client {
	
	private final Minecraft mc;
	
	public ClassicClient(Minecraft mc) {
		super(GeneralUtils.getMinecraftDirectory());
		RenderHelper.setHelper(new ClientRenderHelper());
		InputHelper.setHelper(new ClientInputHelper());
		this.mc = mc;
	}
	
	public void init() {
		ConsoleHandler console = new ConsoleHandler();
		console.setFormatter(new DateOutputFormatter(new SimpleDateFormat("HH:mm:ss")));

		Logger logger = Logger.getLogger("");
		for (Handler handler : logger.getHandlers()) {
			logger.removeHandler(handler);
		}

		logger.addHandler(console);
		
		try {
			FileHandler handler = new FileHandler(this.getDirectory().getPath() + "/client.log");
			handler.setFormatter(new DateOutputFormatter(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")));
			OpenClassic.getLogger().addHandler(handler);
		} catch(IOException e) {
			OpenClassic.getLogger().severe(this.getTranslator().translate("log.create-fail"));
			e.printStackTrace();
		}

		OpenClassic.getLogger().info(String.format(this.getTranslator().translate("client.startup"), Constants.CLIENT_VERSION));
		
		this.registerExecutor(null, new ClientCommands());
		this.registerGenerator("normal", new LevelGenerator());
		this.registerGenerator("flat", new FlatLandGenerator());
		
		VanillaBlock.SAND.setPhysics(new FallingBlockPhysics((byte) 12));
		VanillaBlock.GRAVEL.setPhysics(new FallingBlockPhysics((byte) 13));
		VanillaBlock.ROSE.setPhysics(new FlowerPhysics());
		VanillaBlock.DANDELION.setPhysics(new FlowerPhysics());
		VanillaBlock.GRASS.setPhysics(new GrassPhysics());
		VanillaBlock.WATER.setPhysics(new LiquidPhysics((byte) 8));
		VanillaBlock.LAVA.setPhysics(new LiquidPhysics((byte) 10));
		VanillaBlock.RED_MUSHROOM.setPhysics(new MushroomPhysics());
		VanillaBlock.BROWN_MUSHROOM.setPhysics(new MushroomPhysics());
		VanillaBlock.SAPLING.setPhysics(new SaplingPhysics());
		VanillaBlock.SPONGE.setPhysics(new SpongePhysics());
		VanillaBlock.SLAB.setPhysics(new HalfStepPhysics());
		VanillaBlock.TNT.setPhysics(new TNTPhysics());
		
		Blocks.register(new CustomBlock((byte) 50, StepSound.STONE, new CubeModel(new Texture("/rock.png", true, 16, 16), 0)));
		
		this.getPluginManager().loadPlugins(LoadOrder.PREWORLD);
		this.getPluginManager().loadPlugins(LoadOrder.POSTWORLD);
	}

	@Override
	public void shutdown() {
		this.mc.shutdown();
	}

	@Override
	public Level createLevel(LevelInfo info, Generator generator) {
		if(generator instanceof LevelGenerator) {
			((LevelGenerator) generator).setInfo(info.getName(), this.mc.data != null ? this.mc.data.username : "unknown", info.getWidth(), info.getHeight(), info.getDepth());
		}
		
		com.mojang.minecraft.level.Level level = new com.mojang.minecraft.level.Level();
		level.name = info.getName();
		level.creator = this.mc.data != null ? this.mc.data.username : "unknown";
		level.createTime = System.currentTimeMillis();
		byte[] data = new byte[info.getWidth() * info.getHeight() * info.getDepth()];
		level.setData(info.getWidth(), info.getHeight(), info.getDepth(), data);
		generator.generate(level.openclassic, data);
		level.setData(info.getWidth(), info.getHeight(), info.getDepth(), data);
				
		if(level.openclassic.getSpawn() == null) {
			level.openclassic.setSpawn(generator.findSpawn(level.openclassic));
		}

		if(info.getSpawn() != null) level.openclassic.setSpawn(info.getSpawn());
		level.openclassic.data = new NBTData(level.name);
		level.openclassic.data.load(OpenClassic.getGame().getDirectory().getPath() + "/levels/" + level.name + ".nbt");

		this.mc.mode.prepareLevel(level);
		this.mc.setLevel(level);
		EventFactory.callEvent(new LevelCreateEvent(level.openclassic));
		return level.openclassic;
	}

	@Override
	public boolean isRunning() {
		return this.mc.running;
	}

	@Override
	public Player getPlayer() {
		return this.mc.player.openclassic;
	}

	@Override
	public Level getLevel() {
		if(this.mc.level == null) return null;
		return this.mc.level.openclassic;
	}

	@Override
	public Level openLevel(String name) {
		if(this.mc.level != null && this.mc.level.name.equals(name)) return this.mc.level.openclassic;
		return this.mc.levelIo.load(name).openclassic;
	}

	@Override
	public void saveLevel() {
		if(this.mc.level == null) return;
		this.mc.levelIo.save(this.mc.level);
	}
	
	@Override
	public void exitLevel() {
		this.exitLevel(true);
	}
	
	@Override
	public void exitLevel(boolean save) {
		if(this.mc.level == null) return;
		
		if(save) this.saveLevel();
		this.mc.stopGame(true);
	}

	@Override
	public AudioManager getAudioManager() {
		return this.mc.audio;
	}
	
	public Minecraft getMinecraft() {
		return this.mc;
	}

	@Override
	public void setCurrentScreen(GuiScreen screen) {
		this.mc.setCurrentScreen(screen);
	}

	@Override
	public GuiScreen getCurrentScreen() {
		return this.mc.currentScreen;
	}

	@Override
	public boolean isInGame() {
		return this.mc.ingame;
	}

	@Override
	public MainScreen getMainScreen() {
		return this.mc.hud;
	}

	@Override
	public boolean isConnectedToOpenClassic() {
		return this.mc.openclassicServer;
	}

	@Override
	public ProgressBar getProgressBar() {
		return this.mc.progressBar;
	}

	@Override
	public String getServerVersion() {
		return this.mc.openclassicVersion;
	}
	
	private static class DateOutputFormatter extends Formatter {
		private final SimpleDateFormat date;

		public DateOutputFormatter(SimpleDateFormat date) {
			this.date = date;
		}

		@Override
		public String format(LogRecord record) {
			StringBuilder builder = new StringBuilder();

			builder.append(date.format(record.getMillis()));
			builder.append(" [");
			builder.append(record.getLevel().getLocalizedName().toUpperCase());
			builder.append("] ");
			builder.append(formatMessage(record));
			builder.append('\n');

			if (record.getThrown() != null) {
				StringWriter writer = new StringWriter();
				record.getThrown().printStackTrace(new PrintWriter(writer));
				builder.append(writer.toString());
			}

			return builder.toString();
		}
	}

}
