package com.mojang.minecraft.render.animation;

public class AnimatedTexture {

	public byte[] textureData = new byte[1024];
	public int textureId;
	public boolean anaglyph = false;
	protected float[] red = new float[256];
	protected float[] green = new float[256];
	protected float[] blue = new float[256];
	protected float[] alpha = new float[256];

	public AnimatedTexture(int textureId) {
		this.textureId = textureId;
	}

	public void animate() {
	}
}
