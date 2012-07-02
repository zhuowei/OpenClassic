package com.mojang.minecraft.render;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public final class ShapeRenderer {

	private float[] data = new float[524288];
	private int vertices = 0;
	private float u;
	private float v;
	private float r;
	private float g;
	private float b;
	private boolean colors = false;
	private boolean textures = false;
	private int length = 0;
	private boolean noColor = false;
	public static final ShapeRenderer instance = new ShapeRenderer();

	public final void end() {
		if (this.vertices > 0) {
			FloatBuffer buffer = BufferUtils.createFloatBuffer(this.length);
			buffer.put(this.data, 0, this.length);
			buffer.flip();
			
			if (this.textures && this.colors) {
				GL11.glInterleavedArrays(GL11.GL_T2F_C3F_V3F, 0, buffer);
			} else if (this.textures) {
				GL11.glInterleavedArrays(GL11.GL_T2F_V3F, 0, buffer);
			} else if (this.colors) {
				GL11.glInterleavedArrays(GL11.GL_C3F_V3F, 0, buffer);
			} else {
				GL11.glInterleavedArrays(GL11.GL_V3F, 0, buffer);
			}

			GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
			if (this.textures) {
				GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
			}

			if (this.colors) {
				GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
			}
			
			GL11.glDrawArrays(GL11.GL_QUADS, 0, this.vertices);
			GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
			if (this.textures) {
				GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
			}

			if (this.colors) {
				GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
			}
		}

		this.clear();
	}

	private void clear() {
		this.vertices = 0;
		this.length = 0;
	}

	public final void begin() {
		this.clear();
		this.colors = false;
		this.textures = false;
		this.noColor = false;
	}

	public final void color(float r, float g, float b) {
		if (!this.noColor) {
			this.colors = true;
			this.r = r;
			this.g = g;
			this.b = b;
		}
	}

	public final void vertexUV(float x, float y, float z, float u, float v) {
		this.textures = true;
		this.u = u;
		this.v = v;
		this.vertex(x, y, z);
	}

	public final void vertex(float x, float y, float z) {
		if (this.textures) {
			this.data[this.length++] = this.u;
			this.data[this.length++] = this.v;
		}

		if (this.colors) {
			this.data[this.length++] = this.r;
			this.data[this.length++] = this.g;
			this.data[this.length++] = this.b;
		}

		this.data[this.length++] = x;
		this.data[this.length++] = y;
		this.data[this.length++] = z;
		this.vertices++;
	}

	public final void color(int color) {
		byte red = (byte) (color >> 16 & 255);
		byte green = (byte) (color >> 8 & 255);
		byte blue = (byte) (color & 255);
		
		this.color((red & 255) / 255.0F, (green & 255) / 255.0F, (blue & 255) / 255.0F);
	}

	public final void noColor() {
		this.noColor = true;
	}

	public final void glNormal3f(float nx, float ny, float nz) {
		GL11.glNormal3f(nx, ny, nz);
	}

}
