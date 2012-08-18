package ch.spacebase.openclassic.server.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.command.Console;
import ch.spacebase.openclassic.server.ClassicServer;


import jline.ConsoleOperations;
import jline.ConsoleReader;

public class TextConsoleManager implements ConsoleManager {

	public static final Console SENDER = new Console();
	
	private ConsoleReader reader;
	private ConsoleCommandThread thread;
	private final OpenClassicConsoleHandler consoleHandler;
	private FileHandler fileHandler = null;
	
	private boolean running = true;

	public TextConsoleManager() {
		this.consoleHandler = new OpenClassicConsoleHandler();

		try {
			this.fileHandler = new FileHandler(OpenClassic.getGame().getDirectory() + "/server.log");
		} catch(IOException e) {
			OpenClassic.getLogger().severe("Failed to create log file handler!");
			e.printStackTrace();
		}

		this.consoleHandler.setFormatter(new DateOutputFormatter(new SimpleDateFormat("HH:mm:ss")));
		
		if(this.fileHandler != null) {
			this.fileHandler.setFormatter(new DateOutputFormatter(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")));
		}

		Logger logger = Logger.getLogger("");

		for (Handler handler : logger.getHandlers()) {
			logger.removeHandler(handler);
		}

		logger.addHandler(this.consoleHandler);
		
		if(this.fileHandler != null) {
			logger.addHandler(this.fileHandler);
		}

		try {
			this.reader = new ConsoleReader();
		} catch (IOException e) {
			OpenClassic.getLogger().severe("Exception while initializing console manager.");
			e.printStackTrace();
		}

		Runtime.getRuntime().addShutdownHook(new Thread(new ServerShutdownHandler()));
		
		System.setOut(new PrintStream(new LoggerOutputStream(Level.INFO), true));
		System.setErr(new PrintStream(new LoggerOutputStream(Level.SEVERE), true));
	}
	
	public void stop() {
		this.consoleHandler.flush();
		this.fileHandler.flush();
		this.fileHandler.close();
		this.running = false;
	}

	public void setup() {
		this.thread = new ConsoleCommandThread();
		this.thread.setDaemon(true);
		this.thread.start();
	}

	public String formatOutput(String message) {
		if (!message.contains(Color.COLOR_CHARACTER.toString())) {
			return message;
		} else {
			return message.replace(Color.RED.toString(), "\033[1;31m").replace(Color.YELLOW.toString(), "\033[1;33m").replace(Color.GREEN.toString(), "\033[1;32m").replace(Color.AQUA.toString(), "\033[1;36m").replace(Color.BLUE.toString(), "\033[1;34m").replace(Color.PINK.toString(), "\033[1;35m").replace(Color.BLACK.toString(), "\033[0;0m").replace(Color.DARK_GRAY.toString(), "\033[1;30m").replace(Color.DARK_RED.toString(), "\033[0;31m").replace(Color.GOLD.toString(), "\033[0;33m").replace(Color.DARK_GREEN.toString(), "\033[0;32m").replace(Color.CYAN.toString(), "\033[0;36m").replace(Color.DARK_BLUE.toString(), "\033[0;34m").replace(Color.PURPLE.toString(), "\033[0;35m").replace(Color.GRAY.toString(), "\033[0;37m").replace(Color.WHITE.toString(), "\033[1;37m") + "\033[0m";
		}
	}
	
	private class ConsoleCommandThread extends Thread {
		@Override
		public void run() {
			String command;
			while (running) {
				try {
					command = reader.readLine(">", null);

					if (command == null || command.trim().length() == 0) {
						continue;
					}

					((ClassicServer) OpenClassic.getGame()).processCommand(SENDER, command);
				} catch (Exception ex) {
					OpenClassic.getLogger().severe("Exception while executing command: " + ex.getMessage());
					ex.printStackTrace();
				}
			}
		}
	}

	private class OpenClassicConsoleHandler extends ConsoleHandler {
		@Override
		public synchronized void flush() {
			try {
				reader.printString(ConsoleOperations.RESET_LINE + "");
				reader.flushConsole();
				
				super.flush();
				
				try {
					reader.drawLine();
				} catch (Throwable ex) {
					reader.getCursorBuffer().clearBuffer();
				}
				
				reader.flushConsole();
			} catch (IOException ex) {
				OpenClassic.getLogger().severe("Exception flushing console output");
				ex.printStackTrace();
			}
		}
	}

	private class DateOutputFormatter extends Formatter {
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
			builder.append(formatOutput(formatMessage(record)));
			builder.append('\n');

			if (record.getThrown() != null) {
				StringWriter writer = new StringWriter();
				record.getThrown().printStackTrace(new PrintWriter(writer));
				builder.append(writer.toString());
			}

			return builder.toString();
		}
	}

	private class LoggerOutputStream extends ByteArrayOutputStream {
		private final String separator = System.getProperty("line.separator");
		private final Level level;

		public LoggerOutputStream(Level level) {
			super();
			this.level = level;
		}

		@Override
		public synchronized void flush() throws IOException {
			super.flush();

			String record = this.toString();

			super.reset();

			if (record.length() > 0 && !record.equals(separator)) {
				OpenClassic.getLogger().logp(level, "LoggerOutputStream", "log" + level, record);
			}
		}
	}
	
	private class ServerShutdownHandler implements Runnable {
		@Override
		public void run() {
			OpenClassic.getGame().shutdown();
		}
	}

}
