package com.mojang.minecraft.model;

import com.mojang.minecraft.model.Vertex;

public final class Quad {

	public Vertex[] vertices;

	private Quad(Vertex[] vertices) {
		this.vertices = vertices;
	}

	public Quad(Vertex[] vertices, int var2, int var3, int var4, int var5) {
		this(vertices);
		float var7 = 0.0015625F;
		float var6 = 0.003125F;
		vertices[0] = vertices[0].a(var4 / 64.0F - var7, var3 / 32.0F + var6);
		vertices[1] = vertices[1].a(var2 / 64.0F + var7, var3 / 32.0F + var6);
		vertices[2] = vertices[2].a(var2 / 64.0F + var7, var5 / 32.0F - var6);
		vertices[3] = vertices[3].a(var4 / 64.0F - var7, var5 / 32.0F - var6);
	}

	public Quad(Vertex[] var1, float var2, float var3, float var4, float var5) {
		this(var1);
		var1[0] = var1[0].a(var4, var3);
		var1[1] = var1[1].a(var2, var3);
		var1[2] = var1[2].a(var2, var5);
		var1[3] = var1[3].a(var4, var5);
	}
}
