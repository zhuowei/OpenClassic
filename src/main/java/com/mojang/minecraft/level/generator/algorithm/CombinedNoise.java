package com.mojang.minecraft.level.generator.algorithm;

import com.mojang.minecraft.level.generator.algorithm.Noise;

public final class CombinedNoise extends Noise {

	private Noise alg1;
	private Noise alg2;

	public CombinedNoise(Noise alg1, Noise alg2) {
		this.alg1 = alg1;
		this.alg2 = alg2;
	}

	public final double compute(double x, double z) {
		return this.alg1.compute(x + this.alg2.compute(x, z), z);
	}
}
