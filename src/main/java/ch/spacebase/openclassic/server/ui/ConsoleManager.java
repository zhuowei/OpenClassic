package ch.spacebase.openclassic.server.ui;

public interface ConsoleManager {

	public void stop();
	
	public void setup();
	
	public String formatOutput(String message);
	
}
