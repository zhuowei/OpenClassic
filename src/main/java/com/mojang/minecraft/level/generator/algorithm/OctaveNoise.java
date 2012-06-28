package com.mojang.minecraft.level.generator.algorithm;

import com.mojang.minecraft.level.generator.algorithm.Noise;
import com.mojang.minecraft.level.generator.algorithm.PerlinNoise;
import java.util.Random;

public final class OctaveNoise extends Noise {

	private PerlinNoise[] algs;
	private int count;

	public OctaveNoise(Random rand, int algs) {
		this.count = algs;
		this.algs = new PerlinNoise[algs];

		for (int count = 0; count < algs; ++count) {
			this.algs[count] = new PerlinNoise(rand);
		}

	}

	public final double compute(double x, double z) {
		double result = 0;
		double var7 = 1;

		for (int count = 0; count < this.count; count++) {
			result += this.algs[count].compute(x / var7, z / var7) * var7;
			var7 *= 2;
		}

		return result;
	}
}
