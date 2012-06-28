package com.mojang.util;

public final class MathHelper {

	private static float[] values = new float[65536];

	public static final float sin(float f) {
		return values[(int) (f * 10430.378) & Character.MAX_VALUE];
	}

	public static final float cos(float f) {
		return values[(int) (f * 10430.378 + 16384) & Character.MAX_VALUE];
	}

	public static final float sqrt(float f) {
		return (float) Math.sqrt(f);
	}

	static {
		for (int count = 0; count < Character.MAX_VALUE; ++count) {
			values[count] = (float) Math.sin(count * Math.PI * 2 / Character.MAX_VALUE);
		}

	}
}
