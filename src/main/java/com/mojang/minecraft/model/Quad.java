package com.mojang.minecraft.model;

import com.mojang.minecraft.model.Vertex;

public final class Quad {

	public Vertex[] vertices;

	private Quad(Vertex[] vertices) {
		this.vertices = vertices;
	}

	public Quad(Vertex[] vertices, int u1, int v1, int u2, int v2) {
		this(vertices);
		vertices[0] = vertices[0].create(u2 / 64.0F - 0.0015625F, v1 / 32.0F + 0.003125F);
		vertices[1] = vertices[1].create(u1 / 64.0F + 0.0015625F, v1 / 32.0F + 0.003125F);
		vertices[2] = vertices[2].create(u1 / 64.0F + 0.0015625F, v2 / 32.0F - 0.003125F);
		vertices[3] = vertices[3].create(u2 / 64.0F - 0.0015625F, v2 / 32.0F - 0.003125F);
	}

	public Quad(Vertex[] vertices, float u1, float v1, float u2, float v2) {
		this(vertices);
		vertices[0] = vertices[0].create(u2, v1);
		vertices[1] = vertices[1].create(u1, v1);
		vertices[2] = vertices[2].create(u1, v2);
		vertices[3] = vertices[3].create(u2, v2);
	}
}
