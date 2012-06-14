package com.mojang.minecraft.level.generator.algorithm;

import com.mojang.minecraft.level.generator.algorithm.Algorithm;
import com.mojang.minecraft.level.generator.algorithm.d;
import java.util.Random;

public final class b extends Algorithm {

	private d[] algs;
	private int count;

	public b(Random rand, int algs) {
		this.count = algs;
		this.algs = new d[algs];

		for (int count = 0; count < algs; ++count) {
			this.algs[count] = new d(rand);
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
