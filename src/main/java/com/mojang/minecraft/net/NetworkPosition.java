package com.mojang.minecraft.net;

public final class NetworkPosition {

	public float x;
	public float y;
	public float z;
	public float yaw;
	public float pitch;
	public boolean rotation = false;
	public boolean position = false;

	public NetworkPosition(float x, float y, float z, float yaw, float pitch) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
		this.rotation = true;
		this.position = true;
	}

	public NetworkPosition(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.position = true;
		this.rotation = false;
	}

	public NetworkPosition(float yaw, float pitch) {
		this.yaw = yaw;
		this.pitch = pitch;
		this.rotation = true;
		this.position = false;
	}
}
