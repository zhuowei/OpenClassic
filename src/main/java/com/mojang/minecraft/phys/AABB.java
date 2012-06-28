package com.mojang.minecraft.phys;

import com.mojang.minecraft.MovingObjectPosition;
import com.mojang.minecraft.model.Vector;
import java.io.Serializable;

public class AABB implements Serializable {

	public static final long serialVersionUID = 0L;
	private float epsilon = 0.0F;
	public float x0;
	public float y0;
	public float z0;
	public float x1;
	public float y1;
	public float z1;

	public AABB(float x0, float y0, float z0, float x1, float y1, float z1) {
		this.x0 = x0;
		this.y0 = y0;
		this.z0 = z0;
		this.x1 = x1;
		this.y1 = y1;
		this.z1 = z1;
	}

	public AABB expand(float x, float y, float z) {
		float x0 = this.x0;
		float y0 = this.y0;
		float z0 = this.z0;
		float x1 = this.x1;
		float y1 = this.y1;
		float z1 = this.z1;
		
		if (x < 0.0F) {
			x0 += x;
		}

		if (x > 0.0F) {
			x1 += x;
		}

		if (y < 0.0F) {
			y0 += y;
		}

		if (y > 0.0F) {
			y1 += y;
		}

		if (z < 0.0F) {
			z0 += z;
		}

		if (z > 0.0F) {
			z1 += z;
		}

		return new AABB(x0, y0, z0, x1, y1, z1);
	}

	public AABB grow(float x, float y, float z) {
		float x0 = this.x0 - x;
		float y0 = this.y0 - y;
		float z0 = this.z0 - z;
		float x1 = this.x1 + x;
		float y1 = this.y1 + y;
		float z1 = this.z1 + z;
		
		return new AABB(x0, y0, z0, x1, y1, z1);
	}

	public AABB cloneMove(float x, float y, float z) {
		return new AABB(this.x0 + z, this.y0 + y, this.z0 + z, this.x1 + x, this.y1 + y, this.z1 + z);
	}

	public float clipXCollide(AABB aabb, float x) {
		if (aabb.y1 > this.y0 && aabb.y0 < this.y1) {
			if (aabb.z1 > this.z0 && aabb.z0 < this.z1) {
				float collX = this.x0 - aabb.x1 - this.epsilon;
				if (x > 0.0F && aabb.x1 <= this.x0 && collX < x) {
					return collX;
				}
				
				collX = this.x1 - aabb.x0 + this.epsilon;
				if (x < 0.0F && aabb.x0 >= this.x1 && collX > x) {
					return collX;
				}

				return x;
			} else {
				return x;
			}
		} else {
			return x;
		}
	}

	public float clipYCollide(AABB aabb, float y) {
		if (aabb.x1 > this.x0 && aabb.x0 < this.x1) {
			if (aabb.z1 > this.z0 && aabb.z0 < this.z1) {
				float collY = this.y0 - aabb.y1 - this.epsilon;
				if (y > 0.0F && aabb.y1 <= this.y0 && collY < y) {
					return collY;
				}

				collY = this.y1 - aabb.y0 + this.epsilon;
				if (y < 0.0F && aabb.y0 >= this.y1 && collY > y) {
					return collY;
				}

				return y;
			} else {
				return y;
			}
		} else {
			return y;
		}
	}

	public float clipZCollide(AABB aabb, float z) {
		if (aabb.x1 > this.x0 && aabb.x0 < this.x1) {
			if (aabb.y1 > this.y0 && aabb.y0 < this.y1) {
				float collZ = this.z0 - aabb.z1 - this.epsilon;
				if (z > 0.0F && aabb.z1 <= this.z0 && collZ < z) {
					return collZ;
				}

				collZ = this.z1 - aabb.z0 + this.epsilon; 
				if (z < 0.0F && aabb.z0 >= this.z1 && collZ > z) {
					return collZ;
				}

				return z;
			} else {
				return z;
			}
		} else {
			return z;
		}
	}

	public boolean intersects(AABB aabb) {
		return aabb.x1 > this.x0 && aabb.x0 < this.x1 && aabb.y1 > this.y0 && aabb.y0 < this.y1 && aabb.z1 > this.z0 && aabb.z0 < this.z1;
	}

	public boolean intersectsInner(AABB aabb) {
		return aabb.x1 >= this.x0 && aabb.x0 <= this.x1 && aabb.y1 >= this.y0 && aabb.y0 <= this.y1 && aabb.z1 >= this.z0 && aabb.z0 <= this.z1;
	}

	public void move(float x, float y, float z) {
		this.x0 += x;
		this.y0 += y;
		this.z0 += z;
		this.x1 += x;
		this.y1 += y;
		this.z1 += z;
	}

	public boolean intersects(float x0, float y0, float z0, float x1, float y1, float z1) {
		return x1 > this.x0 && x0 < this.x1 && y1 > this.y0 && y0 < this.y1 && z1 > this.z0 && z0 < this.z1;
	}

	public boolean contains(Vector point) {
		return point.x > this.x0 && point.x < this.x1 && point.y > this.y0 && point.y < this.y1 && point.z > this.z0 && point.z < this.z1;
	}

	public float getSize() {
		float length = this.x1 - this.x0;
		float height = this.y1 - this.y0;
		float depth = this.z1 - this.z0;
		return (length + height + depth) / 3.0F;
	}

	public AABB shrink(float x, float y, float z) {
		float x0 = this.x0;
		float y0 = this.y0;
		float z0 = this.z0;
		float x1 = this.x1;
		float y1 = this.y1;
		float z1 = this.z1;
		
		if (x < 0.0F) {
			x0 -= x;
		}

		if (x > 0.0F) {
			x1 -= x;
		}

		if (y < 0.0F) {
			y0 -= y;
		}

		if (y > 0.0F) {
			y1 -= y;
		}

		if (z < 0.0F) {
			z0 -= z;
		}

		if (z > 0.0F) {
			z1 -= z;
		}

		return new AABB(x0, y0, z0, x1, y1, z1);
	}

	public AABB copy() {
		return new AABB(this.x0, this.y0, this.z0, this.x1, this.y1, this.z1);
	}

	public MovingObjectPosition clip(Vector point, Vector other) {
		Vector x0 = point.getXIntersection(other, this.x0);
		Vector x1 = point.getXIntersection(other, this.x1);
		Vector y0 = point.getYIntersection(other, this.y0);
		Vector y1 = point.getYIntersection(other, this.y1);
		Vector z0 = point.getZIntersection(other, this.z0);
		Vector z1 = point.getZIntersection(other, this.z1);
		
		if (!this.xIntersects(x0)) {
			x0 = null;
		}

		if (!this.xIntersects(x1)) {
			x1 = null;
		}

		if (!this.yIntersects(y0)) {
			y0 = null;
		}

		if (!this.yIntersects(y1)) {
			y1 = null;
		}

		if (!this.zIntersects(z0)) {
			z0 = null;
		}

		if (!this.zIntersects(z1)) {
			z1 = null;
		}

		Vector result = null;
		
		if (x0 != null) {
			result = x0;
		}

		if (x1 != null && (result == null || point.distanceSquared(x1) < point.distanceSquared(result))) {
			result = x1;
		}

		if (y0 != null && (result == null || point.distanceSquared(y0) < point.distanceSquared(result))) {
			result = y0;
		}

		if (y1 != null && (result == null || point.distanceSquared(y1) < point.distanceSquared(result))) {
			result = y1;
		}

		if (z0 != null && (result == null || point.distanceSquared(z0) < point.distanceSquared(result))) {
			result = z0;
		}

		if (z1 != null && (result == null || point.distanceSquared(z1) < point.distanceSquared(result))) {
			result = z1;
		}

		if (result == null) {
			return null;
		} else {
			byte side = -1;
			if (result == x0) {
				side = 4;
			}

			if (result == x1) {
				side = 5;
			}

			if (result == y0) {
				side = 0;
			}

			if (result == y1) {
				side = 1;
			}

			if (result == z0) {
				side = 2;
			}

			if (result == z1) {
				side = 3;
			}

			return new MovingObjectPosition(0, 0, 0, side, result);
		}
	}

	private boolean xIntersects(Vector point) {
		return point != null && point.y >= this.y0 && point.y <= this.y1 && point.z >= this.z0 && point.z <= this.z1;
	}

	private boolean yIntersects(Vector point) {
		return point != null && point.x >= this.x0 && point.x <= this.x1 && point.z >= this.z0 && point.z <= this.z1;
	}

	private boolean zIntersects(Vector point) {
		return point != null && point.x >= this.x0 && point.x <= this.x1 && point.y >= this.y0 && point.y <= this.y1;
	}
}
