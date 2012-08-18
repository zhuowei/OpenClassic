package ch.spacebase.openclassic.server.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.client.gui.ConsoleScreen;

public class EmbeddedConsoleManager implements ConsoleManager {

	private List<String> log = new ArrayList<String>();
	
	@Override
	public void stop() {
		ConsoleScreen.get().setHandler(null);
	}

	@Override
	public void setup() {
		if(OpenClassic.getClient() == null) return;
		ConsoleScreen.get().setHandler(this);
		OpenClassic.getLogger().addHandler(new OutputHandler());
	}

	@Override
	public String formatOutput(String message) {
		return message;
	}
	
	public void send(String message) {
		OpenClassic.getServer().processCommand(TextConsoleManager.SENDER, message.trim());
	}

	private void addLine(String line) {
		this.log.add(line);

		while (this.log.size() > 23) {
			this.log.remove(0);
		}
	}
	
	public List<String> getLog() {
		return this.log;
	}
	
	private class OutputHandler extends Handler {
		public synchronized void publish(LogRecord record) {
			if (!this.isLoggable(record))
				return;

			addLine(record.getMessage());
		}

		public void close() {
		}

		public void flush() {
		}
	}

}
