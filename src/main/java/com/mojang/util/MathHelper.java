package com.mojang.util;

public final class MathHelper {

	private static float[] a = new float[65536];

	public static final float a(float f) {
		return a[(int) (f * 10430.378) & Character.MAX_VALUE];
	}

	public static final float b(float f) {
		return a[(int) (f * 10430.378 + 16384) & Character.MAX_VALUE];
	}

	public static final float sqrt(float f) {
		return (float) Math.sqrt(f);
	}

	static {
		for (int var0 = 0; var0 < Character.MAX_VALUE; ++var0) {
			a[var0] = (float) Math.sin(var0 * Math.PI * 2 / Character.MAX_VALUE);
		}

	}
}
