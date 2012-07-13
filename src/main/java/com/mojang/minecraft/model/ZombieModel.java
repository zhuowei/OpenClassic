package com.mojang.minecraft.model;

import com.mojang.minecraft.model.HumanoidModel;
import com.mojang.util.MathHelper;

public class ZombieModel extends HumanoidModel {

	public final void b(float var1, float var2, float var3, float var4, float var5, float var6) {
		super.b(var1, var2, var3, var4, var5, var6);
		var1 = MathHelper.sin(this.a * 3.1415927F);
		var2 = MathHelper.sin((1.0F - (1.0F - this.a) * (1.0F - this.a)) * 3.1415927F);
		this.leggings.roll = 0.0F;
		this.arm.roll = 0.0F;
		this.leggings.yaw = -(0.1F - var1 * 0.6F);
		this.arm.yaw = 0.1F - var1 * 0.6F;
		this.leggings.pitch = -1.5707964F;
		this.arm.pitch = -1.5707964F;
		this.leggings.pitch -= var1 * 1.2F - var2 * 0.4F;
		this.arm.pitch -= var1 * 1.2F - var2 * 0.4F;
		this.leggings.roll += MathHelper.cos(var3 * 0.09F) * 0.05F + 0.05F;
		this.arm.roll -= MathHelper.cos(var3 * 0.09F) * 0.05F + 0.05F;
		this.leggings.pitch += MathHelper.sin(var3 * 0.067F) * 0.05F;
		this.arm.pitch -= MathHelper.sin(var3 * 0.067F) * 0.05F;
	}
}
