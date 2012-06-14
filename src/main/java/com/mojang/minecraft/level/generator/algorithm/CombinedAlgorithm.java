package com.mojang.minecraft.level.generator.algorithm;

import com.mojang.minecraft.level.generator.algorithm.Algorithm;

public final class CombinedAlgorithm extends Algorithm {

	private Algorithm alg1;
	private Algorithm alg2;

	public CombinedAlgorithm(Algorithm alg1, Algorithm alg2) {
		this.alg1 = alg1;
		this.alg2 = alg2;
	}

	public final double compute(double x, double z) {
		return this.alg1.compute(x + this.alg2.compute(x, z), z);
	}
}
