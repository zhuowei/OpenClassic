package com.mojang.minecraft.mob;

import java.io.Serializable;

import ch.spacebase.openclassic.api.block.VanillaBlock;

import com.mojang.minecraft.Entity;
import com.mojang.minecraft.item.Item;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.mob.QuadrupedMob;
import com.mojang.minecraft.mob.ai.BasicAI;
import com.mojang.minecraft.model.AnimalModel;
import com.mojang.minecraft.player.Player;
import com.mojang.minecraft.render.TextureManager;
import com.mojang.util.MathHelper;

import org.lwjgl.opengl.GL11;

public class Sheep extends QuadrupedMob {

	public static final long serialVersionUID = 0L;
	public boolean hasFur = true;
	public boolean grazing = false;
	public int grazingTime = 0;
	public float graze;
	public float grazeO;

	public Sheep(Level level, float x, float y, float z) {
		super(level, x, y, z);
		this.setSize(1.4F, 1.72F);
		this.setPos(x, y, z);
		this.heightOffset = 1.72F;
		this.modelName = "sheep";
		this.textureName = "/mob/sheep.png";
		this.ai = new SheepAI();
	}

	public void aiStep() {
		super.aiStep();
		this.grazeO = this.graze;
		if (this.grazing) {
			this.graze += 0.2F;
		} else {
			this.graze -= 0.2F;
		}

		if (this.graze < 0.0F) {
			this.graze = 0.0F;
		}

		if (this.graze > 1.0F) {
			this.graze = 1.0F;
		}

	}

	public void die(Entity cause) {
		if (cause != null) {
			cause.awardKillScore(this, 10);
		}

		int drops = (int) (Math.random() + Math.random() + 1.0D);

		for (int count = 0; count < drops; ++count) {
			this.level.addEntity(new Item(this.level, this.x, this.y, this.z, VanillaBlock.WHITE_CLOTH.getId()));
		}

		super.die(cause);
	}

	public void hurt(Entity cause, int damage) {
		if (this.hasFur && cause instanceof Player) {
			this.hasFur = false;
			int wool = (int) (Math.random() * 3.0D + 1.0D);

			for (int count = 0; count < wool; ++count) {
				this.level.addEntity(new Item(this.level, this.x, this.y, this.z, VanillaBlock.WHITE_CLOTH.getId()));
			}

		} else {
			super.hurt(cause, damage);
		}
	}

	public void renderModel(TextureManager textures, float var2, float var3, float var4, float var5, float var6, float var7) {
		AnimalModel model = (AnimalModel) modelCache.getModel(this.modelName);
		float var9 = model.b.y;
		float var10 = model.b.z;
		model.b.y += (this.grazeO + (this.graze - this.grazeO) * var3) * 8.0F;
		model.b.z -= this.grazeO + (this.graze - this.grazeO) * var3;
		super.renderModel(textures, var2, var3, var4, var5, var6, var7);
		if (this.hasFur) {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.bindTexture("/mob/sheep_fur.png"));
			GL11.glDisable(GL11.GL_CULL_FACE);
			AnimalModel fur = (AnimalModel) modelCache.getModel("sheep.fur");
			fur.b.g = model.b.g;
			fur.b.f = model.b.f;
			fur.b.y = model.b.y;
			fur.b.x = model.b.x;
			fur.c.g = model.c.g;
			fur.c.f = model.c.f;
			fur.d.f = model.d.f;
			fur.e.f = model.e.f;
			fur.f.f = model.f.f;
			fur.g.f = model.g.f;
			fur.b.a(var7);
			fur.c.a(var7);
			fur.d.a(var7);
			fur.e.a(var7);
			fur.f.a(var7);
			fur.g.a(var7);
		}

		model.b.y = var9;
		model.b.z = var10;
	}
	
	public class SheepAI extends BasicAI implements Serializable {
		private static final long serialVersionUID = 1L;

		public final void update() {
			float xDiff = -0.7F * MathHelper.sin(Sheep.this.yRot * (float) Math.PI / 180.0F);
			float zDiff = 0.7F * MathHelper.cos(Sheep.this.yRot * (float) Math.PI / 180.0F);
			int x = (int) (this.mob.x + xDiff);
			int y = (int) (this.mob.y - 2.0F);
			int z = (int) (this.mob.z + zDiff);
			if (Sheep.this.grazing) {
				if (this.level.getTile(x, y, z) != VanillaBlock.GRASS.getId()) {
					Sheep.this.grazing = false;
				} else {
					if (Sheep.this.grazingTime++ == 60) {
						this.level.setTile(x, y, z, VanillaBlock.DIRT.getId());
						if (this.random.nextInt(5) == 0) {
							Sheep.this.hasFur = true;
						}
					}

					this.xxa = 0.0F;
					this.yya = 0.0F;
					this.mob.xRot = 40 + Sheep.this.grazingTime / 2 % 2 * 10;
				}
			} else {
				if (this.level.getTile(x, y, z) == VanillaBlock.GRASS.getId()) {
					Sheep.this.grazing = true;
					Sheep.this.grazingTime = 0;
				}

				super.update();
			}
		}
	}
}
