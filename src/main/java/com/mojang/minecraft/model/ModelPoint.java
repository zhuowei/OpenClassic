package com.mojang.minecraft.model;

import com.mojang.util.MathHelper;

public final class ModelPoint {

	public float x;
	public float y;
	public float z;

	public ModelPoint(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public final ModelPoint subtract(ModelPoint other) {
		return new ModelPoint(this.x - other.x, this.y - other.y, this.z - other.z);
	}

	public final ModelPoint a() {
		float var1 = MathHelper.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
		return new ModelPoint(this.x / var1, this.y / var1, this.z / var1);
	}

	public final ModelPoint add(float x, float y, float z) {
		return new ModelPoint(this.x + x, this.y + y, this.z + z);
	}

	public final float distance(ModelPoint other) {
		return MathHelper.sqrt(this.distanceSquared(other));
	}

	public final float distanceSquared(ModelPoint other) {
		float x = other.x - this.x;
		float y = other.y - this.y;
		float z = other.z - this.z;
		return x * x + y * y + z * z;
	}

	public final ModelPoint getXIntersection(ModelPoint other, float intersectX) {
		float x = other.x - this.x;
		float y = other.y - this.y;
		float z = other.z - this.z;
		return x * x < 1.0E-7F ? null : ((intersectX = (intersectX - this.x) / x) >= 0.0F && intersectX <= 1.0F ? new ModelPoint(this.x + x * intersectX, this.y + y * intersectX, this.z + z * intersectX) : null);
	}

	public final ModelPoint getYIntersection(ModelPoint other, float intersectY) {
		float x = other.x - this.x;
		float y = other.y - this.y;
		float z = other.z - this.z;
		return y * y < 1.0E-7F ? null : ((intersectY = (intersectY - this.y) / y) >= 0.0F && intersectY <= 1.0F ? new ModelPoint(this.x + x * intersectY, this.y + y * intersectY, this.z + z * intersectY) : null);
	}

	public final ModelPoint getZIntersection(ModelPoint other, float intersectZ) {
		float x = other.x - this.x;
		float y = other.y - this.y;
		float z = other.z - this.z;
		return z * z < 1.0E-7F ? null : ((intersectZ = (intersectZ - this.z) / z) >= 0.0F && intersectZ <= 1.0F ? new ModelPoint(this.x + x * intersectZ, this.y + y * intersectZ, this.z + z * intersectZ) : null);
	}

	public final String toString() {
		return "(" + this.x + ", " + this.y + ", " + this.z + ")";
	}
}
