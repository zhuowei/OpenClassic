package ch.spacebase.openclassic.client.input;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import ch.spacebase.openclassic.api.input.InputHelper;
import ch.spacebase.openclassic.client.util.GeneralUtils;

public class ClientInputHelper extends InputHelper {

	@Override
	public boolean isMouseButtonDown(int button) {
		return Mouse.isButtonDown(button);
	}

	@Override
	public boolean isKeyDown(int key) {
		return Keyboard.isKeyDown(key);
	}

	@Override
	public int getMouseX() {
		return Mouse.getX();
	}

	@Override
	public int getMouseY() {
		return Mouse.getY();
	}

	@Override
	public void grabMouse() {
		GeneralUtils.getMinecraft().grabMouse();
	}
	
}
