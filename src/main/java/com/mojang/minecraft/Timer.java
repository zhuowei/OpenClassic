package com.mojang.minecraft;

public final class Timer {

	public float tps;
	public double lastHR;
	public int elapsedTicks;
	public float renderPartialTicks;
	public float speed = 1;
	public float elapsedPartialTicks = 0;
	public long lastSysClock;
	public long lastHRClock;
	public double adjustment = 1;

	public Timer(float tps) {
		this.tps = tps;
		this.lastSysClock = System.currentTimeMillis();
		this.lastHRClock = System.nanoTime() / 1000000L;
	}
}
