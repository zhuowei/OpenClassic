package com.mojang.minecraft.render.animation;

public class AnimatedTexture {

	public byte[] textureData = new byte[1024];
	public int textureId;
	public boolean anaglyph = false;

	public AnimatedTexture(int textureId) {
		this.textureId = textureId;
	}

	public void animate() {
	}
}
