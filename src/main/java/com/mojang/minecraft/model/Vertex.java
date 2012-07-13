package com.mojang.minecraft.model;

import com.mojang.minecraft.model.Vector;

public class Vertex {

	public Vector vector;
	public float u;
	public float v;

	public Vertex(float x, float y, float z, float u, float v) {
		this(new Vector(x, y, z), u, v);
	}

	public Vertex create(float u, float v) {
		return new Vertex(this, u, v);
	}

	private Vertex(Vertex vert, float u, float v) {
		this.vector = vert.vector;
		this.u = u;
		this.v = v;
	}

	private Vertex(Vector vec, float u, float v) {
		this.vector = vec;
		this.u = u;
		this.v = v;
	}
}
