package com.mojang.minecraft.render;

import com.mojang.util.MathHelper;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public final class ClippingHelper {

	private static ClippingHelper instance = new ClippingHelper();
	
	public float[][] data = new float[16][16];
	public float[] widthData = new float[16];
	public float[] depthData = new float[16];
	public float[] d = new float[16];
	
	private FloatBuffer widthBuffer = BufferUtils.createFloatBuffer(16);
	private FloatBuffer depthBuffer = BufferUtils.createFloatBuffer(16);

	public static ClippingHelper prepare() {
		instance.widthBuffer.clear();
		instance.depthBuffer.clear();
		GL11.glGetFloat(2983, instance.widthBuffer);
		GL11.glGetFloat(2982, instance.depthBuffer);
		instance.widthBuffer.flip().limit(16);
		instance.widthBuffer.get(instance.widthData);
		instance.depthBuffer.flip().limit(16);
		instance.depthBuffer.get(instance.depthData);
		instance.d[0] = instance.depthData[0] * instance.widthData[0] + instance.depthData[1] * instance.widthData[4] + instance.depthData[2] * instance.widthData[8] + instance.depthData[3] * instance.widthData[12];
		instance.d[1] = instance.depthData[0] * instance.widthData[1] + instance.depthData[1] * instance.widthData[5] + instance.depthData[2] * instance.widthData[9] + instance.depthData[3] * instance.widthData[13];
		instance.d[2] = instance.depthData[0] * instance.widthData[2] + instance.depthData[1] * instance.widthData[6] + instance.depthData[2] * instance.widthData[10] + instance.depthData[3] * instance.widthData[14];
		instance.d[3] = instance.depthData[0] * instance.widthData[3] + instance.depthData[1] * instance.widthData[7] + instance.depthData[2] * instance.widthData[11] + instance.depthData[3] * instance.widthData[15];
		instance.d[4] = instance.depthData[4] * instance.widthData[0] + instance.depthData[5] * instance.widthData[4] + instance.depthData[6] * instance.widthData[8] + instance.depthData[7] * instance.widthData[12];
		instance.d[5] = instance.depthData[4] * instance.widthData[1] + instance.depthData[5] * instance.widthData[5] + instance.depthData[6] * instance.widthData[9] + instance.depthData[7] * instance.widthData[13];
		instance.d[6] = instance.depthData[4] * instance.widthData[2] + instance.depthData[5] * instance.widthData[6] + instance.depthData[6] * instance.widthData[10] + instance.depthData[7] * instance.widthData[14];
		instance.d[7] = instance.depthData[4] * instance.widthData[3] + instance.depthData[5] * instance.widthData[7] + instance.depthData[6] * instance.widthData[11] + instance.depthData[7] * instance.widthData[15];
		instance.d[8] = instance.depthData[8] * instance.widthData[0] + instance.depthData[9] * instance.widthData[4] + instance.depthData[10] * instance.widthData[8] + instance.depthData[11] * instance.widthData[12];
		instance.d[9] = instance.depthData[8] * instance.widthData[1] + instance.depthData[9] * instance.widthData[5] + instance.depthData[10] * instance.widthData[9] + instance.depthData[11] * instance.widthData[13];
		instance.d[10] = instance.depthData[8] * instance.widthData[2] + instance.depthData[9] * instance.widthData[6] + instance.depthData[10] * instance.widthData[10] + instance.depthData[11] * instance.widthData[14];
		instance.d[11] = instance.depthData[8] * instance.widthData[3] + instance.depthData[9] * instance.widthData[7] + instance.depthData[10] * instance.widthData[11] + instance.depthData[11] * instance.widthData[15];
		instance.d[12] = instance.depthData[12] * instance.widthData[0] + instance.depthData[13] * instance.widthData[4] + instance.depthData[14] * instance.widthData[8] + instance.depthData[15] * instance.widthData[12];
		instance.d[13] = instance.depthData[12] * instance.widthData[1] + instance.depthData[13] * instance.widthData[5] + instance.depthData[14] * instance.widthData[9] + instance.depthData[15] * instance.widthData[13];
		instance.d[14] = instance.depthData[12] * instance.widthData[2] + instance.depthData[13] * instance.widthData[6] + instance.depthData[14] * instance.widthData[10] + instance.depthData[15] * instance.widthData[14];
		instance.d[15] = instance.depthData[12] * instance.widthData[3] + instance.depthData[13] * instance.widthData[7] + instance.depthData[14] * instance.widthData[11] + instance.depthData[15] * instance.widthData[15];
		instance.data[0][0] = instance.d[3] - instance.d[0];
		instance.data[0][1] = instance.d[7] - instance.d[4];
		instance.data[0][2] = instance.d[11] - instance.d[8];
		instance.data[0][3] = instance.d[15] - instance.d[12];
		a(instance.data, 0);
		instance.data[1][0] = instance.d[3] + instance.d[0];
		instance.data[1][1] = instance.d[7] + instance.d[4];
		instance.data[1][2] = instance.d[11] + instance.d[8];
		instance.data[1][3] = instance.d[15] + instance.d[12];
		a(instance.data, 1);
		instance.data[2][0] = instance.d[3] + instance.d[1];
		instance.data[2][1] = instance.d[7] + instance.d[5];
		instance.data[2][2] = instance.d[11] + instance.d[9];
		instance.data[2][3] = instance.d[15] + instance.d[13];
		a(instance.data, 2);
		instance.data[3][0] = instance.d[3] - instance.d[1];
		instance.data[3][1] = instance.d[7] - instance.d[5];
		instance.data[3][2] = instance.d[11] - instance.d[9];
		instance.data[3][3] = instance.d[15] - instance.d[13];
		a(instance.data, 3);
		instance.data[4][0] = instance.d[3] - instance.d[2];
		instance.data[4][1] = instance.d[7] - instance.d[6];
		instance.data[4][2] = instance.d[11] - instance.d[10];
		instance.data[4][3] = instance.d[15] - instance.d[14];
		a(instance.data, 4);
		instance.data[5][0] = instance.d[3] + instance.d[2];
		instance.data[5][1] = instance.d[7] + instance.d[6];
		instance.data[5][2] = instance.d[11] + instance.d[10];
		instance.data[5][3] = instance.d[15] + instance.d[14];
		a(instance.data, 5);
		return instance;
	}

	private static void a(float[][] var0, int var1) {
		float var2 = MathHelper.sqrt(var0[var1][0] * var0[var1][0] + var0[var1][1] * var0[var1][1] + var0[var1][2] * var0[var1][2]);
		var0[var1][0] /= var2;
		var0[var1][1] /= var2;
		var0[var1][2] /= var2;
		var0[var1][3] /= var2;
	}
	
	public boolean checkClipping(float x1, float y1, float z1, float x2, float y2, float z2) {
		for (int var7 = 0; var7 < 6; ++var7) {
			if (this.data[var7][0] * x1 + this.data[var7][1] * y1 + this.data[var7][2] * z1 + this.data[var7][3] <= 0.0F && this.data[var7][0] * x2 + this.data[var7][1] * y1 + this.data[var7][2] * z1 + this.data[var7][3] <= 0.0F && this.data[var7][0] * x1 + this.data[var7][1] * y2 + this.data[var7][2] * z1 + this.data[var7][3] <= 0.0F && this.data[var7][0] * x2 + this.data[var7][1] * y2 + this.data[var7][2] * z1 + this.data[var7][3] <= 0.0F && this.data[var7][0] * x1 + this.data[var7][1] * y1 + this.data[var7][2] * z2 + this.data[var7][3] <= 0.0F && this.data[var7][0] * x2 + this.data[var7][1] * y1 + this.data[var7][2] * z2 + this.data[var7][3] <= 0.0F && this.data[var7][0] * x1 + this.data[var7][1] * y2 + this.data[var7][2] * z2 + this.data[var7][3] <= 0.0F && this.data[var7][0] * x2 + this.data[var7][1] * y2 + this.data[var7][2] * z2 + this.data[var7][3] <= 0.0F) {
				return false;
			}
		}

		return true;
	}

}
