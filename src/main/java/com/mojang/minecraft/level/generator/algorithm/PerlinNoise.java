package com.mojang.minecraft.level.generator.algorithm;

import com.mojang.minecraft.level.generator.algorithm.Noise;
import java.util.Random;

public final class PerlinNoise extends Noise {

	private int[] permutations;

	public PerlinNoise() {
		this(new Random());
	}

	public PerlinNoise(Random rand) {
		this.permutations = new int[512];

		for (int count = 0; count < 256; this.permutations[count] = count++);

		for (int count = 0; count < 256; count++) {
			int var3 = rand.nextInt(256 - count) + count;
			int var4 = this.permutations[count];
			this.permutations[count] = this.permutations[var3];
			this.permutations[var3] = var4;
			this.permutations[count + 256] = this.permutations[count];
		}

	}

	private static double a(double var0) {
		return var0 * var0 * var0 * (var0 * (var0 * 6.0D - 15.0D) + 10.0D);
	}

	private static double lerp(double var0, double var2, double var4) {
		return var2 + var0 * (var4 - var2);
	}

	private static double grad(int var0, double var1, double var3, double var5) {
		double var7 = (var0 &= 15) < 8 ? var1 : var3;
		double var9 = var0 < 4 ? var3 : (var0 != 12 && var0 != 14 ? var5 : var1);
		return ((var0 & 1) == 0 ? var7 : -var7) + ((var0 & 2) == 0 ? var9 : -var9);
	}

	public final double compute(double x, double z) {
		double var5 = 0.0D;
		double var7 = z;
		double var9 = x;
		int var18 = (int) Math.floor(x) & 255;
		int var2 = (int) Math.floor(z) & 255;
		int var19 = (int) Math.floor(0.0D) & 255;
		var9 -= Math.floor(var9);
		var7 -= Math.floor(var7);
		var5 = 0.0D - Math.floor(0.0D);
		double var11 = a(var9);
		double var13 = a(var7);
		double var15 = a(var5);
		int var4 = this.permutations[var18] + var2;
		int var17 = this.permutations[var4] + var19;
		var4 = this.permutations[var4 + 1] + var19;
		var18 = this.permutations[var18 + 1] + var2;
		var2 = this.permutations[var18] + var19;
		var18 = this.permutations[var18 + 1] + var19;
		return lerp(var15, lerp(var13, lerp(var11, grad(this.permutations[var17], var9, var7, var5), grad(this.permutations[var2], var9 - 1.0D, var7, var5)), lerp(var11, grad(this.permutations[var4], var9, var7 - 1.0D, var5), grad(this.permutations[var18], var9 - 1.0D, var7 - 1.0D, var5))), lerp(var13, lerp(var11, grad(this.permutations[var17 + 1], var9, var7, var5 - 1.0D), grad(this.permutations[var2 + 1], var9 - 1.0D, var7, var5 - 1.0D)), lerp(var11, grad(this.permutations[var4 + 1], var9, var7 - 1.0D, var5 - 1.0D), grad(this.permutations[var18 + 1], var9 - 1.0D, var7 - 1.0D, var5 - 1.0D))));
	}
}
