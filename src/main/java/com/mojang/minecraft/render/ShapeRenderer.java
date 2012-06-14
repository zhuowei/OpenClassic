package com.mojang.minecraft.render;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public final class ShapeRenderer {

	private float[] data = new float[524288];
	private int length = 0;
	private float texX;
	private float texY;
	private float red;
	private float green;
	private float blue;
	private boolean colors = false;
	private boolean textureCoords = false;
	private int extra = 3;
	private int dataLength = 0;
	private boolean colorLock = false;
	public static ShapeRenderer instance = new ShapeRenderer();

	public final void draw() {
		if (this.length > 0) {
			FloatBuffer buffer = BufferUtils.createFloatBuffer(this.dataLength);
			buffer.put(this.data, 0, this.dataLength);
			buffer.flip();
			
			if (this.textureCoords && this.colors) {
				GL11.glInterleavedArrays(GL11.GL_T2F_C3F_V3F, 0, buffer);
			} else if (this.textureCoords) {
				GL11.glInterleavedArrays(GL11.GL_T2F_V3F, 0, buffer);
			} else if (this.colors) {
				GL11.glInterleavedArrays(GL11.GL_C3F_V3F, 0, buffer);
			} else {
				GL11.glInterleavedArrays(GL11.GL_V3F, 0, buffer);
			}

			GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
			if (this.textureCoords) {
				GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
			}

			if (this.colors) {
				GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
			}
			
			GL11.glDrawArrays(GL11.GL_QUADS, 0, this.length);
			GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
			if (this.textureCoords) {
				GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
			}

			if (this.colors) {
				GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
			}
		}

		this.clear();
	}

	private void clear() {
		this.length = 0;
		this.dataLength = 0;
	}

	public final void reset() {
		this.clear();
		this.colors = false;
		this.textureCoords = false;
		this.colorLock = false;
	}

	public final void addColor(float red, float green, float blue) {
		if (!this.colorLock) {
			if (!this.colors) {
				this.extra += 3;
			}

			this.colors = true;
			this.red = red;
			this.green = green;
			this.blue = blue;
		}
	}

	public final void addTexturedPoint(float x, float y, float z, float textureX, float textureY) {
		if (!this.textureCoords) {
			this.extra += 2;
		}

		this.textureCoords = true;
		this.texX = textureX;
		this.texY = textureY;
		this.addPoint(x, y, z);
	}

	public final void addPoint(float x, float y, float z) {
		if (this.textureCoords) {
			this.data[this.dataLength++] = this.texX;
			this.data[this.dataLength++] = this.texY;
		}

		if (this.colors) {
			this.data[this.dataLength++] = this.red;
			this.data[this.dataLength++] = this.green;
			this.data[this.dataLength++] = this.blue;
		}

		this.data[this.dataLength++] = x;
		this.data[this.dataLength++] = y;
		this.data[this.dataLength++] = z;
		this.length++;
		if (this.length % 4 == 0 && this.dataLength >= 524288 - (this.extra << 2)) {
			this.draw();
		}
	}

	public final void addColor(int color) {
		byte red = (byte) (color >> 16 & 255);
		byte green = (byte) (color >> 8 & 255);
		byte blue = (byte) (color & 255);
		
		this.addColor((red & 255) / 255.0F, (green & 255) / 255.0F, (blue & 255) / 255.0F);
	}

	public final void lockColorSetting() {
		this.colorLock = true;
	}

	public final void glNormal3f(float nx, float ny, float nz) {
		GL11.glNormal3f(nx, ny, nz);
	}

}
