package com.mojang.minecraft.player;

import ch.spacebase.openclassic.api.input.Keyboard;

import com.mojang.minecraft.GameSettings;
import com.mojang.minecraft.player.InputHandler;

public final class InputHandler {

	public float xxa = 0;
	public float yya = 0;
	public boolean jumping = false;
	public boolean speed = false;
	
	private boolean[] keyStates = new boolean[10];
	private GameSettings settings;

	public InputHandler(GameSettings settings) {
		this.settings = settings;
	}

	public final void setKeyState(int key, boolean pressed) {
		byte index = -1;
		
		if (key == this.settings.forwardKey.key) {
			index = 0;
		}

		if (key == this.settings.backKey.key) {
			index = 1;
		}

		if (key == this.settings.leftKey.key) {
			index = 2;
		}

		if (key == this.settings.rightKey.key) {
			index = 3;
		}

		if (key == this.settings.jumpKey.key) {
			index = 4;
		}
		
		if (key == Keyboard.KEY_LSHIFT) {
			index = 5;
		}

		if (index >= 0) {
			this.keyStates[index] = pressed;
		}
	}

	public final void resetKeys() {
		for (int index = 0; index < 10; ++index) {
			this.keyStates[index] = false;
		}
	}

	public final void updateMovement() {
		this.xxa = 0;
		this.yya = 0;
		
		if (this.keyStates[0]) {
			this.yya--;
		}

		if (this.keyStates[1]) {
			this.yya++;
		}

		if (this.keyStates[2]) {
			this.xxa--;
		}

		if (this.keyStates[3]) {
			this.xxa++;
		}

		this.jumping = this.keyStates[4];
		this.speed = this.keyStates[5];
	}
}
