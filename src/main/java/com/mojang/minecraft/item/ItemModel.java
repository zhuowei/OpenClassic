package com.mojang.minecraft.item;

import com.mojang.minecraft.model.ModelRenderer;
import com.mojang.minecraft.model.Quad;
import com.mojang.minecraft.model.Vertex;

public final class ItemModel {

	private ModelRenderer renderer = new ModelRenderer(0, 0);

	public ItemModel(int var1) {
		int var2 = var1;
		this.renderer.vertices = new Vertex[8];
		this.renderer.quads = new Quad[6];
		Vertex var5 = new Vertex(-2.0F, -2.0F, -2.0F, 0.0F, 0.0F);
		Vertex var6 = new Vertex(2.0F, -2.0F, -2.0F, 0.0F, 8.0F);
		Vertex var7 = new Vertex(2.0F, 2.0F, -2.0F, 8.0F, 8.0F);
		Vertex var19 = new Vertex(-2.0F, 2.0F, -2.0F, 8.0F, 0.0F);
		Vertex var8 = new Vertex(-2.0F, -2.0F, 2.0F, 0.0F, 0.0F);
		Vertex var20 = new Vertex(2.0F, -2.0F, 2.0F, 0.0F, 8.0F);
		Vertex var9 = new Vertex(2.0F, 2.0F, 2.0F, 8.0F, 8.0F);
		Vertex var17 = new Vertex(-2.0F, 2.0F, 2.0F, 8.0F, 0.0F);
		this.renderer.vertices[0] = var5;
		this.renderer.vertices[1] = var6;
		this.renderer.vertices[2] = var7;
		this.renderer.vertices[3] = var19;
		this.renderer.vertices[4] = var8;
		this.renderer.vertices[5] = var20;
		this.renderer.vertices[6] = var9;
		this.renderer.vertices[7] = var17;
		float var10 = 0.25F;
		float var11 = 0.25F;
		float var12 = ((var2 % 16) + (1.0F - var10)) / 16.0F;
		float var13 = ((var2 / 16) + (1.0F - var11)) / 16.0F;
		var10 = ((var2 % 16) + var10) / 16.0F;
		float var18 = ((var2 / 16) + var11) / 16.0F;
		this.renderer.quads[0] = new Quad(new Vertex[] { var20, var6, var7, var9 }, var12, var13, var10, var18);
		this.renderer.quads[1] = new Quad(new Vertex[] { var5, var8, var17, var19 }, var12, var13, var10, var18);
		this.renderer.quads[2] = new Quad(new Vertex[] { var20, var8, var5, var6 }, var12, var13, var10, var18);
		this.renderer.quads[3] = new Quad(new Vertex[] { var7, var19, var17, var9 }, var12, var13, var10, var18);
		this.renderer.quads[4] = new Quad(new Vertex[] { var6, var5, var19, var7 }, var12, var13, var10, var18);
		this.renderer.quads[5] = new Quad(new Vertex[] { var8, var20, var9, var17 }, var12, var13, var10, var18);
	}

	public final void a() {
		this.renderer.a(0.0625F);
	}
}
