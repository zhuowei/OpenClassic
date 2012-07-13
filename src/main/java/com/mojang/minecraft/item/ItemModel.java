package com.mojang.minecraft.item;

import ch.spacebase.openclassic.api.block.VanillaBlock;

import com.mojang.minecraft.model.ModelPart;
import com.mojang.minecraft.model.Quad;
import com.mojang.minecraft.model.Vertex;

public class ItemModel {

	private ModelPart box = new ModelPart(0, 0);

	public ItemModel(int block, int tex) {
		this.box.vertices = new Vertex[8];
		this.box.quads = new Quad[6];
		Vertex vert1 = new Vertex(-2.0F, -2.0F, -2.0F, 0.0F, 0.0F);
		Vertex vert2 = new Vertex(2.0F, -2.0F, -2.0F, 0.0F, 8.0F);
		Vertex vert3 = new Vertex(2.0F, 2.0F, -2.0F, 8.0F, 8.0F);
		Vertex vert4 = new Vertex(-2.0F, 2.0F, -2.0F, 8.0F, 0.0F);
		Vertex vert5 = new Vertex(-2.0F, -2.0F, 2.0F, 0.0F, 0.0F);
		Vertex vert6 = new Vertex(2.0F, -2.0F, 2.0F, 0.0F, 8.0F);
		Vertex vert7 = new Vertex(2.0F, 2.0F, 2.0F, 8.0F, 8.0F);
		Vertex vert8 = new Vertex(-2.0F, 2.0F, 2.0F, 8.0F, 0.0F);
		this.box.vertices[0] = vert1;
		this.box.vertices[1] = vert2;
		this.box.vertices[2] = vert3;
		this.box.vertices[3] = vert4;
		this.box.vertices[4] = vert5;
		this.box.vertices[5] = vert6;
		this.box.vertices[6] = vert7;
		this.box.vertices[7] = vert8;
		float u1 = ((tex % 16f) + (1.0F - 0.25F)) / 16.0F;
		float v1 = ((tex / 16f) + (1.0F - 0.25F)) / 16.0F - 0.02f;
		float u2 = ((tex % 16f) + 0.25F) / 16.0F;
		float v2 = ((tex / 16f) + 0.25F) / 16.0F - 0.02f;
		if(block == VanillaBlock.COBBLESTONE.getId() || block == VanillaBlock.RED_CLOTH.getId()) {
			v1 += 0.01f;
			v2 += 0.01f;
		} else if(block == VanillaBlock.PINK_CLOTH.getId() || block == VanillaBlock.INDIGO_CLOTH.getId() || block == VanillaBlock.MAGENTA_CLOTH.getId()) {
			v1 -= 0.01f;
			v2 -= 0.01f;
		} else if(block == VanillaBlock.GRAY_CLOTH.getId() || block == VanillaBlock.BLACK_CLOTH.getId()) {
			v1 -= 0.02f;
			v2 -= 0.02f;
		} else if(block == VanillaBlock.WHITE_CLOTH.getId()) {
			v1 -= 0.025f;
			v2 -= 0.025f;
		}
		this.box.quads[0] = new Quad(new Vertex[] { vert6, vert2, vert3, vert7 }, u1, v1, u2, v2);
		this.box.quads[1] = new Quad(new Vertex[] { vert1, vert5, vert8, vert4 }, u1, v1, u2, v2);
		this.box.quads[2] = new Quad(new Vertex[] { vert6, vert5, vert1, vert2 }, u1, v1, u2, v2);
		this.box.quads[3] = new Quad(new Vertex[] { vert3, vert4, vert8, vert7 }, u1, v1, u2, v2);
		this.box.quads[4] = new Quad(new Vertex[] { vert2, vert1, vert4, vert3 }, u1, v1, u2, v2);
		this.box.quads[5] = new Quad(new Vertex[] { vert5, vert6, vert7, vert8 }, u1, v1, u2, v2);
	}

	public void render() {
		this.box.render(0.0625F);
	}
}
