package com.mojang.minecraft;

public final class Timer {

	public float a;
	public double b;
	public int c;
	public float time;
	public float e = 1;
	public float f = 0;
	public long g;
	public long h;
	public double i = 1;

	public Timer(float var1) {
		this.a = var1;
		this.g = System.currentTimeMillis();
		this.h = System.nanoTime() / 1000000L;
	}
}
