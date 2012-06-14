package com.mojang.minecraft.model;

import com.mojang.minecraft.model.ModelPoint;
import com.mojang.minecraft.model.Quad;
import com.mojang.minecraft.model.Vertex;
import org.lwjgl.opengl.GL11;

public final class ModelRenderer {

	public Vertex[] vertices;
	public Quad[] quads;
	private int m;
	private int n;
	public float x;
	public float y;
	public float z;
	public float f;
	public float g;
	public float h;
	public boolean i = false;
	public int list = 0;
	public boolean k = false;
	public boolean l = true;

	public ModelRenderer(int var1, int var2) {
		this.m = var1;
		this.n = var2;
	}

	public final void addBox(float x1, float y1, float z1, int x2, int y2, int z2, float var7) {
		this.vertices = new Vertex[8];
		this.quads = new Quad[6];
		float var8 = x1 + x2;
		float var9 = y1 + y2;
		float var10 = z1 + z2;
		x1 -= var7;
		y1 -= var7;
		z1 -= var7;
		var8 += var7;
		var9 += var7;
		var10 += var7;
		if (this.k) {
			var7 = var8;
			var8 = x1;
			x1 = var7;
		}

		Vertex var20 = new Vertex(x1, y1, z1, 0.0F, 0.0F);
		Vertex var11 = new Vertex(var8, y1, z1, 0.0F, 8.0F);
		Vertex var12 = new Vertex(var8, var9, z1, 8.0F, 8.0F);
		Vertex var18 = new Vertex(x1, var9, z1, 8.0F, 0.0F);
		Vertex var13 = new Vertex(x1, y1, var10, 0.0F, 0.0F);
		Vertex var15 = new Vertex(var8, y1, var10, 0.0F, 8.0F);
		Vertex var21 = new Vertex(var8, var9, var10, 8.0F, 8.0F);
		Vertex var14 = new Vertex(x1, var9, var10, 8.0F, 0.0F);
		this.vertices[0] = var20;
		this.vertices[1] = var11;
		this.vertices[2] = var12;
		this.vertices[3] = var18;
		this.vertices[4] = var13;
		this.vertices[5] = var15;
		this.vertices[6] = var21;
		this.vertices[7] = var14;
		this.quads[0] = new Quad(new Vertex[] { var15, var11, var12, var21 }, this.m + z2 + x2, this.n + z2, this.m + z2 + x2 + z2, this.n + z2 + y2);
		this.quads[1] = new Quad(new Vertex[] { var20, var13, var14, var18 }, this.m, this.n + z2, this.m + z2, this.n + z2 + y2);
		this.quads[2] = new Quad(new Vertex[] { var15, var13, var20, var11 }, this.m + z2, this.n, this.m + z2 + x2, this.n + z2);
		this.quads[3] = new Quad(new Vertex[] { var12, var18, var14, var21 }, this.m + z2 + x2, this.n, this.m + z2 + x2 + x2, this.n + z2);
		this.quads[4] = new Quad(new Vertex[] { var11, var20, var18, var12 }, this.m + z2, this.n + z2, this.m + z2 + x2, this.n + z2 + y2);
		this.quads[5] = new Quad(new Vertex[] { var13, var15, var21, var14 }, this.m + z2 + x2 + z2, this.n + z2, this.m + z2 + x2 + z2 + x2, this.n + z2 + y2);
		if (this.k) {
			for (int var16 = 0; var16 < this.quads.length; ++var16) {
				Quad var17;
				Vertex[] var19 = new Vertex[(var17 = this.quads[var16]).vertices.length];

				for (x2 = 0; x2 < var17.vertices.length; ++x2) {
					var19[x2] = var17.vertices[var17.vertices.length - x2 - 1];
				}

				var17.vertices = var19;
			}
		}

	}

	public final void setPos(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public final void a(float var1) {
		if (this.l) {
			if (!this.i) {
				this.b(var1);
			}

			if (this.f == 0.0F && this.g == 0.0F && this.h == 0.0F) {
				if (this.x == 0.0F && this.y == 0.0F && this.z == 0.0F) {
					GL11.glCallList(this.list);
				} else {
					GL11.glTranslatef(this.x * var1, this.y * var1, this.z * var1);
					GL11.glCallList(this.list);
					GL11.glTranslatef(-this.x * var1, -this.y * var1, -this.z * var1);
				}
			} else {
				GL11.glPushMatrix();
				GL11.glTranslatef(this.x * var1, this.y * var1, this.z * var1);
				if (this.h != 0.0F) {
					GL11.glRotatef(this.h * 57.295776F, 0.0F, 0.0F, 1.0F);
				}

				if (this.g != 0.0F) {
					GL11.glRotatef(this.g * 57.295776F, 0.0F, 1.0F, 0.0F);
				}

				if (this.f != 0.0F) {
					GL11.glRotatef(this.f * 57.295776F, 1.0F, 0.0F, 0.0F);
				}

				GL11.glCallList(this.list);
				GL11.glPopMatrix();
			}
		}
	}

	public void b(float var1) {
		this.list = GL11.glGenLists(1);
		GL11.glNewList(this.list, 4864);
		GL11.glBegin(7);

		for (int var2 = 0; var2 < this.quads.length; ++var2) {
			Quad var10000 = this.quads[var2];
			float var3 = var1;
			Quad var4 = var10000;
			ModelPoint var5 = var10000.vertices[1].a.subtract(var4.vertices[0].a).a();
			ModelPoint var6 = var4.vertices[1].a.subtract(var4.vertices[2].a).a();
			GL11.glNormal3f((var5 = (new ModelPoint(var5.y * var6.z - var5.z * var6.y, var5.z * var6.x - var5.x * var6.z, var5.x * var6.y - var5.y * var6.x)).a()).x, var5.y, var5.z);

			for (int var7 = 0; var7 < 4; ++var7) {
				Vertex var8;
				GL11.glTexCoord2f((var8 = var4.vertices[var7]).b, var8.c);
				GL11.glVertex3f(var8.a.x * var3, var8.a.y * var3, var8.a.z * var3);
			}
		}

		GL11.glEnd();
		GL11.glEndList();
		this.i = true;
	}
}
