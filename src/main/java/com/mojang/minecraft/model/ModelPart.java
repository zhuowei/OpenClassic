package com.mojang.minecraft.model;

import com.mojang.minecraft.model.Vector;
import com.mojang.minecraft.model.Quad;
import com.mojang.minecraft.model.Vertex;
import org.lwjgl.opengl.GL11;

public final class ModelPart {

	public Vertex[] vertices;
	public Quad[] quads;
	private int u;
	private int v;
	public float x;
	public float y;
	public float z;
	public float pitch;
	public float yaw;
	public float roll;
	public boolean hasList = false;
	public int list = 0;
	public boolean mirror = false;
	public boolean render = true;

	public ModelPart(int u, int v) {
		this.u = u;
		this.v = v;
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
		if (this.mirror) {
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
		this.quads[0] = new Quad(new Vertex[] { var15, var11, var12, var21 }, this.u + z2 + x2, this.v + z2, this.u + z2 + x2 + z2, this.v + z2 + y2);
		this.quads[1] = new Quad(new Vertex[] { var20, var13, var14, var18 }, this.u, this.v + z2, this.u + z2, this.v + z2 + y2);
		this.quads[2] = new Quad(new Vertex[] { var15, var13, var20, var11 }, this.u + z2, this.v, this.u + z2 + x2, this.v + z2);
		this.quads[3] = new Quad(new Vertex[] { var12, var18, var14, var21 }, this.u + z2 + x2, this.v, this.u + z2 + x2 + x2, this.v + z2);
		this.quads[4] = new Quad(new Vertex[] { var11, var20, var18, var12 }, this.u + z2, this.v + z2, this.u + z2 + x2, this.v + z2 + y2);
		this.quads[5] = new Quad(new Vertex[] { var13, var15, var21, var14 }, this.u + z2 + x2 + z2, this.v + z2, this.u + z2 + x2 + z2 + x2, this.v + z2 + y2);
		if (this.mirror) {
			for (int q = 0; q < this.quads.length; ++q) {
				Quad quad = this.quads[q];
				Vertex[] vecs = new Vertex[quad.vertices.length];

				for (int vert = 0; vert < quad.vertices.length; ++vert) {
					vecs[vert] = quad.vertices[quad.vertices.length - vert - 1];
				}

				quad.vertices = vecs;
			}
		}

	}

	public final void setRotationPoint(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public final void render(float scale) {
		if (this.render) {
			if (!this.hasList) {
				this.generateList(scale);
			}

			if (this.pitch == 0 && this.yaw == 0 && this.roll == 0) {
				if (this.x == 0 && this.y == 0 && this.z == 0) {
					GL11.glCallList(this.list);
				} else {
					GL11.glTranslatef(this.x * scale, this.y * scale, this.z * scale);
					GL11.glCallList(this.list);
					GL11.glTranslatef(-this.x * scale, -this.y * scale, -this.z * scale);
				}
			} else {
				GL11.glPushMatrix();
				GL11.glTranslatef(this.x * scale, this.y * scale, this.z * scale);
				if (this.roll != 0) {
					GL11.glRotatef(this.roll * 57.295776F, 0.0F, 0.0F, 1.0F);
				}

				if (this.yaw != 0) {
					GL11.glRotatef(this.yaw * 57.295776F, 0.0F, 1.0F, 0.0F);
				}

				if (this.pitch != 0) {
					GL11.glRotatef(this.pitch * 57.295776F, 1.0F, 0.0F, 0.0F);
				}

				GL11.glCallList(this.list);
				GL11.glPopMatrix();
			}
		}
	}

	public void generateList(float scale) {
		this.list = GL11.glGenLists(1);
		GL11.glNewList(this.list, 4864);
		GL11.glBegin(GL11.GL_QUADS);

		for (int q = 0; q < this.quads.length; ++q) {
			Quad quad = this.quads[q];
			Vector var5 = quad.vertices[1].vector.subtract(quad.vertices[0].vector).normalize();
			Vector var6 = quad.vertices[1].vector.subtract(quad.vertices[2].vector).normalize();
			var5 = (new Vector(var5.y * var6.z - var5.z * var6.y, var5.z * var6.x - var5.x * var6.z, var5.x * var6.y - var5.y * var6.x)).normalize();
			GL11.glNormal3f(var5.x, var5.y, var5.z);

			for (int vertex = 0; vertex < 4; ++vertex) {
				Vertex vert = quad.vertices[vertex];
				GL11.glTexCoord2f(vert.u, vert.v);
				GL11.glVertex3f(vert.vector.x * scale, vert.vector.y * scale, vert.vector.z * scale);
			}
		}

		GL11.glEnd();
		GL11.glEndList();
		this.hasList = true;
	}
}
