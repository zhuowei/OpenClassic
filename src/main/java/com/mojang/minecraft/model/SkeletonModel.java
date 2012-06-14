package com.mojang.minecraft.model;

import com.mojang.minecraft.model.ModelRenderer;
import com.mojang.minecraft.model.ZombieModel;

public final class SkeletonModel extends ZombieModel {

	public SkeletonModel() {
		this.e = new ModelRenderer(40, 16);
		this.e.addBox(-1.0F, -2.0F, -1.0F, 2, 12, 2, 0.0F);
		this.e.setPos(-5.0F, 2.0F, 0.0F);
		this.f = new ModelRenderer(40, 16);
		this.f.k = true;
		this.f.addBox(-1.0F, -2.0F, -1.0F, 2, 12, 2, 0.0F);
		this.f.setPos(5.0F, 2.0F, 0.0F);
		this.g = new ModelRenderer(0, 16);
		this.g.addBox(-1.0F, 0.0F, -1.0F, 2, 12, 2, 0.0F);
		this.g.setPos(-2.0F, 12.0F, 0.0F);
		this.h = new ModelRenderer(0, 16);
		this.h.k = true;
		this.h.addBox(-1.0F, 0.0F, -1.0F, 2, 12, 2, 0.0F);
		this.h.setPos(2.0F, 12.0F, 0.0F);
	}
}
